package com.gmi.nordborglab.browser.server.security;

import com.gmi.nordborglab.browser.server.domain.acl.AclSid;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.repository.AclSidRepository;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 28.06.13
 * Time: 13:42
 * To change this template use File | Settings | File Templates.
 */

@Component
public class EsAclManager {

    @Value("${ELASTICSEARCH.gdpmd.index}")
    private String index;

    @Resource
    protected RoleHierarchy roleHierarchy;

    @Resource
    protected AclSidRepository aclSidRepository;

    @Resource
    protected UserRepository userRepository;

    protected LoadingCache<Sid, Long> aclSidCache;

    protected Long publicAclSidId;
    protected final Sid ANONYMOUS_SID = new GrantedAuthoritySid("ROLE_ANONYMOUS");
    protected final Predicate<Sid> isAnonymousUser = Predicates.equalTo(ANONYMOUS_SID);


    public EsAclManager() {
        aclSidCache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .build(new CacheLoader<Sid, Long>() {
                    @Override
                    public Long load(Sid sid) {
                        AclSid aclSid = aclSidRepository.findBySid(SecurityUtil.sid2String.apply(sid));
                        if (aclSid == null)
                            return 0L;
                        return aclSid.getId();
                    }
                });
    }


    public static Function<AclSid, String> aclSid2String = new Function<AclSid, String>() {
        @Override
        public String apply(AclSid aclSid) {
            Preconditions.checkNotNull(aclSid);
            return aclSid.getId().toString();
        }
    };


    public QueryBuilder getAclFilterForPermissions(List<String> permissions, String aclField) {
         return getAclFilterForPermissions(permissions,aclField,false);
    }

    public QueryBuilder getAclFilterForPermissions(List<String> permissions, boolean noPublic) {
        return getAclFilterForPermissions(permissions,"acl",false);
    }


    public QueryBuilder getAclFilterForPermissions(List<String> permissions) {
        return getAclFilterForPermissions(permissions, "acl", false);
    }

    public QueryBuilder getAclFilterForPermissions(List<String> permissions, String aclField, boolean noPublic) {
        FluentIterable<Sid> sids = FluentIterable.from(SecurityUtil.getSids(roleHierarchy));
        // don't include public ones
        if (noPublic)
            sids = sids.filter(Predicates.not(isAnonymousUser));
        List<String> stringSids = sids.transform(SecurityUtil.sid2String).toList();
        List<AclSid> aclSids = aclSidRepository.findAllBySidIn(stringSids);
        List<String> aclSidsToCheck = Lists.transform(aclSids, aclSid2String);
        BoolQueryBuilder filter = QueryBuilders.boolQuery().filter(QueryBuilders.nestedQuery(
                aclField, QueryBuilders.boolQuery()
                        .filter(QueryBuilders.boolQuery().filter(QueryBuilders.termsQuery(aclField + ".id", aclSidsToCheck))
                                .filter(QueryBuilders.termsQuery(aclField + ".permissions", permissions)))));
        return filter;
    }


    public QueryBuilder getAclFilterForType(ConstEnums.TABLE_FILTER filter) {
        QueryBuilder filterBuilder = null;
        Authentication auth = SecurityUtil.getAuthentication();
        Long userAclSid = aclSidCache.getUnchecked(new PrincipalSid(auth));
        switch (filter) {
            case PRIVATE:
                if (auth.isAuthenticated()) {
                    filterBuilder = QueryBuilders.boolQuery().filter(QueryBuilders.termQuery("owner.id", userAclSid));
                }
                break;
            case SHARED:
                if (auth.isAuthenticated()) {
                    filterBuilder = QueryBuilders.boolQuery().filter(
                            QueryBuilders.nestedQuery("acl", QueryBuilders.boolQuery()
                                    .filter(QueryBuilders.boolQuery().filter(QueryBuilders.termsQuery("acl.id", userAclSid.toString()))
                                            .filter(QueryBuilders.termsQuery("acl.permissions", "read")))))
                            .mustNot(QueryBuilders.termsQuery("owner.id", userAclSid.toString()));
                }
                break;
            case PUBLISHED:
                filterBuilder = QueryBuilders.nestedQuery("acl", QueryBuilders.boolQuery()
                        .filter(QueryBuilders.boolQuery()
                                .filter(QueryBuilders.termQuery("acl.id", publicAclSidId))
                                .filter(QueryBuilders.termsQuery("acl.permissions", "read"))));
                break;
        }
        return filterBuilder;
    }


    public QueryBuilder getOwnerFilter(List<Long> ids) {
        return QueryBuilders.termsQuery("owner.sid", ids);
    }


    public XContentBuilder addACLAndOwnerContent(XContentBuilder builder, Acl acl) {
        try {
            String sid = SecurityUtil.sid2String.apply(acl.getOwner());
            AclSid aclSid = aclSidRepository.findAllBySid(sid);
            String name = "";
            if (aclSid.getPrincipal()) {
                AppUser user = userRepository.findOne(Long.parseLong(aclSid.getSid()));
                if (user != null) {
                    name = user.getFirstname() + " " + user.getLastname();
                }
            }

            // add owner:
            builder.startObject("owner")
                    .field("id", aclSid.getId())
                    .field("sid", sid)
                    .field("name", name);
            builder.endObject();
            builder.startArray("acl");
            addACLContent(builder, acl);
            builder.endArray();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return builder;
    }

    public XContentBuilder addACLContent(XContentBuilder builder, Acl acl) {
        try {
            for (AccessControlEntry ace : acl.getEntries()) {
                Long id = aclSidCache.getUnchecked(ace.getSid());
                builder.startObject()
                        .field("id", id);
                builder.startArray("permissions");
                if ((CustomPermission.READ.getMask() & ace.getPermission().getMask()) == CustomPermission.READ.getMask()) {
                    builder.value("read");
                }
                if ((CustomPermission.EDIT.getMask() & ace.getPermission().getMask()) == CustomPermission.EDIT.getMask()) {
                    builder.value("write");
                }
                if ((CustomPermission.ADMINISTRATION.getMask() & ace.getPermission().getMask()) == CustomPermission.ADMINISTRATION.getMask()) {
                    builder.value("admin");
                }
                builder.endArray();
                builder.endObject();
            }
            if (acl.isEntriesInheriting() && acl.getParentAcl() != null) {
                return addACLContent(builder, acl.getParentAcl());
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return builder;
    }

    public List<Map<String, Object>> getACLContent(List<Map<String, Object>> permissions, Acl acl) {
        if (permissions == null) {
            permissions = Lists.newArrayList();
        }
        for (AccessControlEntry ace : acl.getEntries()) {
            Map<String, Object> aclMap = new HashMap<String, Object>();
            Long id = aclSidCache.getUnchecked(ace.getSid());
            aclMap.put("id", id);
            List<String> perms = Lists.newArrayList();
            if ((CustomPermission.READ.getMask() & ace.getPermission().getMask()) == CustomPermission.READ.getMask()) {
                perms.add("read");
            }
            if ((CustomPermission.EDIT.getMask() & ace.getPermission().getMask()) == CustomPermission.EDIT.getMask()) {
                perms.add("write");
            }
            if ((CustomPermission.ADMINISTRATION.getMask() & ace.getPermission().getMask()) == CustomPermission.ADMINISTRATION.getMask()) {
                perms.add("admin");
            }
            aclMap.put("permissions", perms);
            permissions.add(aclMap);
        }
        if (acl.isEntriesInheriting() && acl.getParentAcl() != null) {
            return getACLContent(permissions, acl.getParentAcl());
        }
        return permissions;
    }

    public String getIndex() {
        return index;
    }

    @PostConstruct
    public void init() {
        publicAclSidId = aclSidRepository.findBySid("ROLE_ANONYMOUS").getId();
    }
}
