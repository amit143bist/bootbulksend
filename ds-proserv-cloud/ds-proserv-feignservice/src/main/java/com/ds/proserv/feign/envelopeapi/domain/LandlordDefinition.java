
package com.ds.proserv.feign.envelopeapi.domain;

import java.util.List;

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
@JsonPropertyOrder({
        "fulName",
        "email",
        "items"
})
public class LandlordDefinition implements IDocuSignInformation {

    @JsonProperty("fullName")
    private String fullName;
    @JsonProperty("email")
    private String email;
    @JsonProperty("items")
    private List<Item> items = null;


}
