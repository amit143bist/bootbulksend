
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
    "height",
    "validationPattern",
    "validationMessage",
    "shared",
    "requireInitialOnSharedChange",
    "requireAll",
    "value",
    "originalValue",
    "width",
    "required",
    "locked",
    "concealValueOnDocument",
    "disableAutoSize",
    "maxLength",
    "tabLabel",
    "font",
    "bold",
    "italic",
    "underline",
    "fontColor",
    "fontSize",
    "documentId",
    "recipientId",
    "pageNumber",
    "xPosition",
    "yPosition",
    "tabId",
    "templateLocked",
    "templateRequired",
    "name",
    "conditionalParentLabel",
    "conditionalParentValue"
})
public class TextTab {

    @JsonProperty("height")
    public Integer height;
    @JsonProperty("validationPattern")
    public String validationPattern;
    @JsonProperty("validationMessage")
    public String validationMessage;
    @JsonProperty("shared")
    public String shared;
    @JsonProperty("requireInitialOnSharedChange")
    public String requireInitialOnSharedChange;
    @JsonProperty("requireAll")
    public String requireAll;
    @JsonProperty("value")
    public String value;
    @JsonProperty("originalValue")
    public String originalValue;
    @JsonProperty("width")
    public Integer width;
    @JsonProperty("required")
    public String required;
    @JsonProperty("locked")
    public String locked;
    @JsonProperty("concealValueOnDocument")
    public String concealValueOnDocument;
    @JsonProperty("disableAutoSize")
    public String disableAutoSize;
    @JsonProperty("maxLength")
    public Integer maxLength;
    @JsonProperty("tabLabel")
    public String tabLabel;
    @JsonProperty("font")
    public String font;
    @JsonProperty("bold")
    public String bold;
    @JsonProperty("italic")
    public String italic;
    @JsonProperty("underline")
    public String underline;
    @JsonProperty("fontColor")
    public String fontColor;
    @JsonProperty("fontSize")
    public String fontSize;
    @JsonProperty("documentId")
    public String documentId;
    @JsonProperty("recipientId")
    public String recipientId;
    @JsonProperty("pageNumber")
    public String pageNumber;
    @JsonProperty("xPosition")
    public String xPosition;
    @JsonProperty("yPosition")
    public String yPosition;
    @JsonProperty("tabId")
    public String tabId;
    @JsonProperty("templateLocked")
    public String templateLocked;
    @JsonProperty("templateRequired")
    public String templateRequired;
    @JsonProperty("name")
    public String name;
    @JsonProperty("conditionalParentLabel")
    public String conditionalParentLabel;
    @JsonProperty("conditionalParentValue")
    public String conditionalParentValue;

}
