
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RequestTemplateWithDocumentFieldsResult" type="{http://www.docusign.net/API/3.0}EnvelopeTemplate" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "requestTemplateWithDocumentFieldsResult"
})
@XmlRootElement(name = "RequestTemplateWithDocumentFieldsResponse")
public class RequestTemplateWithDocumentFieldsResponse {

    @XmlElement(name = "RequestTemplateWithDocumentFieldsResult")
    protected EnvelopeTemplate requestTemplateWithDocumentFieldsResult;

    /**
     * Gets the value of the requestTemplateWithDocumentFieldsResult property.
     * 
     * @return
     *     possible object is
     *     {@link EnvelopeTemplate }
     *     
     */
    public EnvelopeTemplate getRequestTemplateWithDocumentFieldsResult() {
        return requestTemplateWithDocumentFieldsResult;
    }

    /**
     * Sets the value of the requestTemplateWithDocumentFieldsResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnvelopeTemplate }
     *     
     */
    public void setRequestTemplateWithDocumentFieldsResult(EnvelopeTemplate value) {
        this.requestTemplateWithDocumentFieldsResult = value;
    }

}
