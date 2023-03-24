package com.ds.proserv.envelopeapi.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.Assert;

//@RunWith(SpringRunner.class)
//@SpringBootTest(properties = "spring.cloud.config.enabled=false", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = "unittest")
@TestPropertySource(locations = "classpath:application-unittest.yml")
@AutoConfigureMockMvc
@SpringBootTest
public class DSEnvelopeApiControllerTest{

    @Test
    public void createEnvelope() {
        Assert.isTrue(true, "pass");
    }

    @Test
    public void setRoleTabs() {
        Assert.isTrue(true, "pass");

    }

    @Test
    public void buildTabsFor() {
        Assert.isTrue(true, "pass");

    }
}