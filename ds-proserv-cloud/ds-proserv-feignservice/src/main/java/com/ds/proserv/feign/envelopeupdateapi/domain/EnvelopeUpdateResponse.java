package com.ds.proserv.feign.envelopeupdateapi.domain;

import java.util.List;
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
        "recipientUpdateResults"
})
public class EnvelopeUpdateResponse {

    @JsonProperty("envelopeId")
    public String envelopeId;
    @JsonProperty("recipientUpdateResults")
    public List<RecipientUpdateResult> recipientUpdateResults = null;



}
