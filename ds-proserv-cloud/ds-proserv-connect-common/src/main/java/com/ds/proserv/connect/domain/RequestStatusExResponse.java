
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
 *         &lt;element name="RequestStatusExResult" type="{http://www.docusign.net/API/3.0}EnvelopeStatus" minOccurs="0"/>
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
    "requestStatusExResult"
})
@XmlRootElement(name = "RequestStatusExResponse")
public class RequestStatusExResponse {

    @XmlElement(name = "RequestStatusExResult")
    protected EnvelopeStatus requestStatusExResult;

    /**
     * Gets the value of the requestStatusExResult property.
     * 
     * @return
     *     possible object is
     *     {@link EnvelopeStatus }
     *     
     */
    public EnvelopeStatus getRequestStatusExResult() {
        return requestStatusExResult;
    }

    /**
     * Sets the value of the requestStatusExResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnvelopeStatus }
     *     
     */
    public void setRequestStatusExResult(EnvelopeStatus value) {
        this.requestStatusExResult = value;
    }

}
