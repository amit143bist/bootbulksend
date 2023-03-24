
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SetSharedAccessResult" type="{http://www.docusign.net/API/3.0}SetSharedAccessResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "setSharedAccessResult"
})
@XmlRootElement(name = "SetSharedAccessResponse")
public class SetSharedAccessResponse {

    @XmlElement(name = "SetSharedAccessResult")
    protected SetSharedAccessResult setSharedAccessResult;

    /**
     * Gets the value of the setSharedAccessResult property.
     * 
     * @return
     *     possible object is
     *     {@link SetSharedAccessResult }
     *     
     */
    public SetSharedAccessResult getSetSharedAccessResult() {
        return setSharedAccessResult;
    }

    /**
     * Sets the value of the setSharedAccessResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link SetSharedAccessResult }
     *     
     */
    public void setSetSharedAccessResult(SetSharedAccessResult value) {
        this.setSharedAccessResult = value;
    }

}
