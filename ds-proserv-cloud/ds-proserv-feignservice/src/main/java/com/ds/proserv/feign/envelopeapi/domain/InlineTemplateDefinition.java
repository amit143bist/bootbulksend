
package com.ds.proserv.feign.envelopeapi.domain;

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
    "sequence",
    "recipients"
})
public class InlineTemplateDefinition {

    @JsonProperty("sequence")
    public Integer sequence;
    @JsonProperty("recipients")
    public Recipients recipients;

}
