package com.ds.proserv.shell;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles(value = "unittest")
@Import(TestApplicationRunner.class)
@Slf4j
public class DSShellApplicationTests {


	@Before
	public void purgeAllMessages_Before() throws IOException, TimeoutException {

//		log.info("In Before -> {}", shell);
	}

	@After
	public void purgeAllMessages_After() throws IOException, TimeoutException {

	}

	@Test
	public void testMarkSelectedApplications() {

//		String shellCommand = "markSelectedApplications csvFullPath " + "data/selected-applications.csv " + "drawReferenceId " +  "12345";
//
//		assertThat(shell.evaluate(() -> shellCommand));

//		log.info("In Before -> {}", shell);

		log.info("In testMarkSelectedApplications");
	}


	@Test
	public void testAssignReviewer() {

//		String shellCommand = "assignReviewer";
//
//		assertThat(shell.evaluate(() -> shellCommand));

		log.info("In testAssignReviewer");

	}


}