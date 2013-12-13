package com.gmi.nordborglab.browser.server.security;

import com.gmi.nordborglab.browser.server.domain.acl.AclEntry;
import com.gmi.nordborglab.browser.server.domain.acl.AclSid;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.repository.AclSidRepository;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.NestedFilterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.IOException;
import java.security.acl.Permission;
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


    public EsAclManager() {
        aclSidCache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .build(new CacheLoader<Sid, Long>() {
                    @Override
                    public Long load(Sid sid) throws Exception {
                        return aclSidRepository.findBySid(SecurityUtil.sid2String.apply(sid)).getId();
                    }
                });

    }

    public static Function<AclSid, String> aclSid2String = new Function<AclSid, String>() {
        @Nullable
        @Override
        public String apply(@Nullable AclSid aclSid) {
            return aclSid.getId().toString();
        }
    };

    public FilterBuilder getAclFilter(List<String> permission) {
        return getAclFilter(permission, false, false);
    }

    public FilterBuilder getAclFilter(List<String> permissions, boolean isPersonal, boolean isPublic) {
        return getAclFilter(permissions, "acl", isPersonal, isPublic);
    }

    public FilterBuilder getAclFilter(List<String> permissions, String aclField, boolean isPersonal, boolean isPublic) {

        List<Sid> sids = SecurityUtil.getSids(roleHierarchy);
        List<AclSid> aclSids = aclSidRepository.findAllBySidIn(Lists.transform(sids, SecurityUtil.sid2String));
        List<String> aclSidsToCheck = Lists.transform(aclSids, aclSid2String);
        BoolFilterBuilder filter = FilterBuilders.boolFilter().must(FilterBuilders.nestedFilter(
                aclField, FilterBuilders.boolFilter().must(
                FilterBuilders.boolFilter().must(FilterBuilders.termsFilter(aclField + ".id", aclSidsToCheck),
                        FilterBuilders.termsFilter(aclField + ".permissions", permissions)))));

        if (isPersonal) {
            filter.must(FilterBuilders.termsFilter("owner.id", aclSidsToCheck));
        }
        if (isPublic) {
            AclSid publicSid = aclSidRepository.findBySid("ROLE_ANONYMOUS");
            filter.must(FilterBuilders.nestedFilter(
                    "acl", FilterBuilders.boolFilter().must(
                    FilterBuilders.boolFilter().must(FilterBuilders.termsFilter("acl.id", publicSid.getId().toString()),
                            FilterBuilders.termsFilter(aclField + ".permissions", permissions)))));
        }
        return filter;
    }

    public FilterBuilder getOwnerFilter(List<Long> ids) {
        return FilterBuilders.termsFilter("owner.sid", ids);
    }


    public XContentBuilder addACLAndOwnerContent(XContentBuilder builder, Acl acl) {
        try {
            String sid = SecurityUtil.sid2String.apply(acl.getOwner());
            AclSid aclSid = aclSidRepository.findAllBySid(sid);
            String name = "";
            if (aclSid.getPrincipal()) {
                AppUser user = userRepository.findOne(Long.parseLong(aclSid.getSid()));
                name = user.getFirstname() + " " + user.getLastname();
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
}
