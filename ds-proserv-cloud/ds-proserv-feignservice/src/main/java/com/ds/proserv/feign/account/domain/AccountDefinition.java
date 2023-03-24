package com.ds.proserv.feign.account.domain;

import com.ds.proserv.feign.domain.IDocuSignInformation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "account_id", "is_default", "account_name", "base_uri", "organization" })
public class AccountDefinition  implements IDocuSignInformation {

    @JsonProperty("account_id")
    private String accountId;
    @JsonProperty("is_default")
    private Boolean isDefault;
    @JsonProperty("account_name")
    private String accountName;
    @JsonProperty("base_uri")
    private String baseUri;
    @JsonProperty("organization")
    private OrganizationDefinition organization;

}
