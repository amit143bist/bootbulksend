package com.ds.proserv.report.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ds.proserv.common.constant.APICategoryType;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.report.dsapi.service.DSAccountService;

@Service
public class ESignOAuthService extends AbstractDSAuthService implements IAuthService {

	@Autowired
	private DSAccountService dsAccountService;

	@Override
	public boolean canHandleRequest(APICategoryType apiCategoryType) {

		return APICategoryType.ESIGNAPI == apiCategoryType;
	}

	@Override
	public String requestBaseUrl(AuthenticationRequest authenticationRequest) {

		return dsAccountService.getUserInfo(authenticationRequest.getAccountGuid(), authenticationRequest)
				+ AppConstants.FORWARD_SLASH + "restapi" + AppConstants.FORWARD_SLASH
				+ dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
						PropertyCacheConstants.REPORT_ESIGN_API_VERSION,
						PropertyCacheConstants.DS_API_REFERENCE_NAME);
	}

}