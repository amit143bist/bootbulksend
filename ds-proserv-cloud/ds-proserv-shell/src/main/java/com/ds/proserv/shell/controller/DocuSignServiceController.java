package com.ds.proserv.shell.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;
import com.ds.proserv.feign.shell.domain.Recipients;
import com.ds.proserv.feign.shell.domain.Signer;
import com.ds.proserv.feign.shell.domain.UpdateRecipientRequest;
import com.ds.proserv.feign.shell.domain.UpdateRecipientResponse;
import com.ds.proserv.feign.shell.domain.User;
import com.ds.proserv.feign.shell.domain.UsersSet;
import com.ds.proserv.feign.shell.service.DocuSignService;
import com.ds.proserv.feign.util.ApiLimitUtil;
import com.ds.proserv.shell.client.AuthenticationClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class DocuSignServiceController implements DocuSignService {

	@Value("${docusign.api.baseuri}")
	private String baseuri;

	@Value("${docusign.api.account}")
	private String account;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	private HttpHeaders headers;

	@Value("${docusign.api.userid}")
	private String dsUserId;

	@Value("${docusign.api.scopes}")
	private String dsScopes;

	@Value("${docusign.api.group.pagesize}")
	private String pagesize;

	@Value("${bulk.update.rolename}")
	private String roleName;

	@Value("${docusign.api.apithresholdlimitpercent}")
	private Integer apiThresholdlLimitPercent;

	private String groupId;

	@Autowired
	private AuthenticationClient authenticationClient;

	@Override
	public ResponseEntity<List<User>> getUsersByGroup(String groupId, String status) {
		log.info("DocuSignServiceController.getUsersByGroup() group -> {}, pagesize -> {}, status -> {}", groupId,
				pagesize, status);
		Assert.notNull(groupId, "groupId was null");
		Assert.notNull(status, "status was null");
		this.groupId = groupId;

		String relativeUri = "/users?group_id={groupId}&status=active&count={userPageSize}";
		List<User> activeUsers = new ArrayList<>();
		return new ResponseEntity<>(getUsers(relativeUri, activeUsers), HttpStatus.OK);
	}

	private List<User> getUsers(String relativeUri, List<User> activeUsers) {

		String uri = baseuri + "/accounts/" + account + relativeUri;
		setHeaders();
		HttpEntity<String> requestEntity = new HttpEntity<>(headers);

		return Optional
				.ofNullable(
						restTemplate.exchange(uri, HttpMethod.GET, requestEntity, UsersSet.class, groupId, pagesize))
				.map(userSet -> {

					Assert.notNull(userSet.getBody(), "userSet is null for group  " + groupId);
					Assert.isTrue(userSet.getStatusCode().is2xxSuccessful(),
							"ProntoSignedDrafts is not returned with 200 status code");
					Assert.notEmpty(userSet.getBody().users, "User list in set is null for group  " + groupId);

					activeUsers.addAll(userSet.getBody().getUsers());

					ApiLimitUtil.readApiHourlyLimitData(userSet.getHeaders(), apiThresholdlLimitPercent);

					if (!StringUtils.isEmpty(userSet.getBody().nextUri)) {
						activeUsers.addAll(getUsers(userSet.getBody().nextUri, activeUsers));
					}
					return activeUsers;

				}).orElseThrow(() -> new ResourceNotFoundException("No users found in group: " + groupId));
	}

	public ResponseEntity<Signer> getRecipientByRole(@PathVariable String roleName,
			@PathVariable("envelope") String envelope) {

		Signer signer = getRecipients(envelope).getBody().getSigners().stream().filter(filterSigner -> {
			if (roleName.equalsIgnoreCase((filterSigner.getRoleName()))) {
				return true;
			} else {
				return false;
			}
		}).findFirst().orElse(null);

		return new ResponseEntity<Signer>(signer, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Recipients> getRecipients(String envelope) {
		log.info("DsUserService.getRecipients() getRecipients -> {}", envelope);

		String uri = baseuri + "/accounts/" + account + "/envelopes/{envelope}/recipients?include_tabs=true";

		setHeaders();

		HttpEntity<String> requestEntity = new HttpEntity<>(headers);
		return Optional
				.ofNullable(restTemplate.exchange(uri, HttpMethod.GET, requestEntity, Recipients.class, envelope))
				.map(recipients -> {

					Assert.notNull(recipients.getBody(), "No recipient return for envelope  " + envelope);
					Assert.isTrue(recipients.getStatusCode().is2xxSuccessful(),
							"ProntoSignedDrafts is not returned with 200 status code");
					Assert.notEmpty(recipients.getBody().signers,
							"Signer list in recipients is empty for envelope:  " + envelope);

					ApiLimitUtil.readApiHourlyLimitData(recipients.getHeaders(), apiThresholdlLimitPercent);
					return new ResponseEntity<Recipients>(recipients.getBody(), HttpStatus.OK);

				}).orElseThrow(() -> new ResourceNotFoundException(
						"Signer list in recipients is empty for envelope:  " + envelope));
	}

	@Override
	public ResponseEntity<UpdateRecipientResponse> updateRecipient(UpdateRecipientRequest request, String envelope) {
		log.info("DsUserService.updateRecipient() getRecipients -> {}", envelope);
		Assert.notNull(request, "Recipient is null in UpdateRecipientRequest");

		String uri = baseuri + "/accounts/" + account + "/envelopes/{envelope}?resend_envelope=true";
		log.info(" Calling updateRecipient URI -> {}", uri);
		setHeaders();

		Map<String, String> param = new HashMap<String, String>();
		param.put("envelope", envelope);
		HttpEntity<UpdateRecipientRequest> requestEntity = new HttpEntity<UpdateRecipientRequest>(request, headers);

		return Optional
				.ofNullable(
						restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, UpdateRecipientResponse.class, param))
				.map(recipientUpdateResults -> {

					Assert.notNull(recipientUpdateResults.getBody(), "No recipient return for envelope  " + envelope);
					Assert.isTrue(recipientUpdateResults.getStatusCode().is2xxSuccessful(),
							"uddateRecipient is not returned with 200 status code");
					Assert.notEmpty(recipientUpdateResults.getBody().getRecipientUpdateResults(),
							"update status is empty for envelope:  " + envelope);
					ApiLimitUtil.readApiHourlyLimitData(recipientUpdateResults.getHeaders(), apiThresholdlLimitPercent);

					return new ResponseEntity<>(recipientUpdateResults.getBody(), HttpStatus.OK);

				})
				.orElseThrow(() -> new ResourceNotFoundException("update status is empty for envelope:  " + envelope));
	}

	private void setHeaders() {
		log.debug("AuthenticationResponse is called for user -> {}", dsUserId);
		headers.setContentType(MediaType.APPLICATION_JSON);

		AuthenticationRequest authenticationRequest = new AuthenticationRequest();
		authenticationRequest.setUser(dsUserId);
		authenticationRequest.setScopes(dsScopes);
		AuthenticationResponse authenticationResponse = authenticationClient.requestJWTUserToken(authenticationRequest)
				.getBody();
		log.debug("OAuth Token is used {} for user -> {}", authenticationResponse.getAccessToken(), dsUserId);
		headers.set(HttpHeaders.AUTHORIZATION,
				authenticationResponse.getTokenType() + " " + authenticationResponse.getAccessToken());

	}
}