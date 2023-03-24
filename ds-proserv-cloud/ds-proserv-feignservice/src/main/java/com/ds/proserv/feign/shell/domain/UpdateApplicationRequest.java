
package com.ds.proserv.feign.shell.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "csvFile",
    "drawReferenceId"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateApplicationRequest {

    @JsonProperty("csvFile")
    public String csvFile;
    @JsonProperty("drawReferenceId")
    public String drawReferenceId;

}
