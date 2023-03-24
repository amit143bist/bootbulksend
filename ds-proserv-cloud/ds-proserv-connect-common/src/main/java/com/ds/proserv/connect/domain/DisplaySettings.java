
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DisplaySettings complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DisplaySettings">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Display" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DisplayLabel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DisplayPageNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="DisplayOrder" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="TableStyle" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CellStyle" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="InlineOuterStyle" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LabelWhenOpened" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PreLabel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HideLabelWhenOpened" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ScrollToTopWhenOpened" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="CollapsibleSettings" type="{http://www.docusign.net/API/3.0}CollapsibleSettings" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DisplaySettings", propOrder = {
    "display",
    "displayLabel",
    "displayPageNumber",
    "displayOrder",
    "tableStyle",
    "cellStyle",
    "inlineOuterStyle",
    "labelWhenOpened",
    "preLabel",
    "hideLabelWhenOpened",
    "scrollToTopWhenOpened",
    "collapsibleSettings"
})
public class DisplaySettings {

    @XmlElement(name = "Display")
    protected String display;
    @XmlElement(name = "DisplayLabel")
    protected String displayLabel;
    @XmlElement(name = "DisplayPageNumber")
    protected Integer displayPageNumber;
    @XmlElement(name = "DisplayOrder")
    protected Integer displayOrder;
    @XmlElement(name = "TableStyle")
    protected String tableStyle;
    @XmlElement(name = "CellStyle")
    protected String cellStyle;
    @XmlElement(name = "InlineOuterStyle")
    protected String inlineOuterStyle;
    @XmlElement(name = "LabelWhenOpened")
    protected String labelWhenOpened;
    @XmlElement(name = "PreLabel")
    protected String preLabel;
    @XmlElement(name = "HideLabelWhenOpened")
    protected Boolean hideLabelWhenOpened;
    @XmlElement(name = "ScrollToTopWhenOpened")
    protected Boolean scrollToTopWhenOpened;
    @XmlElement(name = "CollapsibleSettings")
    protected CollapsibleSettings collapsibleSettings;

    /**
     * Gets the value of the display property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplay() {
        return display;
    }

    /**
     * Sets the value of the display property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplay(String value) {
        this.display = value;
    }

    /**
     * Gets the value of the displayLabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayLabel() {
        return displayLabel;
    }

    /**
     * Sets the value of the displayLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayLabel(String value) {
        this.displayLabel = value;
    }

    /**
     * Gets the value of the displayPageNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDisplayPageNumber() {
        return displayPageNumber;
    }

    /**
     * Sets the value of the displayPageNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDisplayPageNumber(Integer value) {
        this.displayPageNumber = value;
    }

    /**
     * Gets the value of the displayOrder property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDisplayOrder() {
        return displayOrder;
    }

    /**
     * Sets the value of the displayOrder property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDisplayOrder(Integer value) {
        this.displayOrder = value;
    }

    /**
     * Gets the value of the tableStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTableStyle() {
        return tableStyle;
    }

    /**
     * Sets the value of the tableStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTableStyle(String value) {
        this.tableStyle = value;
    }

    /**
     * Gets the value of the cellStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCellStyle() {
        return cellStyle;
    }

    /**
     * Sets the value of the cellStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCellStyle(String value) {
        this.cellStyle = value;
    }

    /**
     * Gets the value of the inlineOuterStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInlineOuterStyle() {
        return inlineOuterStyle;
    }

    /**
     * Sets the value of the inlineOuterStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInlineOuterStyle(String value) {
        this.inlineOuterStyle = value;
    }

    /**
     * Gets the value of the labelWhenOpened property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabelWhenOpened() {
        return labelWhenOpened;
    }

    /**
     * Sets the value of the labelWhenOpened property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabelWhenOpened(String value) {
        this.labelWhenOpened = value;
    }

    /**
     * Gets the value of the preLabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreLabel() {
        return preLabel;
    }

    /**
     * Sets the value of the preLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreLabel(String value) {
        this.preLabel = value;
    }

    /**
     * Gets the value of the hideLabelWhenOpened property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isHideLabelWhenOpened() {
        return hideLabelWhenOpened;
    }

    /**
     * Sets the value of the hideLabelWhenOpened property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHideLabelWhenOpened(Boolean value) {
        this.hideLabelWhenOpened = value;
    }

    /**
     * Gets the value of the scrollToTopWhenOpened property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isScrollToTopWhenOpened() {
        return scrollToTopWhenOpened;
    }

    /**
     * Sets the value of the scrollToTopWhenOpened property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setScrollToTopWhenOpened(Boolean value) {
        this.scrollToTopWhenOpened = value;
    }

    /**
     * Gets the value of the collapsibleSettings property.
     * 
     * @return
     *     possible object is
     *     {@link CollapsibleSettings }
     *     
     */
    public CollapsibleSettings getCollapsibleSettings() {
        return collapsibleSettings;
    }

    /**
     * Sets the value of the collapsibleSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link CollapsibleSettings }
     *     
     */
    public void setCollapsibleSettings(CollapsibleSettings value) {
        this.collapsibleSettings = value;
    }

}
