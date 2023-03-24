
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
 *         &lt;element name="DeleteCaptiveRecipientsSignaturesResult" type="{http://www.docusign.net/API/3.0}CaptiveRecipientResults" minOccurs="0"/>
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
    "deleteCaptiveRecipientsSignaturesResult"
})
@XmlRootElement(name = "DeleteCaptiveRecipientsSignaturesResponse")
public class DeleteCaptiveRecipientsSignaturesResponse {

    @XmlElement(name = "DeleteCaptiveRecipientsSignaturesResult")
    protected CaptiveRecipientResults deleteCaptiveRecipientsSignaturesResult;

    /**
     * Gets the value of the deleteCaptiveRecipientsSignaturesResult property.
     * 
     * @return
     *     possible object is
     *     {@link CaptiveRecipientResults }
     *     
     */
    public CaptiveRecipientResults getDeleteCaptiveRecipientsSignaturesResult() {
        return deleteCaptiveRecipientsSignaturesResult;
    }

    /**
     * Sets the value of the deleteCaptiveRecipientsSignaturesResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link CaptiveRecipientResults }
     *     
     */
    public void setDeleteCaptiveRecipientsSignaturesResult(CaptiveRecipientResults value) {
        this.deleteCaptiveRecipientsSignaturesResult = value;
    }

}
