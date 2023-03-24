
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SignatureProviderOptions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SignatureProviderOptions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Sms" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OneTimePassword" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CPFNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SignerRole" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SignatureProviderOptions", propOrder = {
    "sms",
    "oneTimePassword",
    "cpfNumber",
    "signerRole"
})
public class SignatureProviderOptions {

    @XmlElement(name = "Sms")
    protected String sms;
    @XmlElement(name = "OneTimePassword")
    protected String oneTimePassword;
    @XmlElement(name = "CPFNumber")
    protected String cpfNumber;
    @XmlElement(name = "SignerRole")
    protected String signerRole;

    /**
     * Gets the value of the sms property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSms() {
        return sms;
    }

    /**
     * Sets the value of the sms property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSms(String value) {
        this.sms = value;
    }

    /**
     * Gets the value of the oneTimePassword property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOneTimePassword() {
        return oneTimePassword;
    }

    /**
     * Sets the value of the oneTimePassword property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOneTimePassword(String value) {
        this.oneTimePassword = value;
    }

    /**
     * Gets the value of the cpfNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCPFNumber() {
        return cpfNumber;
    }

    /**
     * Sets the value of the cpfNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCPFNumber(String value) {
        this.cpfNumber = value;
    }

    /**
     * Gets the value of the signerRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSignerRole() {
        return signerRole;
    }

    /**
     * Sets the value of the signerRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSignerRole(String value) {
        this.signerRole = value;
    }

}
