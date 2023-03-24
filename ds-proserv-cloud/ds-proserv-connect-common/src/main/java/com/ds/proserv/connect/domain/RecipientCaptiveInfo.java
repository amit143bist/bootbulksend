
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RecipientCaptiveInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RecipientCaptiveInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ClientUserId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="EmbeddedRecipientStartURL" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecipientCaptiveInfo", propOrder = {
    "clientUserId",
    "embeddedRecipientStartURL"
})
public class RecipientCaptiveInfo {

    @XmlElement(name = "ClientUserId")
    protected String clientUserId;
    @XmlElement(name = "EmbeddedRecipientStartURL")
    protected String embeddedRecipientStartURL;

    /**
     * Gets the value of the clientUserId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClientUserId() {
        return clientUserId;
    }

    /**
     * Sets the value of the clientUserId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClientUserId(String value) {
        this.clientUserId = value;
    }

    /**
     * Gets the value of the embeddedRecipientStartURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmbeddedRecipientStartURL() {
        return embeddedRecipientStartURL;
    }

    /**
     * Sets the value of the embeddedRecipientStartURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmbeddedRecipientStartURL(String value) {
        this.embeddedRecipientStartURL = value;
    }

}
