package com.ds.proserv.report.processor;

import java.util.List;
import java.util.Map;

import javax.script.Bindings;

import org.springframework.stereotype.Service;

import com.ds.proserv.feign.report.domain.PathParam;
import com.ds.proserv.feign.util.BindingParamDataUtil;

@Service
public class BindingParamDataProcessor {

	public void populateBindingParams(Map<String, Object> inputParams, List<PathParam> pathParams, Bindings bindings,
			String parentData, String nextUri, String accountId, String batchId) {

		BindingParamDataUtil.populateBindingParams(inputParams, pathParams, bindings, parentData, nextUri, accountId,
				batchId);
	}
}