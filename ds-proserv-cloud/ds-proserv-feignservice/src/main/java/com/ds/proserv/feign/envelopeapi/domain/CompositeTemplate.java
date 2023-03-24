
package com.ds.proserv.feign.envelopeapi.domain;

import java.util.ArrayList;
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
    "serverTemplates",
    "inlineTemplates"
})
public class CompositeTemplate {

    @JsonProperty("serverTemplates")
    public List<ServerTemplate> serverTemplates = new ArrayList<>();
    @JsonProperty("inlineTemplates")
    public List<InlineTemplate> inlineTemplates = new ArrayList<>();

}
