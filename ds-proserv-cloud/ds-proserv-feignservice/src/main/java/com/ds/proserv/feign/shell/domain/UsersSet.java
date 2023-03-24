
package com.ds.proserv.feign.shell.domain;

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
    "users",
    "resultSetSize",
    "totalSetSize",
    "startPosition",
    "endPosition",
    "nextUri"
})
public class UsersSet {

    @JsonProperty("users")
    public List<User> users = null;
    @JsonProperty("resultSetSize")
    public String resultSetSize;
    @JsonProperty("totalSetSize")
    public String totalSetSize;
    @JsonProperty("startPosition")
    public String startPosition;
    @JsonProperty("endPosition")
    public String endPosition;
    @JsonProperty("nextUri")
    public String nextUri;

}
