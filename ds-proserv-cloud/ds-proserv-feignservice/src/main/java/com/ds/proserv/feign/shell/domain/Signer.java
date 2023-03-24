
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
    "tabs",
    "signInEachLocation",
    "creationReason",
    "isBulkRecipient",
    "name",
    "email",
    "recipientId",
    "recipientIdGuid",
    "requireIdLookup",
    "userId",
    "clientUserId",
    "routingOrder",
    "roleName",
    "status",
    "deliveredDateTime",
    "deliveryMethod",
    "templateLocked",
    "templateRequired",
    "totalTabCount"
})
public class Signer {

    @JsonProperty("tabs")
    public Tabs tabs;
    @JsonProperty("signInEachLocation")
    public String signInEachLocation;
    @JsonProperty("creationReason")
    public String creationReason;
    @JsonProperty("isBulkRecipient")
    public String isBulkRecipient;
    @JsonProperty("name")
    public String name;
    @JsonProperty("email")
    public String email;
    @JsonProperty("recipientId")
    public String recipientId;
    @JsonProperty("recipientIdGuid")
    public String recipientIdGuid;
    @JsonProperty("requireIdLookup")
    public String requireIdLookup;
    @JsonProperty("userId")
    public String userId;
    @JsonProperty("clientUserId")
    public String clientUserId;
    @JsonProperty("routingOrder")
    public String routingOrder;
    @JsonProperty("roleName")
    public String roleName;
    @JsonProperty("status")
    public String status;
    @JsonProperty("deliveredDateTime")
    public String deliveredDateTime;
    @JsonProperty("deliveryMethod")
    public String deliveryMethod;
    @JsonProperty("templateLocked")
    public String templateLocked;
    @JsonProperty("templateRequired")
    public String templateRequired;
    @JsonProperty("totalTabCount")
    public String totalTabCount;

}
