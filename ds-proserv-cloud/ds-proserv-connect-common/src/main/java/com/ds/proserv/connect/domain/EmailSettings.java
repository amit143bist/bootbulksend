
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EmailSettings complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EmailSettings">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ReplyEmailAddressOverride" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ReplyEmailNameOverride" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="BCCEmailAddresses" type="{http://www.docusign.net/API/3.0}ArrayOfBCCEmailAddress" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EmailSettings", propOrder = {
    "replyEmailAddressOverride",
    "replyEmailNameOverride",
    "bccEmailAddresses"
})
public class EmailSettings {

    @XmlElement(name = "ReplyEmailAddressOverride")
    protected String replyEmailAddressOverride;
    @XmlElement(name = "ReplyEmailNameOverride")
    protected String replyEmailNameOverride;
    @XmlElement(name = "BCCEmailAddresses")
    protected ArrayOfBCCEmailAddress bccEmailAddresses;

    /**
     * Gets the value of the replyEmailAddressOverride property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReplyEmailAddressOverride() {
        return replyEmailAddressOverride;
    }

    /**
     * Sets the value of the replyEmailAddressOverride property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReplyEmailAddressOverride(String value) {
        this.replyEmailAddressOverride = value;
    }

    /**
     * Gets the value of the replyEmailNameOverride property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReplyEmailNameOverride() {
        return replyEmailNameOverride;
    }

    /**
     * Sets the value of the replyEmailNameOverride property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReplyEmailNameOverride(String value) {
        this.replyEmailNameOverride = value;
    }

    /**
     * Gets the value of the bccEmailAddresses property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfBCCEmailAddress }
     *     
     */
    public ArrayOfBCCEmailAddress getBCCEmailAddresses() {
        return bccEmailAddresses;
    }

    /**
     * Sets the value of the bccEmailAddresses property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfBCCEmailAddress }
     *     
     */
    public void setBCCEmailAddresses(ArrayOfBCCEmailAddress value) {
        this.bccEmailAddresses = value;
    }

}
