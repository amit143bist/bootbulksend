package com.ds.proserv.appdata.transformer;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ds.proserv.appdata.model.CustomEnvelopeData;
import com.ds.proserv.appdata.projection.CustomEnvelopeDataBucketNameProjection;
import com.ds.proserv.appdata.projection.CustomEnvelopeDataCountDateProjection;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataCountBucketNameResponse;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataCountDateResponse;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataDefinition;

@Component
public class CustomEnvelopeDataTransformer {

	public CustomEnvelopeData transformToCustomEnvelopeData(CustomEnvelopeDataDefinition customEnvelopeDataDefinition) {

		CustomEnvelopeData customEnvelopeData = new CustomEnvelopeData();
		customEnvelopeData.setEnvelopeId(customEnvelopeDataDefinition.getEnvelopeId());

		if (!StringUtils.isEmpty(customEnvelopeDataDefinition.getEnvDate())) {

			customEnvelopeData.setEnvDate(LocalDate.parse(customEnvelopeDataDefinition.getEnvDate()));
		}
		if (!StringUtils.isEmpty(customEnvelopeDataDefinition.getEnvTimeStamp())) {

			customEnvelopeData.setEnvTimeStamp(LocalDateTime.parse(customEnvelopeDataDefinition.getEnvTimeStamp()));
		}

		customEnvelopeData.setSenderIdentifier(customEnvelopeDataDefinition.getSenderIdentifier());
		customEnvelopeData.setDocDownloadStatusFlag(customEnvelopeDataDefinition.getDocDownloadStatusFlag());

		if (!StringUtils.isEmpty(customEnvelopeDataDefinition.getDocDownloadTimeStamp())) {

			customEnvelopeData.setDocDownloadTimeStamp(
					LocalDateTime.parse(customEnvelopeDataDefinition.getDocDownloadTimeStamp()));
		}

		if (!StringUtils.isEmpty(customEnvelopeDataDefinition.getEnvProcessEndDateTime())) {

			customEnvelopeData.setEnvProcessEndDateTime(
					LocalDateTime.parse(customEnvelopeDataDefinition.getEnvProcessEndDateTime()));
		}

		if (!StringUtils.isEmpty(customEnvelopeDataDefinition.getEnvProcessStartDateTime())) {

			customEnvelopeData.setEnvProcessStartDateTime(
					LocalDateTime.parse(customEnvelopeDataDefinition.getEnvProcessStartDateTime()));
		}

		customEnvelopeData.setEnvProcessStatusFlag(customEnvelopeDataDefinition.getEnvProcessStatusFlag());
		customEnvelopeData.setDownloadBucketName(customEnvelopeDataDefinition.getDownloadBucketName());

		return customEnvelopeData;
	}

	public CustomEnvelopeData transformToCustomEnvelopeDataAsUpdate(
			CustomEnvelopeDataDefinition customEnvelopeDataDefinition, CustomEnvelopeData customEnvelopeData) {

		if (!StringUtils.isEmpty(customEnvelopeDataDefinition.getDocDownloadStatusFlag())) {

			customEnvelopeData.setDocDownloadStatusFlag(customEnvelopeDataDefinition.getDocDownloadStatusFlag());
		}

		if (!StringUtils.isEmpty(customEnvelopeDataDefinition.getDocDownloadTimeStamp())) {

			customEnvelopeData.setDocDownloadTimeStamp(
					LocalDateTime.parse(customEnvelopeDataDefinition.getDocDownloadTimeStamp()));
		}

		if (!StringUtils.isEmpty(customEnvelopeDataDefinition.getEnvProcessStatusFlag())) {

			customEnvelopeData.setEnvProcessStatusFlag(customEnvelopeDataDefinition.getEnvProcessStatusFlag());
		}

		if (!StringUtils.isEmpty(customEnvelopeDataDefinition.getEnvProcessStartDateTime())) {

			customEnvelopeData.setEnvProcessStartDateTime(
					LocalDateTime.parse(customEnvelopeDataDefinition.getEnvProcessStartDateTime()));
		}

		if (!StringUtils.isEmpty(customEnvelopeDataDefinition.getEnvProcessEndDateTime())) {

			customEnvelopeData.setEnvProcessEndDateTime(
					LocalDateTime.parse(customEnvelopeDataDefinition.getEnvProcessEndDateTime()));
		}

		if (!StringUtils.isEmpty(customEnvelopeDataDefinition.getDownloadBucketName())) {

			customEnvelopeData.setDownloadBucketName(customEnvelopeDataDefinition.getDownloadBucketName());
		}

		return customEnvelopeData;
	}

	public CustomEnvelopeDataDefinition transformToCustomEnvelopeDataDefinition(CustomEnvelopeData customEnvelopeData) {

		CustomEnvelopeDataDefinition customEnvelopeDataDefinition = new CustomEnvelopeDataDefinition();

		customEnvelopeDataDefinition.setEnvelopeId(customEnvelopeData.getEnvelopeId());

		if (null != customEnvelopeData) {

			customEnvelopeDataDefinition.setEnvDate(customEnvelopeData.toString());
		}

		if (null != customEnvelopeData.getEnvTimeStamp()) {

			customEnvelopeDataDefinition.setEnvTimeStamp(customEnvelopeData.getEnvTimeStamp().toString());
		}

		customEnvelopeDataDefinition.setSenderIdentifier(customEnvelopeData.getSenderIdentifier());

		customEnvelopeDataDefinition.setDocDownloadStatusFlag(customEnvelopeData.getDocDownloadStatusFlag());

		if (null != customEnvelopeData.getDocDownloadTimeStamp()) {

			customEnvelopeDataDefinition
					.setDocDownloadTimeStamp(customEnvelopeData.getDocDownloadTimeStamp().toString());
		}

		customEnvelopeDataDefinition.setEnvProcessStatusFlag(customEnvelopeData.getEnvProcessStatusFlag());

		if (null != customEnvelopeData.getEnvProcessStartDateTime()) {

			customEnvelopeDataDefinition
					.setEnvProcessStartDateTime(customEnvelopeData.getEnvProcessStartDateTime().toString());
		}

		if (null != customEnvelopeData.getEnvProcessEndDateTime()) {

			customEnvelopeDataDefinition
					.setEnvProcessEndDateTime(customEnvelopeData.getEnvProcessEndDateTime().toString());
		}

		customEnvelopeDataDefinition.setDownloadBucketName(customEnvelopeData.getDownloadBucketName());

		return customEnvelopeDataDefinition;
	}

	public CustomEnvelopeDataCountDateResponse transformToCustomEnvelopeDataCountDateResponse(
			CustomEnvelopeDataCountDateProjection customEnvelopeDataCountDateProjection) {

		CustomEnvelopeDataCountDateResponse customEnvelopeDataCountDateResponse = new CustomEnvelopeDataCountDateResponse();

		customEnvelopeDataCountDateResponse.setCount(customEnvelopeDataCountDateProjection.getCount());
		customEnvelopeDataCountDateResponse.setEnvDate(customEnvelopeDataCountDateProjection.getDate().toString());

		return customEnvelopeDataCountDateResponse;
	}

	public CustomEnvelopeDataCountBucketNameResponse transformToCustomEnvelopeDataCountBucketNameResponse(
			CustomEnvelopeDataBucketNameProjection customEnvelopeDataBucketNameProjection) {

		CustomEnvelopeDataCountBucketNameResponse customEnvelopeDataCountBucketNameResponse = new CustomEnvelopeDataCountBucketNameResponse();

		customEnvelopeDataCountBucketNameResponse.setCount(customEnvelopeDataBucketNameProjection.getCount());
		customEnvelopeDataCountBucketNameResponse
				.setBucketName(customEnvelopeDataBucketNameProjection.getDownloadBucketName());

		return customEnvelopeDataCountBucketNameResponse;
	}
}