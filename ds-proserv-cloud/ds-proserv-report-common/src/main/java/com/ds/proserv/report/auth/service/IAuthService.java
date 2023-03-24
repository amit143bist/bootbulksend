package com.ds.proserv.report.auth.service;

import com.ds.proserv.common.constant.APICategoryType;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;

public interface IAuthService {

	boolean canHandleRequest(APICategoryType apiCategoryType);

	AuthenticationResponse requestOAuthToken(AuthenticationRequest authenticationRequest);

	String requestBaseUrl(AuthenticationRequest authenticationRequest);
}