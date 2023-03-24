
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RecipientSignatureProvider complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RecipientSignatureProvider">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SignatureProviderName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SignatureProviderOptions" type="{http://www.docusign.net/API/3.0}SignatureProviderOptions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecipientSignatureProvider", propOrder = {
    "signatureProviderName",
    "signatureProviderOptions"
})
public class RecipientSignatureProvider {

    @XmlElement(name = "SignatureProviderName")
    protected String signatureProviderName;
    @XmlElement(name = "SignatureProviderOptions")
    protected SignatureProviderOptions signatureProviderOptions;

    /**
     * Gets the value of the signatureProviderName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSignatureProviderName() {
        return signatureProviderName;
    }

    /**
     * Sets the value of the signatureProviderName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSignatureProviderName(String value) {
        this.signatureProviderName = value;
    }

    /**
     * Gets the value of the signatureProviderOptions property.
     * 
     * @return
     *     possible object is
     *     {@link SignatureProviderOptions }
     *     
     */
    public SignatureProviderOptions getSignatureProviderOptions() {
        return signatureProviderOptions;
    }

    /**
     * Sets the value of the signatureProviderOptions property.
     * 
     * @param value
     *     allowed object is
     *     {@link SignatureProviderOptions }
     *     
     */
    public void setSignatureProviderOptions(SignatureProviderOptions value) {
        this.signatureProviderOptions = value;
    }

}
