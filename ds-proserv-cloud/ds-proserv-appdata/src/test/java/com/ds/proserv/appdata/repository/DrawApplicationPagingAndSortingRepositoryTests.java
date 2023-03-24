package com.ds.proserv.appdata.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ds.proserv.appdata.model.DrawApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.cloud.config.enabled=false")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles(value = "unittest")
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "auditorTestProvider")
@TestPropertySource(locations = "classpath:application-unittest.yml")
public class DrawApplicationPagingAndSortingRepositoryTests {

	@Autowired
	private DrawApplicationPagingAndSortingRepository drawApplicationPagingAndSortingRepository;

	@Test
	public void testCountByBatchIdAndProcessEndDateTime() {

		List<DrawApplication> drawApplicationList = drawApplicationPagingAndSortingRepository
				.findAllByTriggerEnvelopeIdOrBridgeEnvelopeId("e781ca58-dec7-44b7-a312-5c21fded402d",
						"e781ca58-dec7-44b7-a312-5c21fded402d");
		assertThat(drawApplicationList).isNotNull();
	}

}