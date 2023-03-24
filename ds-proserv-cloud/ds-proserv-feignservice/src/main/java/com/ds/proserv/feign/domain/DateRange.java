package com.ds.proserv.feign.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateRange {

	private LocalDateTime startDateTime;
	private LocalDateTime endDateTime;
}