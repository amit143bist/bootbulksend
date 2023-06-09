
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
 *         &lt;element name="RequestEnvelopeHistoryTokenResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "requestEnvelopeHistoryTokenResult"
})
@XmlRootElement(name = "RequestEnvelopeHistoryTokenResponse")
public class RequestEnvelopeHistoryTokenResponse {

    @XmlElement(name = "RequestEnvelopeHistoryTokenResult")
    protected String requestEnvelopeHistoryTokenResult;

    /**
     * Gets the value of the requestEnvelopeHistoryTokenResult property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestEnvelopeHistoryTokenResult() {
        return requestEnvelopeHistoryTokenResult;
    }

    /**
     * Sets the value of the requestEnvelopeHistoryTokenResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestEnvelopeHistoryTokenResult(String value) {
        this.requestEnvelopeHistoryTokenResult = value;
    }

}
