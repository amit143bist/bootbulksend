package com.ds.proserv.feign.account.domain;

import com.ds.proserv.feign.domain.IDocuSignInformation;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"sub", "name", "given_name", "family_name", "created", "email", "accounts"})
public class LoginUserInfoDefinition implements IDocuSignInformation {

    @JsonProperty("sub")
    private String sub;
    @JsonProperty("name")
    private String name;
    @JsonProperty("given_name")
    private String givenName;
    @JsonProperty("family_name")
    private String familyName;
    @JsonProperty("created")
    private String created;
    @JsonProperty("email")
    private String email;
    @JsonProperty("accounts")
    private List<AccountDefinition> accounts = null;

}
