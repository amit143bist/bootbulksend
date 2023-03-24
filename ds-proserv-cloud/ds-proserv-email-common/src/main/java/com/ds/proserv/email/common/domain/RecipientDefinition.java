package com.ds.proserv.email.common.domain;

import java.util.List;

import javax.mail.Message.RecipientType;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecipientDefinition {

	private RecipientType recipientType;
	private List<String> recipientEmailAddresses;
}