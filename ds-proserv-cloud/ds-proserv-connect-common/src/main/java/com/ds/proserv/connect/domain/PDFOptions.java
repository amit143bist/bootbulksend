
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PDFOptions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PDFOptions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ShowChanges" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="AddWaterMark" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="IncludeCert" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="CertificateLanguage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PDFOptions", propOrder = {
    "showChanges",
    "addWaterMark",
    "includeCert",
    "certificateLanguage"
})
public class PDFOptions {

    @XmlElement(name = "ShowChanges")
    protected boolean showChanges;
    @XmlElement(name = "AddWaterMark")
    protected boolean addWaterMark;
    @XmlElement(name = "IncludeCert")
    protected boolean includeCert;
    @XmlElement(name = "CertificateLanguage")
    protected String certificateLanguage;

    /**
     * Gets the value of the showChanges property.
     * 
     */
    public boolean isShowChanges() {
        return showChanges;
    }

    /**
     * Sets the value of the showChanges property.
     * 
     */
    public void setShowChanges(boolean value) {
        this.showChanges = value;
    }

    /**
     * Gets the value of the addWaterMark property.
     * 
     */
    public boolean isAddWaterMark() {
        return addWaterMark;
    }

    /**
     * Sets the value of the addWaterMark property.
     * 
     */
    public void setAddWaterMark(boolean value) {
        this.addWaterMark = value;
    }

    /**
     * Gets the value of the includeCert property.
     * 
     */
    public boolean isIncludeCert() {
        return includeCert;
    }

    /**
     * Sets the value of the includeCert property.
     * 
     */
    public void setIncludeCert(boolean value) {
        this.includeCert = value;
    }

    /**
     * Gets the value of the certificateLanguage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCertificateLanguage() {
        return certificateLanguage;
    }

    /**
     * Sets the value of the certificateLanguage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCertificateLanguage(String value) {
        this.certificateLanguage = value;
    }

}
