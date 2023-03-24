package com.ds.proserv.report.auth.service;

import org.springframework.stereotype.Service;

import com.ds.proserv.common.constant.APICategoryType;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;

@Service
public class OrgAdminOAuthService extends AbstractDSAuthService implements IAuthService {

	@Override
	public boolean canHandleRequest(APICategoryType apiCategoryType) {

		return APICategoryType.ORGADMINAPI == apiCategoryType;
	}

	@Override
	public String requestBaseUrl(AuthenticationRequest authenticationRequest) {

		return dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.REPORT_ORG_ADMIN_API_BASEURL, PropertyCacheConstants.DS_API_REFERENCE_NAME);
	}

}