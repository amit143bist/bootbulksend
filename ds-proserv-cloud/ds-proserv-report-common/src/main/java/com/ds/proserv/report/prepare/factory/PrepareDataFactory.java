package com.ds.proserv.report.prepare.factory;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ds.proserv.common.constant.APICategoryType;
import com.ds.proserv.report.prepare.service.IPrepareData;

@Component
public class PrepareDataFactory {

	@Autowired
	private List<IPrepareData> prepareDataServices;

	public Optional<IPrepareData> prepareData(APICategoryType apiCategoryType) {

		return prepareDataServices.stream().filter(service -> service.canHandleRequest(apiCategoryType)).findFirst();

	}
}