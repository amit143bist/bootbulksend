package com.ds.proserv.feign.account.domain;

import com.ds.proserv.feign.domain.IDocuSignInformation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "organization_id", "links" })
public class OrganizationDefinition  implements IDocuSignInformation {

    @JsonProperty("organization_id")
    private String organizationId;
    @JsonProperty("links")
    private List<LinkDefinition> links = null;

}