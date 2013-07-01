package com.gmi.nordborglab.browser.server.security;

import com.gmi.nordborglab.browser.server.domain.acl.AclSid;
import com.gmi.nordborglab.browser.server.repository.AclSidRepository;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.security.acl.Permission;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 28.06.13
 * Time: 13:42
 * To change this template use File | Settings | File Templates.
 */

@Component
public class EsAclManager {


    @Resource
    protected RoleHierarchy roleHierarchy;

    @Resource
    protected AclSidRepository aclSidRepository;

    public static Function<AclSid, String> aclSid2String = new Function<AclSid, String>() {
        @Nullable
        @Override
        public String apply(@Nullable AclSid aclSid) {
            return aclSid.getId().toString();
        }
    };

    public FilterBuilder getAclFilter(List<String> permissions) {
        List<String> aclSidsToCheck = Lists.newArrayList();
        List<Sid> sids = SecurityUtil.getSids(roleHierarchy);
        List<AclSid> aclSids = aclSidRepository.findAllBySidIn(Lists.transform(sids, SecurityUtil.sid2String));
        FilterBuilder filter = FilterBuilders.nestedFilter(
                "acl", FilterBuilders.boolFilter().must(
                FilterBuilders.termsFilter("acl.id", Lists.transform(aclSids, aclSid2String)),
                FilterBuilders.termsFilter("acl.permissions", permissions)
        )
        );
        return filter;
    }
}
