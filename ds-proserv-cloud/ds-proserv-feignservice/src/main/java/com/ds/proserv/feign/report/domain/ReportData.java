package com.ds.proserv.feign.report.domain;

import com.ds.proserv.feign.domain.IDocuSignInformation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportData implements IDocuSignInformation{

	private String reportColumnName;
	private Object reportColumnValue;
}