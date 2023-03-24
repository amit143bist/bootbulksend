
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
    "tabLabel",
    "selected"
})
public class CheckboxTab {

    @JsonProperty("tabLabel")
    public String tabLabel;
    @JsonProperty("selected")
    public Boolean selected;

}
