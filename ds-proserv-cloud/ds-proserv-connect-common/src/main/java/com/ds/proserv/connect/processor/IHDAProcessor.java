package com.ds.proserv.connect.processor;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.ConnectProcessorType;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.feign.envelopedata.domain.DSCustomFieldDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDefinition;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class IHDAProcessor implements IConnectProcessor {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Override
	public boolean canHandleRequest(List<String> allowedProcessorTypes) {

		return allowedProcessorTypes.contains(ConnectProcessorType.IHDA.toString());
	}

	@Override
	public void processConnectData(String queueName, List<DSEnvelopeDefinition> toProcessDSEnvelopeDefinitionList) {

		List<String> connectExclusionFieldNames = connectExclusionFieldNames();
		Iterator<DSEnvelopeDefinition> dsEnvelopeDefinitionIterator = toProcessDSEnvelopeDefinitionList.iterator();
		while (dsEnvelopeDefinitionIterator.hasNext()) {

			DSEnvelopeDefinition dsEnvelopeDefinition = dsEnvelopeDefinitionIterator.next();

			List<DSCustomFieldDefinition> dsCustomFieldDefinitions = dsEnvelopeDefinition.getDsCustomFieldDefinitions();

			for (DSCustomFieldDefinition dsCustomFieldDefinition : dsCustomFieldDefinitions) {

				if (null != connectExclusionFieldNames
						&& connectExclusionFieldNames.contains(dsCustomFieldDefinition.getFieldName().toUpperCase())
						&& !StringUtils.isEmpty(dsCustomFieldDefinition.getFieldValue())) {

					// Should not migrate Connect messages for CLM generated envelopes
					log.info("Removing Envelope -> {} from Migration", dsEnvelopeDefinition.getEnvelopeId());
					dsEnvelopeDefinitionIterator.remove();
					break;
				}
			}
		}
	}

	private List<String> connectExclusionFieldNames() {

		String connectExclusionFieldNamesAsStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_EXCLUSION_FIELDNAMES, PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(connectExclusionFieldNamesAsStr)) {

			throw new InvalidInputException(
					PropertyCacheConstants.CONNECT_EXCLUSION_FIELDNAMES + " is missing or empty");
		} else {

			List<String> connectExclusionFieldNames = Stream
					.of(connectExclusionFieldNamesAsStr.split(AppConstants.COMMA_DELIMITER)).map(String::trim)
					.map(String::toUpperCase).collect(Collectors.toList());

			return connectExclusionFieldNames;
		}
	}

}