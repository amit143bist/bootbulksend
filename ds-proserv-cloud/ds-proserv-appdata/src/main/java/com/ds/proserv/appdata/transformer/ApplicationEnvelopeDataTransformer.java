package com.ds.proserv.appdata.transformer;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ds.proserv.appdata.domain.ApplicationEnvelopeSPRequest;
import com.ds.proserv.appdata.model.ApplicationEnvelopeData;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.feign.appdata.domain.ApplicationEnvelopeDefinition;

@Component
public class ApplicationEnvelopeDataTransformer {

	public ApplicationEnvelopeData transformToApplicationEnvelopeData(
			ApplicationEnvelopeDefinition applicationEnvelopeDefinition) {

		ApplicationEnvelopeData applicationEnvelopeData = new ApplicationEnvelopeData();

		applicationEnvelopeData.setApplicationId(applicationEnvelopeDefinition.getApplicationId());
		applicationEnvelopeData.setApplicationType(applicationEnvelopeDefinition.getApplicationType());
		applicationEnvelopeData.setEnvelopeId(applicationEnvelopeDefinition.getEnvelopeId());

		if (!StringUtils.isEmpty(applicationEnvelopeDefinition.getEnvelopeSentTimestamp())) {

			applicationEnvelopeData.setEnvelopeSentTimestamp(
					LocalDateTime.parse(applicationEnvelopeDefinition.getEnvelopeSentTimestamp()));
		}

		applicationEnvelopeData.setFailureReason(applicationEnvelopeDefinition.getFailureReason());

		if (!StringUtils.isEmpty(applicationEnvelopeDefinition.getFailureTimestamp())) {

			applicationEnvelopeData
					.setFailureTimestamp(LocalDateTime.parse(applicationEnvelopeDefinition.getFailureTimestamp()));
		}

		applicationEnvelopeData.setRecipientEmails(
				String.join(AppConstants.COMMA_DELIMITER, applicationEnvelopeDefinition.getRecipientEmails()));
		applicationEnvelopeData.setCommunityPartnerCode(applicationEnvelopeDefinition.getCommunityPartnerCode());

		return applicationEnvelopeData;
	}

	public ApplicationEnvelopeDefinition transformToApplicationEnvelopeDefinition(
			ApplicationEnvelopeData applicationEnvelopeData) {

		ApplicationEnvelopeDefinition applicationEnvelopeDefinition = new ApplicationEnvelopeDefinition();

		applicationEnvelopeDefinition.setId(applicationEnvelopeData.getId());
		applicationEnvelopeDefinition.setApplicationId(applicationEnvelopeData.getApplicationId());
		applicationEnvelopeDefinition.setApplicationType(applicationEnvelopeData.getApplicationType());
		applicationEnvelopeDefinition.setEnvelopeId(applicationEnvelopeData.getEnvelopeId());

		if (null != applicationEnvelopeData.getEnvelopeSentTimestamp()) {

			applicationEnvelopeDefinition
					.setEnvelopeSentTimestamp(applicationEnvelopeData.getEnvelopeSentTimestamp().toString());
		}

		applicationEnvelopeDefinition.setFailureReason(applicationEnvelopeData.getFailureReason());

		if (null != applicationEnvelopeData.getFailureTimestamp()) {

			applicationEnvelopeDefinition.setFailureTimestamp(applicationEnvelopeData.getFailureTimestamp().toString());
		}

		if (!StringUtils.isEmpty(applicationEnvelopeData.getRecipientEmails())) {

			applicationEnvelopeDefinition
					.setRecipientEmails(DSUtil.getFieldsAsList(applicationEnvelopeData.getRecipientEmails()));
		}
		applicationEnvelopeDefinition.setCommunityPartnerCode(applicationEnvelopeData.getCommunityPartnerCode());

		return applicationEnvelopeDefinition;
	}

	public ApplicationEnvelopeSPRequest transformToApplicationEnvelopeSPRequest(
			ApplicationEnvelopeDefinition applicationEnvelopeDefinition) {

		ApplicationEnvelopeSPRequest applicationEnvelopeSPRequest = new ApplicationEnvelopeSPRequest();

		applicationEnvelopeSPRequest.setApplicationId(applicationEnvelopeDefinition.getApplicationId());
		applicationEnvelopeSPRequest.setApplicationType(applicationEnvelopeDefinition.getApplicationType());
		applicationEnvelopeSPRequest.setEnvelopeId(applicationEnvelopeDefinition.getEnvelopeId());

		if (null != applicationEnvelopeDefinition.getEnvelopeSentTimestamp()) {

			applicationEnvelopeSPRequest
					.setEnvelopeSentTimestamp(applicationEnvelopeDefinition.getEnvelopeSentTimestamp());
		}

		applicationEnvelopeSPRequest.setFailureReason(applicationEnvelopeDefinition.getFailureReason());

		if (null != applicationEnvelopeDefinition.getFailureTimestamp()) {

			applicationEnvelopeSPRequest.setFailureTimestamp(applicationEnvelopeDefinition.getFailureTimestamp());
		}

		if (null != applicationEnvelopeDefinition.getRecipientEmails()
				&& !applicationEnvelopeDefinition.getRecipientEmails().isEmpty()) {

			applicationEnvelopeSPRequest.setRecipientEmails(
					String.join(AppConstants.COMMA_DELIMITER, applicationEnvelopeDefinition.getRecipientEmails()));
		}
		applicationEnvelopeSPRequest.setCommunityPartnerCode(applicationEnvelopeDefinition.getCommunityPartnerCode());

		return applicationEnvelopeSPRequest;
	}
}