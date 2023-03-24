
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RequestEnvelopeV2Options complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RequestEnvelopeV2Options">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="IncludeDocumentBytes" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IncludeAC" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
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
@XmlType(name = "RequestEnvelopeV2Options", propOrder = {
    "includeDocumentBytes",
    "includeAC",
    "includeAnchorTabLocations"
})
public class RequestEnvelopeV2Options {

    @XmlElement(name = "IncludeDocumentBytes")
    protected Boolean includeDocumentBytes;
    @XmlElement(name = "IncludeAC")
    protected Boolean includeAC;
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
     * Gets the value of the includeAC property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIncludeAC() {
        return includeAC;
    }

    /**
     * Sets the value of the includeAC property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludeAC(Boolean value) {
        this.includeAC = value;
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
