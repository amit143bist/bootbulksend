
package com.ds.proserv.feign.envelopeapi.domain;

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
        "dataLabel",
        "dataValue"
})
public class Item implements IDocuSignInformation {

    @JsonProperty("dataLabel")
    private String dataLabel;
    @JsonProperty("dataValue")
    private String dataValue;

}
