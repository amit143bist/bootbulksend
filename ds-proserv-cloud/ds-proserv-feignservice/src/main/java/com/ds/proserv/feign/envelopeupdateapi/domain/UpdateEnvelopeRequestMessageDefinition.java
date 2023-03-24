package com.ds.proserv.feign.envelopeupdateapi.domain;

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
@JsonPropertyOrder({ "envelopeId", "payload" })
public class UpdateEnvelopeRequestMessageDefinition implements IDocuSignInformation {

    @JsonProperty("envelopeId")
    private String envelopeId;
    @JsonProperty("payload")
    private String payload;
}
