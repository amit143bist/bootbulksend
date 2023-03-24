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
        "envelopeId",
        "uri",
        "statusDateTime",
        "status"
})
public class CreateEnvelopeResponse implements IDocuSignInformation {

    @JsonProperty("envelopeId")
    public String envelopeId;
    @JsonProperty("uri")
    public String uri;
    @JsonProperty("statusDateTime")
    public String statusDateTime;
    @JsonProperty("status")
    public String status;

}
