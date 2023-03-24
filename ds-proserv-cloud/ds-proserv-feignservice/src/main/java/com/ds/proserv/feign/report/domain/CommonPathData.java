package com.ds.proserv.feign.report.domain;

import com.ds.proserv.feign.domain.IDocuSignInformation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonPathData implements IDocuSignInformation {

	private String outerPath;
	private String columnDataType;
	private String columnPath;
	private String columnDataPattern;
	private String timeZone;
	private Integer arrayIndex;
	private String mapKey;
	private String outputDataPattern;
	private Integer startIndex;
	private Integer endIndex;
	private String outputDelimiter;
}