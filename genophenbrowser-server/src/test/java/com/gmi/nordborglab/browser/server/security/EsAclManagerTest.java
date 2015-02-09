package com.gmi.nordborglab.browser.server.security;

import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.google.common.collect.Lists;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.FilterBuilder;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 28.06.13
 * Time: 14:32
 * To change this template use File | Settings | File Templates.
 */
public class EsAclManagerTest extends BaseTest {

    @Resource
    protected EsAclManager esAclManager;

    @Test
    public void testGetAclFilter() {
        SecurityUtils.setAnonymousUser();
        FilterBuilder filter = esAclManager.getAclFilterForPermissions(Lists.newArrayList("read"));
        assertNotNull(filter);
        String filterJson = "";
        XContentBuilder builder = null;
        try {
            builder = XContentFactory.jsonBuilder();
            filter.toXContent(builder, ToXContent.EMPTY_PARAMS);
            filterJson = builder.string(); // it will be
        } catch (IOException e) {
            fail();
        } finally {
            if (builder != null)
                builder.close();
        }

        assertEquals("{\"nested\":{\"filter\":{\"bool\":{\"must\":[{\"terms\":{\"acl.id\":[\"3\"]}},{\"terms\":{\"acl.permission\":[\"read\"]}}]}},\"path\":\"acl\"}}", filterJson);
    }

}
