
package com.ds.proserv.feign.shell.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "numberRecordsRead",
    "numberRecordUpdate",
    "status",
    "nmberRecordsNoFound"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateApplicationResponse {

    @JsonProperty("numberRecordsRead")
    public Integer numberRecordsRead;
    @JsonProperty("numberRecordUpdate")
    public Integer numberRecordUpdate;
        @JsonProperty("status")
        public String status;
    @JsonProperty("nmberRecordsNoFound")
    public Integer nmberRecordsNoFound;

}
