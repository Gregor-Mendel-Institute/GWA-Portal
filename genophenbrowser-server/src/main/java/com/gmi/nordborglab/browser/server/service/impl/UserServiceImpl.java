package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.domain.acl.AclSid;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.acl.Authority;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.pages.AppUserPage;
import com.gmi.nordborglab.browser.server.domain.pages.ExperimentPage;
import com.gmi.nordborglab.browser.server.domain.pages.StudyPage;
import com.gmi.nordborglab.browser.server.domain.pages.TraitUomPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.form.Registration;
import com.gmi.nordborglab.browser.server.repository.AclSidRepository;
import com.gmi.nordborglab.browser.server.repository.ExperimentRepository;
import com.gmi.nordborglab.browser.server.repository.StudyRepository;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.repository.UserNotificationRepository;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.server.security.AclManager;
import com.gmi.nordborglab.browser.server.security.EsAclManager;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.DuplicateRegistrationException;
import com.gmi.nordborglab.browser.server.service.UserService;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.gmi.nordborglab.jpaontology.repository.TermRepository;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.RandomStringUtils;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.facet.filter.FilterFacet;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

@Service("userService")
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    @Resource
    private PasswordEncoder encoder;

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserNotificationRepository userNotificationRepository;

    @Resource
    private AclSidRepository aclSidRepository;

    @Resource
    private RoleHierarchy roleHierarchy;

    @Resource
    private Client client;

    @Resource
    private EsAclManager esAclManager;

    @Resource
    private AclManager aclManager;

    @Resource
    private ExperimentRepository experimentRepository;

    @Resource
    private TraitUomRepository traitUomRepository;

    @Resource
    private StudyRepository studyRepository;

    @Resource
    private TermRepository termRepository;


    @Override
    @Transactional(readOnly = false)
    public AppUser registerUserIfValid(Registration registration,
                                    boolean userIsValid) throws DuplicateRegistrationException {
        if (userIsValid) {
            if (userRepository.findByEmail(registration.getEmail()) != null) {
                throw new DuplicateRegistrationException();
            }
            AppUser appUser = new AppUser(registration.getEmail());
            appUser.setEmail(registration.getEmail());
            appUser.setFirstname(registration.getFirstname());
            appUser.setLastname(registration.getLastname());
            List<Authority> authorities = new ArrayList<Authority>();
            Authority authority = new Authority();
            authority.setAuthority(SecurityUtil.DEFAULT_AUTHORITY);
            authorities.add(authority);
            appUser.setAuthorities(authorities);

            if (!registration.isSocialAccount()) {
                appUser.setPassword("TEMPORARY");
                userRepository.save(appUser);
                appUser.setPassword(encoder.encode(registration.getPassword()));
            } else {
                appUser.setOpenidUser(true);
                appUser.setPassword(RandomStringUtils.random(8));
            }
            userRepository.save(appUser);
            //FIXME workaround because exception is thrown when AclSid doesnt exist and first time permission is added
            AclSid aclSid = new AclSid(true, appUser.getId().toString());
            aclSidRepository.save(aclSid);
            indexUser(appUser);
            return appUser;
        }
        return null;
    }

    @Override
    public AppUser findUser(Long id) {
        AppUser user = userRepository.findOne(id);
        return user;
    }

    @Override
    public AppUser findUserWithStats(Long id) {
        AppUser user = findUser(id);
        initStats(user);
        return user;
    }


    private int[] getStats(Long id) {

        BoolFilterBuilder searchFilter = FilterBuilders.boolFilter().must(esAclManager.getAclFilter(Lists.newArrayList("read")))
                .must(esAclManager.getOwnerFilter(Lists.newArrayList(id)));
        QueryBuilder query = QueryBuilders.constantScoreQuery(searchFilter);
        int[] stats = new int[3];
        stats[0] = 0;
        stats[1] = 0;
        stats[2] = 0;
        MultiSearchRequestBuilder request = client.prepareMultiSearch();
        request.add(
                client.prepareSearch(esAclManager.getIndex())
                        .setTypes("experiment")
                        .setNoFields()
                        .setSize(0)
                        .setQuery(query))
                .add(
                        client.prepareSearch(esAclManager.getIndex())
                                .setTypes("phenotype")
                                .setNoFields()
                                .setSize(0)
                                .setQuery(query))
                .add(client.prepareSearch(esAclManager.getIndex())
                        .setTypes("study")
                        .setNoFields()
                        .setSize(0)
                        .setQuery(query));
        MultiSearchResponse response = request.execute().actionGet();
        stats[0] = (int) response.getResponses()[0].getResponse().getHits().getTotalHits();
        stats[1] = (int) response.getResponses()[1].getResponse().getHits().getTotalHits();
        stats[2] = (int) response.getResponses()[2].getResponse().getHits().getTotalHits();
        return stats;
    }

    @Override
    @Transactional(readOnly = false)
    public AppUser saveUser(AppUser user) {
        AppUser existingUser = userRepository.findOne(user.getId());
        // only admins can change other user's information
        boolean isAdmin = SecurityUtil.isAdmin(roleHierarchy);
        if (!isAdmin && !existingUser.getId().equals(user.getId())) {
            return existingUser;
        }

        // set the basic values
        existingUser.setFirstname(user.getFirstname());
        existingUser.setLastname(user.getLastname());
        existingUser.setEmail(user.getEmail());
        existingUser.setAvatarSource(user.getAvatarSource());

        // change password
        if (user.getNewPassword() != null && !user.getNewPassword().isEmpty()) {
            existingUser.setPassword(encoder.encode(user.getNewPassword()));
            user.setNewPassword(null);
            user.setNewPasswordConfirm(null);
        }
        //set the authorities
        if (isAdmin) {

        }
        existingUser = userRepository.save(existingUser);
        if (user.getId().equals(existingUser.getId())) {
            SecurityUtil.updateAppUser(existingUser);
        }
        indexUser(existingUser);
        return existingUser;
    }

    @Override
    public ExperimentPage findExperiments(Long userId, int start, int size) {
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
        request.setSize(size).setFrom(start).setTypes("experiment").setNoFields();

        BoolFilterBuilder searchFilter = FilterBuilders.boolFilter().must(esAclManager.getAclFilter(Lists.newArrayList("read")))
                .must(esAclManager.getOwnerFilter(Lists.newArrayList(userId)));
        request.setQuery(QueryBuilders.constantScoreQuery(searchFilter));
        SearchResponse response = request.execute().actionGet();
        List<Long> idsToFetch = Lists.newArrayList();
        for (SearchHit hit : response.getHits()) {
            idsToFetch.add(Long.parseLong(hit.getId()));
        }
        List<Experiment> experiments = Lists.newArrayList();
        //Neded because ids are not sorted
        Map<Long, Experiment> id2Map = Maps.uniqueIndex(experimentRepository.findAll(idsToFetch), new Function<Experiment, Long>() {
            @Nullable
            @Override
            public Long apply(@Nullable Experiment experiment) {
                return experiment.getId();
            }
        });
        for (Long id : idsToFetch) {
            if (id2Map.containsKey(id)) {
                experiments.add(id2Map.get(id));
            }
        }
        aclManager.setPermissionAndOwners(experiments);
        return new ExperimentPage(experiments, new PageRequest(start, size), response.getHits().getTotalHits(), null);
    }

    @Override
    public TraitUomPage findPhenotypes(Long userId, int start, int size) {
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
        request.setSize(size).setFrom(start).setTypes("phenotype").setNoFields();

        BoolFilterBuilder searchFilter = FilterBuilders.boolFilter().must(esAclManager.getAclFilter(Lists.newArrayList("read")))
                .must(esAclManager.getOwnerFilter(Lists.newArrayList(userId)));
        request.setQuery(QueryBuilders.constantScoreQuery(searchFilter));

        SearchResponse response = request.execute().actionGet();
        List<Long> idsToFetch = Lists.newArrayList();
        for (SearchHit hit : response.getHits()) {
            idsToFetch.add(Long.parseLong(hit.getId()));
        }
        List<TraitUom> traits = Lists.newArrayList();
        //Neded because ids are not sorted

        Map<Long, TraitUom> id2Map = Maps.uniqueIndex(traitUomRepository.findAll(idsToFetch), new Function<TraitUom, Long>() {
            @Nullable
            @Override
            public Long apply(@Nullable TraitUom trait) {
                return trait.getId();
            }
        });
        for (Long id : idsToFetch) {
            if (id2Map.containsKey(id)) {
                traits.add(id2Map.get(id));
            }
        }
        for (TraitUom traitUom : traits) {
            initOntologies(traitUom);

        }
        aclManager.setPermissionAndOwners(traits);
        return new TraitUomPage(traits, new PageRequest(start, size), response.getHits().getTotalHits(), null);
    }

    private void initOntologies(TraitUom traitUom) {

        if (traitUom.getToAccession() != null) {
            traitUom.setTraitOntologyTerm(termRepository.findByAcc(traitUom.getToAccession()));
        }
        if (traitUom.getEoAccession() != null) {
            traitUom.setEnvironOntologyTerm(termRepository.findByAcc(traitUom.getEoAccession()));
        }
    }

    @Override
    public StudyPage findStudies(Long userId, int start, int size) {
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
        request.setSize(size).setFrom(start).setTypes("study").setNoFields();

        BoolFilterBuilder searchFilter = FilterBuilders.boolFilter().must(esAclManager.getAclFilter(Lists.newArrayList("read")))
                .must(esAclManager.getOwnerFilter(Lists.newArrayList(userId)));
        request.setQuery(QueryBuilders.constantScoreQuery(searchFilter));


        SearchResponse response = request.execute().actionGet();
        List<Long> idsToFetch = Lists.newArrayList();
        for (SearchHit hit : response.getHits()) {
            idsToFetch.add(Long.parseLong(hit.getId()));
        }
        List<Study> studies = Lists.newArrayList();
        //Neded because ids are not sorted

        Map<Long, Study> id2Map = Maps.uniqueIndex(studyRepository.findAll(idsToFetch), new Function<Study, Long>() {
            @Nullable
            @Override
            public Long apply(@Nullable Study study) {
                return study.getId();
            }
        });
        for (Long id : idsToFetch) {
            if (id2Map.containsKey(id)) {
                studies.add(id2Map.get(id));
            }
        }
        aclManager.setPermissionAndOwners(studies);
        return new StudyPage(studies, new PageRequest(start, size), response.getHits().getTotalHits(), null);
    }

    @Override
    public AppUserPage findUsers(String searchString, ConstEnums.USER_FILTER filter, int start, int size) {
        FilterBuilder searchFilter = null;
        FilterBuilder adminFilter = FilterBuilders.termFilter("authorities", "ROLE_ADMIN");
        FilterBuilder userFilter = FilterBuilders.termFilter("authorities", "ROLE_USER");
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
        request.setSize(size).setFrom(start).setTypes("user").setNoFields();

        if (searchString != null && !searchString.equalsIgnoreCase("")) {
            request.setQuery(multiMatchQuery(searchString, "firstname^3.5", "firstname.partial^1.5", "lastname^3.5", "lastname.partial^1.5", "email"));
        }
        // set facets
        request.addFacet(FacetBuilders.filterFacet(ConstEnums.USER_FILTER.ALL.name()).filter(FilterBuilders.matchAllFilter()));
        request.addFacet(FacetBuilders.filterFacet(ConstEnums.USER_FILTER.ADMIN.name()).filter(adminFilter));
        request.addFacet(FacetBuilders.filterFacet(ConstEnums.USER_FILTER.USER.name()).filter(userFilter));

        switch (filter) {
            case ADMIN:
                searchFilter = adminFilter;
                break;
            case USER:
                searchFilter = userFilter;
                break;
            default:
                if (searchString == null || searchString.isEmpty())
                    request.addSort("firstname.name", SortOrder.ASC);
        }
        // set filter
        request.setPostFilter(searchFilter);

        SearchResponse response = request.execute().actionGet();
        long totalCount = response.getHits().getTotalHits();
        List<Long> idsToFetch = Lists.newArrayList();
        for (SearchHit hit : response.getHits()) {
            idsToFetch.add(Long.parseLong(hit.getId()));
        }
        List<AppUser> users = Lists.newArrayList();
        //Neded because ids are not sorted
        Map<Long, AppUser> id2Map = Maps.uniqueIndex(userRepository.findAll(idsToFetch), new Function<AppUser, Long>() {
            @Nullable
            @Override
            public Long apply(@Nullable AppUser experiment) {
                return experiment.getId();
            }
        });
        for (Long id : idsToFetch) {
            if (id2Map.containsKey(id)) {
                users.add(id2Map.get(id));
            }
        }

        //extract facets
        Facets searchFacets = response.getFacets();
        List<ESFacet> facets = Lists.newArrayList();

        FilterFacet filterFacet = (FilterFacet) searchFacets.facetsAsMap().get(ConstEnums.TABLE_FILTER.ALL.name());
        facets.add(new ESFacet(ConstEnums.USER_FILTER.ALL.name(), 0, filterFacet.getCount(), 0, null));

        filterFacet = (FilterFacet) searchFacets.facetsAsMap().get(ConstEnums.USER_FILTER.ADMIN.name());
        facets.add(new ESFacet(ConstEnums.USER_FILTER.ADMIN.name(), 0, filterFacet.getCount(), 0, null));

        filterFacet = (FilterFacet) searchFacets.facetsAsMap().get(ConstEnums.USER_FILTER.USER.name());
        facets.add(new ESFacet(ConstEnums.USER_FILTER.USER.name(), 0, filterFacet.getCount(), 0, null));

        addStatsAndGravatarHash(users);
        return new AppUserPage(users, new PageRequest(start, size), totalCount, facets);
    }

    private void addStatsAndGravatarHash(List<AppUser> users) {
        for (AppUser user : users) {
            initStats(user);
        }
    }

    private void initStats(AppUser user) {
        int[] stats = getStats(user.getId());
        user.setNumberOfStudies(stats[0]);
        user.setNumberOfPhenotypes(stats[1]);
        user.setNumberOfAnalysis(stats[2]);
    }

    private void deleteFromIndex(Long userId) {
        client.prepareDelete(esAclManager.getIndex(), "user", userId.toString()).execute();
    }

    private void indexUser(AppUser user) {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();

            builder.startObject()
                    .field("username", user.getUsername())
                    .field("email", user.getEmail())
                    .field("firstname", user.getFirstname())
                    .field("lastname", user.getLastname());
            builder.endObject();
            IndexRequestBuilder request = client.prepareIndex(esAclManager.getIndex(), "user", user.getId().toString())
                    .setSource(builder);

            request.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
