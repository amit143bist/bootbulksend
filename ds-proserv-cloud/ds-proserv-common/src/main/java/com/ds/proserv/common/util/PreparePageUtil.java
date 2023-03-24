package com.ds.proserv.common.util;

import java.util.ArrayList;
import java.util.List;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.domain.PageSortParam;

public class PreparePageUtil {

	public static PageInformation prepareExceptionPageInformation(int pageNumber, int recordsPerPage) {

		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageNumber(pageNumber);
		pageInformation.setRecordsPerPage(recordsPerPage);

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.RETRYSTATUSES_PARAM_NAME);
		pageQueryParam.setParamValue(null);

		List<PageQueryParam> pageQueryParamList = new ArrayList<PageQueryParam>();
		pageQueryParamList.add(pageQueryParam);

		pageInformation.setPageQueryParams(pageQueryParamList);

		PageSortParam pageSortParam = new PageSortParam();
		pageSortParam.setFieldName("exceptionDateTime");
		pageSortParam.setSortDirection("asc");

		List<PageSortParam> pageSortParamList = new ArrayList<PageSortParam>();
		pageSortParamList.add(pageSortParam);

		pageInformation.setPageSortParams(pageSortParamList);

		return pageInformation;
	}

	public static PageInformation prepareExceptionPageInformation(int pageNumber, String exceptionIds,
			int recordsPerPage) {

		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageNumber(pageNumber);
		pageInformation.setRecordsPerPage(recordsPerPage);

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.EXCEPTIONIDS_PARAM_NAME);
		pageQueryParam.setParamValue(exceptionIds);

		List<PageQueryParam> pageQueryParamList = new ArrayList<PageQueryParam>();
		pageQueryParamList.add(pageQueryParam);

		pageInformation.setPageQueryParams(pageQueryParamList);

		return pageInformation;
	}

	public static PageInformation prepareExceptionPageInformation(String retryStatus, String processId,
			String exceptionIds) {

		PageInformation pageInformation = new PageInformation();

		List<PageQueryParam> pageQueryParamList = new ArrayList<PageQueryParam>();

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.RETRYSTATUSES_PARAM_NAME);
		pageQueryParam.setParamValue(retryStatus);

		pageQueryParamList.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.PROCESSID_PARAM_NAME);
		pageQueryParam.setParamValue(processId);

		pageQueryParamList.add(pageQueryParam);

		pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.EXCEPTIONIDS_PARAM_NAME);
		pageQueryParam.setParamValue(exceptionIds);

		pageQueryParamList.add(pageQueryParam);

		pageInformation.setPageQueryParams(pageQueryParamList);

		return pageInformation;
	}
}