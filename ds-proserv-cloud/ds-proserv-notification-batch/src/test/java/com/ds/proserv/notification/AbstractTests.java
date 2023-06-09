package com.ds.proserv.notification;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractTests {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected WebApplicationContext context;

	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	protected static String asJsonString(final Object obj) {
		try {

			System.out.println(new ObjectMapper().writeValueAsString(obj));
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}