
package com.ds.proserv.feign.shell.domain;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "envelopeId",
    "recipientUpdateResults"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRecipientResponse {

    @JsonProperty("envelopeId")
    public String envelopeId;
    @JsonProperty("recipientUpdateResults")
    public List<RecipientUpdateResult> recipientUpdateResults = null;

}
