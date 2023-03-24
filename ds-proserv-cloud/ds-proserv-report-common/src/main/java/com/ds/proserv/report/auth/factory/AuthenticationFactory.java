package com.ds.proserv.report.auth.factory;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ds.proserv.common.constant.APICategoryType;
import com.ds.proserv.report.auth.service.IAuthService;

@Component
public class AuthenticationFactory {

	@Autowired
	private List<IAuthService> authServices;

	public Optional<IAuthService> fetchAuthServiceByAPICategoryType(APICategoryType apiCategoryType) {

		return authServices.stream().filter(service -> service.canHandleRequest(apiCategoryType)).findFirst();

	}
}