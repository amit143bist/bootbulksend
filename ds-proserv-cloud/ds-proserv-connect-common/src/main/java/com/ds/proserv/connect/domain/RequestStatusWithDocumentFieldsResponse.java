
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
 *         &lt;element name="RequestStatusWithDocumentFieldsResult" type="{http://www.docusign.net/API/3.0}EnvelopeStatus" minOccurs="0"/>
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
    "requestStatusWithDocumentFieldsResult"
})
@XmlRootElement(name = "RequestStatusWithDocumentFieldsResponse")
public class RequestStatusWithDocumentFieldsResponse {

    @XmlElement(name = "RequestStatusWithDocumentFieldsResult")
    protected EnvelopeStatus requestStatusWithDocumentFieldsResult;

    /**
     * Gets the value of the requestStatusWithDocumentFieldsResult property.
     * 
     * @return
     *     possible object is
     *     {@link EnvelopeStatus }
     *     
     */
    public EnvelopeStatus getRequestStatusWithDocumentFieldsResult() {
        return requestStatusWithDocumentFieldsResult;
    }

    /**
     * Sets the value of the requestStatusWithDocumentFieldsResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnvelopeStatus }
     *     
     */
    public void setRequestStatusWithDocumentFieldsResult(EnvelopeStatus value) {
        this.requestStatusWithDocumentFieldsResult = value;
    }

}
