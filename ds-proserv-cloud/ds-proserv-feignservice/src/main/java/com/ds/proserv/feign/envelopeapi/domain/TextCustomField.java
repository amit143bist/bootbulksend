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
        "name",
        "required",
        "show",
        "value"
})
public class TextCustomField {

    @JsonProperty("name")
    public String name;
    @JsonProperty("required")
    public String required;
    @JsonProperty("show")
    public String show;
    @JsonProperty("value")
    public String value;

}