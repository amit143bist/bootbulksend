
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
 *         &lt;element name="EnvelopeAuditEventsV2Result" type="{http://www.docusign.net/API/3.0}ArrayOfEnvelopeAuditEvent" minOccurs="0"/>
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
    "envelopeAuditEventsV2Result"
})
@XmlRootElement(name = "EnvelopeAuditEventsV2Response")
public class EnvelopeAuditEventsV2Response {

    @XmlElement(name = "EnvelopeAuditEventsV2Result")
    protected ArrayOfEnvelopeAuditEvent envelopeAuditEventsV2Result;

    /**
     * Gets the value of the envelopeAuditEventsV2Result property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfEnvelopeAuditEvent }
     *     
     */
    public ArrayOfEnvelopeAuditEvent getEnvelopeAuditEventsV2Result() {
        return envelopeAuditEventsV2Result;
    }

    /**
     * Sets the value of the envelopeAuditEventsV2Result property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfEnvelopeAuditEvent }
     *     
     */
    public void setEnvelopeAuditEventsV2Result(ArrayOfEnvelopeAuditEvent value) {
        this.envelopeAuditEventsV2Result = value;
    }

}
