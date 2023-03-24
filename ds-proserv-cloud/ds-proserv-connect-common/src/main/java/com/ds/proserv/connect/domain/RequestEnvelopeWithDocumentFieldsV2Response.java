
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
 *         &lt;element name="RequestEnvelopeWithDocumentFieldsV2Result" type="{http://www.docusign.net/API/3.0}EnvelopeV2" minOccurs="0"/>
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
    "requestEnvelopeWithDocumentFieldsV2Result"
})
@XmlRootElement(name = "RequestEnvelopeWithDocumentFieldsV2Response")
public class RequestEnvelopeWithDocumentFieldsV2Response {

    @XmlElement(name = "RequestEnvelopeWithDocumentFieldsV2Result")
    protected EnvelopeV2 requestEnvelopeWithDocumentFieldsV2Result;

    /**
     * Gets the value of the requestEnvelopeWithDocumentFieldsV2Result property.
     * 
     * @return
     *     possible object is
     *     {@link EnvelopeV2 }
     *     
     */
    public EnvelopeV2 getRequestEnvelopeWithDocumentFieldsV2Result() {
        return requestEnvelopeWithDocumentFieldsV2Result;
    }

    /**
     * Sets the value of the requestEnvelopeWithDocumentFieldsV2Result property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnvelopeV2 }
     *     
     */
    public void setRequestEnvelopeWithDocumentFieldsV2Result(EnvelopeV2 value) {
        this.requestEnvelopeWithDocumentFieldsV2Result = value;
    }

}
