
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CollapsibleSettings complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CollapsibleSettings">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="LabelStyle" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ArrowStyle" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ArrowClosed" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ArrowOpen" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ArrowLocation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ArrowColor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ArrowSize" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ContainerStyle" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OnlyArrowIsClickable" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="OuterLabelAndArrowStyle" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CollapsibleSettings", propOrder = {
    "labelStyle",
    "arrowStyle",
    "arrowClosed",
    "arrowOpen",
    "arrowLocation",
    "arrowColor",
    "arrowSize",
    "containerStyle",
    "onlyArrowIsClickable",
    "outerLabelAndArrowStyle"
})
public class CollapsibleSettings {

    @XmlElement(name = "LabelStyle")
    protected String labelStyle;
    @XmlElement(name = "ArrowStyle")
    protected String arrowStyle;
    @XmlElement(name = "ArrowClosed")
    protected String arrowClosed;
    @XmlElement(name = "ArrowOpen")
    protected String arrowOpen;
    @XmlElement(name = "ArrowLocation")
    protected String arrowLocation;
    @XmlElement(name = "ArrowColor")
    protected String arrowColor;
    @XmlElement(name = "ArrowSize")
    protected String arrowSize;
    @XmlElement(name = "ContainerStyle")
    protected String containerStyle;
    @XmlElement(name = "OnlyArrowIsClickable")
    protected Boolean onlyArrowIsClickable;
    @XmlElement(name = "OuterLabelAndArrowStyle")
    protected String outerLabelAndArrowStyle;

    /**
     * Gets the value of the labelStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabelStyle() {
        return labelStyle;
    }

    /**
     * Sets the value of the labelStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabelStyle(String value) {
        this.labelStyle = value;
    }

    /**
     * Gets the value of the arrowStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArrowStyle() {
        return arrowStyle;
    }

    /**
     * Sets the value of the arrowStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArrowStyle(String value) {
        this.arrowStyle = value;
    }

    /**
     * Gets the value of the arrowClosed property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArrowClosed() {
        return arrowClosed;
    }

    /**
     * Sets the value of the arrowClosed property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArrowClosed(String value) {
        this.arrowClosed = value;
    }

    /**
     * Gets the value of the arrowOpen property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArrowOpen() {
        return arrowOpen;
    }

    /**
     * Sets the value of the arrowOpen property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArrowOpen(String value) {
        this.arrowOpen = value;
    }

    /**
     * Gets the value of the arrowLocation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArrowLocation() {
        return arrowLocation;
    }

    /**
     * Sets the value of the arrowLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArrowLocation(String value) {
        this.arrowLocation = value;
    }

    /**
     * Gets the value of the arrowColor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArrowColor() {
        return arrowColor;
    }

    /**
     * Sets the value of the arrowColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArrowColor(String value) {
        this.arrowColor = value;
    }

    /**
     * Gets the value of the arrowSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArrowSize() {
        return arrowSize;
    }

    /**
     * Sets the value of the arrowSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArrowSize(String value) {
        this.arrowSize = value;
    }

    /**
     * Gets the value of the containerStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContainerStyle() {
        return containerStyle;
    }

    /**
     * Sets the value of the containerStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContainerStyle(String value) {
        this.containerStyle = value;
    }

    /**
     * Gets the value of the onlyArrowIsClickable property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isOnlyArrowIsClickable() {
        return onlyArrowIsClickable;
    }

    /**
     * Sets the value of the onlyArrowIsClickable property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOnlyArrowIsClickable(Boolean value) {
        this.onlyArrowIsClickable = value;
    }

    /**
     * Gets the value of the outerLabelAndArrowStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOuterLabelAndArrowStyle() {
        return outerLabelAndArrowStyle;
    }

    /**
     * Sets the value of the outerLabelAndArrowStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOuterLabelAndArrowStyle(String value) {
        this.outerLabelAndArrowStyle = value;
    }

}
