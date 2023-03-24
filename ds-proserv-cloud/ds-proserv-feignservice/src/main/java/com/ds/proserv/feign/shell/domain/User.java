
package com.ds.proserv.feign.shell.domain;

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
    "userName",
    "userId",
    "email",
    "userType",
    "userStatus",
    "uri"
})
public class User {

    @JsonProperty("userName")
    public String userName;
    @JsonProperty("userId")
    public String userId;
    @JsonProperty("email")
    public String email;
    @JsonProperty("userType")
    public String userType;
    @JsonProperty("userStatus")
    public String userStatus;
    @JsonProperty("uri")
    public String uri;

}
