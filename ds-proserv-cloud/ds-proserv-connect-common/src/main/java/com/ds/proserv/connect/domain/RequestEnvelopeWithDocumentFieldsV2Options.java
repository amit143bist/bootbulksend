
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RequestEnvelopeWithDocumentFieldsV2Options complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RequestEnvelopeWithDocumentFieldsV2Options">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="IncludeDocumentBytes" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IncludeSummary" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IncludeUsedResponsiveSigning" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="CertLanguage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="IncludeAnchorTabLocations" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequestEnvelopeWithDocumentFieldsV2Options", propOrder = {
    "includeDocumentBytes",
    "includeSummary",
    "includeUsedResponsiveSigning",
    "certLanguage",
    "includeAnchorTabLocations"
})
public class RequestEnvelopeWithDocumentFieldsV2Options {

    @XmlElement(name = "IncludeDocumentBytes")
    protected Boolean includeDocumentBytes;
    @XmlElement(name = "IncludeSummary")
    protected Boolean includeSummary;
    @XmlElement(name = "IncludeUsedResponsiveSigning")
    protected Boolean includeUsedResponsiveSigning;
    @XmlElement(name = "CertLanguage")
    protected String certLanguage;
    @XmlElement(name = "IncludeAnchorTabLocations")
    protected Boolean includeAnchorTabLocations;

    /**
     * Gets the value of the includeDocumentBytes property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIncludeDocumentBytes() {
        return includeDocumentBytes;
    }

    /**
     * Sets the value of the includeDocumentBytes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludeDocumentBytes(Boolean value) {
        this.includeDocumentBytes = value;
    }

    /**
     * Gets the value of the includeSummary property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIncludeSummary() {
        return includeSummary;
    }

    /**
     * Sets the value of the includeSummary property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludeSummary(Boolean value) {
        this.includeSummary = value;
    }

    /**
     * Gets the value of the includeUsedResponsiveSigning property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIncludeUsedResponsiveSigning() {
        return includeUsedResponsiveSigning;
    }

    /**
     * Sets the value of the includeUsedResponsiveSigning property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludeUsedResponsiveSigning(Boolean value) {
        this.includeUsedResponsiveSigning = value;
    }

    /**
     * Gets the value of the certLanguage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCertLanguage() {
        return certLanguage;
    }

    /**
     * Sets the value of the certLanguage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCertLanguage(String value) {
        this.certLanguage = value;
    }

    /**
     * Gets the value of the includeAnchorTabLocations property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIncludeAnchorTabLocations() {
        return includeAnchorTabLocations;
    }

    /**
     * Sets the value of the includeAnchorTabLocations property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludeAnchorTabLocations(Boolean value) {
        this.includeAnchorTabLocations = value;
    }

}
