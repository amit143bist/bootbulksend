
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DisplayAnchor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DisplayAnchor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="StartAnchor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="EndAnchor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RemoveStartAnchor" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="RemoveEndAnchor" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="CaseSensitive" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="DisplaySettings" type="{http://www.docusign.net/API/3.0}DisplaySettings" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DisplayAnchor", propOrder = {
    "startAnchor",
    "endAnchor",
    "removeStartAnchor",
    "removeEndAnchor",
    "caseSensitive",
    "displaySettings"
})
public class DisplayAnchor {

    @XmlElement(name = "StartAnchor")
    protected String startAnchor;
    @XmlElement(name = "EndAnchor")
    protected String endAnchor;
    @XmlElement(name = "RemoveStartAnchor")
    protected Boolean removeStartAnchor;
    @XmlElement(name = "RemoveEndAnchor")
    protected Boolean removeEndAnchor;
    @XmlElement(name = "CaseSensitive")
    protected Boolean caseSensitive;
    @XmlElement(name = "DisplaySettings")
    protected DisplaySettings displaySettings;

    /**
     * Gets the value of the startAnchor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStartAnchor() {
        return startAnchor;
    }

    /**
     * Sets the value of the startAnchor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartAnchor(String value) {
        this.startAnchor = value;
    }

    /**
     * Gets the value of the endAnchor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndAnchor() {
        return endAnchor;
    }

    /**
     * Sets the value of the endAnchor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndAnchor(String value) {
        this.endAnchor = value;
    }

    /**
     * Gets the value of the removeStartAnchor property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRemoveStartAnchor() {
        return removeStartAnchor;
    }

    /**
     * Sets the value of the removeStartAnchor property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRemoveStartAnchor(Boolean value) {
        this.removeStartAnchor = value;
    }

    /**
     * Gets the value of the removeEndAnchor property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRemoveEndAnchor() {
        return removeEndAnchor;
    }

    /**
     * Sets the value of the removeEndAnchor property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRemoveEndAnchor(Boolean value) {
        this.removeEndAnchor = value;
    }

    /**
     * Gets the value of the caseSensitive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * Sets the value of the caseSensitive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCaseSensitive(Boolean value) {
        this.caseSensitive = value;
    }

    /**
     * Gets the value of the displaySettings property.
     * 
     * @return
     *     possible object is
     *     {@link DisplaySettings }
     *     
     */
    public DisplaySettings getDisplaySettings() {
        return displaySettings;
    }

    /**
     * Sets the value of the displaySettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link DisplaySettings }
     *     
     */
    public void setDisplaySettings(DisplaySettings value) {
        this.displaySettings = value;
    }

}
