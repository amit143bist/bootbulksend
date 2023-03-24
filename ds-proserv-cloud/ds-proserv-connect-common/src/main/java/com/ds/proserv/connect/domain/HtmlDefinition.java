
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HtmlDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HtmlDefinition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Source" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RemoveEmptyTags" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HeaderLabel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DisplayAnchorPrefix" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MaxScreenWidth" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="DisplayAnchors" type="{http://www.docusign.net/API/3.0}ArrayOfDisplayAnchor" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HtmlDefinition", propOrder = {
    "source",
    "removeEmptyTags",
    "headerLabel",
    "displayAnchorPrefix",
    "maxScreenWidth",
    "displayAnchors"
})
public class HtmlDefinition {

    @XmlElement(name = "Source")
    protected String source;
    @XmlElement(name = "RemoveEmptyTags")
    protected String removeEmptyTags;
    @XmlElement(name = "HeaderLabel")
    protected String headerLabel;
    @XmlElement(name = "DisplayAnchorPrefix")
    protected String displayAnchorPrefix;
    @XmlElement(name = "MaxScreenWidth")
    protected Integer maxScreenWidth;
    @XmlElement(name = "DisplayAnchors")
    protected ArrayOfDisplayAnchor displayAnchors;

    /**
     * Gets the value of the source property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSource(String value) {
        this.source = value;
    }

    /**
     * Gets the value of the removeEmptyTags property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemoveEmptyTags() {
        return removeEmptyTags;
    }

    /**
     * Sets the value of the removeEmptyTags property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemoveEmptyTags(String value) {
        this.removeEmptyTags = value;
    }

    /**
     * Gets the value of the headerLabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHeaderLabel() {
        return headerLabel;
    }

    /**
     * Sets the value of the headerLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHeaderLabel(String value) {
        this.headerLabel = value;
    }

    /**
     * Gets the value of the displayAnchorPrefix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayAnchorPrefix() {
        return displayAnchorPrefix;
    }

    /**
     * Sets the value of the displayAnchorPrefix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayAnchorPrefix(String value) {
        this.displayAnchorPrefix = value;
    }

    /**
     * Gets the value of the maxScreenWidth property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxScreenWidth() {
        return maxScreenWidth;
    }

    /**
     * Sets the value of the maxScreenWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxScreenWidth(Integer value) {
        this.maxScreenWidth = value;
    }

    /**
     * Gets the value of the displayAnchors property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfDisplayAnchor }
     *     
     */
    public ArrayOfDisplayAnchor getDisplayAnchors() {
        return displayAnchors;
    }

    /**
     * Sets the value of the displayAnchors property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfDisplayAnchor }
     *     
     */
    public void setDisplayAnchors(ArrayOfDisplayAnchor value) {
        this.displayAnchors = value;
    }

}
