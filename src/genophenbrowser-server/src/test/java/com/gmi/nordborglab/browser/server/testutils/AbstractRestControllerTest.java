package com.gmi.nordborglab.browser.server.testutils;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;

/**
 * Created by uemit.seren on 7/7/15.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(profiles = "dev")
@WebAppConfiguration
@ContextHierarchy({
        @ContextConfiguration("classpath:test-rest-controller.xml"),
        @ContextConfiguration("file:genophenbrowser-server/src/main/webapp/WEB-INF/spring-servlet.xml")
})
public class AbstractRestControllerTest {

    protected MockMvc mockMvc;
    public final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    @Autowired
    private WebApplicationContext context;

    @Before
    public void setUpBase() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context).build();
    }

    @After
    public void clearContext() {
        SecurityUtils.clearContext();
    }


}
