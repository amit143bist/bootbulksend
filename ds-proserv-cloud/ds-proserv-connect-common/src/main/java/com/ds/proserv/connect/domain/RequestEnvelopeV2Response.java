
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
 *         &lt;element name="RequestEnvelopeV2Result" type="{http://www.docusign.net/API/3.0}Envelope" minOccurs="0"/>
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
    "requestEnvelopeV2Result"
})
@XmlRootElement(name = "RequestEnvelopeV2Response")
public class RequestEnvelopeV2Response {

    @XmlElement(name = "RequestEnvelopeV2Result")
    protected Envelope requestEnvelopeV2Result;

    /**
     * Gets the value of the requestEnvelopeV2Result property.
     * 
     * @return
     *     possible object is
     *     {@link Envelope }
     *     
     */
    public Envelope getRequestEnvelopeV2Result() {
        return requestEnvelopeV2Result;
    }

    /**
     * Sets the value of the requestEnvelopeV2Result property.
     * 
     * @param value
     *     allowed object is
     *     {@link Envelope }
     *     
     */
    public void setRequestEnvelopeV2Result(Envelope value) {
        this.requestEnvelopeV2Result = value;
    }

}
