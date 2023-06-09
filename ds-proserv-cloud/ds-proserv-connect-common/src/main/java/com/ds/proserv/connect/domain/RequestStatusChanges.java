
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
 *         &lt;element name="EnvelopeStatusChangeFilter" type="{http://www.docusign.net/API/3.0}EnvelopeStatusChangeFilter" minOccurs="0"/>
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
    "envelopeStatusChangeFilter"
})
@XmlRootElement(name = "RequestStatusChanges")
public class RequestStatusChanges {

    @XmlElement(name = "EnvelopeStatusChangeFilter")
    protected EnvelopeStatusChangeFilter envelopeStatusChangeFilter;

    /**
     * Gets the value of the envelopeStatusChangeFilter property.
     * 
     * @return
     *     possible object is
     *     {@link EnvelopeStatusChangeFilter }
     *     
     */
    public EnvelopeStatusChangeFilter getEnvelopeStatusChangeFilter() {
        return envelopeStatusChangeFilter;
    }

    /**
     * Sets the value of the envelopeStatusChangeFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnvelopeStatusChangeFilter }
     *     
     */
    public void setEnvelopeStatusChangeFilter(EnvelopeStatusChangeFilter value) {
        this.envelopeStatusChangeFilter = value;
    }

}
