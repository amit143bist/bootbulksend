package com.ds.proserv.report.auth.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.ds.proserv.common.constant.APICategoryType;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;

@Service
public class RoomsOAuthService extends AbstractDSAuthService implements IAuthService {

	@Override
	public boolean canHandleRequest(APICategoryType apiCategoryType) {

		return APICategoryType.ROOMSAPI == apiCategoryType;
	}

	@Override
	public String requestBaseUrl(AuthenticationRequest authenticationRequest) {

		if (StringUtils.isEmpty(authenticationRequest.getAccountGuid())) {

			return dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.REPORT_ROOMS_API_BASEURL, PropertyCacheConstants.DS_API_REFERENCE_NAME)
					+ AppConstants.FORWARD_SLASH
					+ dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
							PropertyCacheConstants.REPORT_ROOMS_API_VERSION,
							PropertyCacheConstants.DS_API_REFERENCE_NAME);
		} else {

			return dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.REPORT_ROOMS_API_BASEURL, PropertyCacheConstants.DS_API_REFERENCE_NAME)
					+ AppConstants.FORWARD_SLASH
					+ dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
							PropertyCacheConstants.REPORT_ROOMS_API_VERSION,
							PropertyCacheConstants.DS_API_REFERENCE_NAME)
					+ AppConstants.FORWARD_SLASH + authenticationRequest.getAccountGuid();
		}
	}

}