package com.ds.proserv.report.auth.service;

import org.springframework.stereotype.Service;

import com.ds.proserv.common.constant.APICategoryType;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;

@Service
public class NoAuthService implements IAuthService {

	@Override
	public boolean canHandleRequest(APICategoryType apiCategoryType) {

		return APICategoryType.NOAUTHAPI == apiCategoryType;
	}

	@Override
	public AuthenticationResponse requestOAuthToken(AuthenticationRequest authenticationRequest) {

		return null;
	}

	@Override
	public String requestBaseUrl(AuthenticationRequest authenticationRequest) {

		return authenticationRequest.getBaseUrl();
	}

}