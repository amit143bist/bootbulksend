package com.ds.proserv.feign.envelopeupdateapi.domain;

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
        "recipientId",
        "recipientIdGuid"
})
public class RecipientUpdateResult {

    @JsonProperty("recipientId")
    public String recipientId;
    @JsonProperty("recipientIdGuid")
    public String recipientIdGuid;

}
