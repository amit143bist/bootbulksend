
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
 *         &lt;element name="SendEnvelopeResult" type="{http://www.docusign.net/API/3.0}EnvelopeStatus" minOccurs="0"/>
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
    "sendEnvelopeResult"
})
@XmlRootElement(name = "SendEnvelopeResponse")
public class SendEnvelopeResponse {

    @XmlElement(name = "SendEnvelopeResult")
    protected EnvelopeStatus sendEnvelopeResult;

    /**
     * Gets the value of the sendEnvelopeResult property.
     * 
     * @return
     *     possible object is
     *     {@link EnvelopeStatus }
     *     
     */
    public EnvelopeStatus getSendEnvelopeResult() {
        return sendEnvelopeResult;
    }

    /**
     * Sets the value of the sendEnvelopeResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnvelopeStatus }
     *     
     */
    public void setSendEnvelopeResult(EnvelopeStatus value) {
        this.sendEnvelopeResult = value;
    }

}
