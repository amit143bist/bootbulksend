
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
 *         &lt;element name="SetSharedAccessRequest" type="{http://www.docusign.net/API/3.0}SetSharedAccessRequest" minOccurs="0"/>
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
    "setSharedAccessRequest"
})
@XmlRootElement(name = "SetSharedAccess")
public class SetSharedAccess {

    @XmlElement(name = "SetSharedAccessRequest")
    protected SetSharedAccessRequest setSharedAccessRequest;

    /**
     * Gets the value of the setSharedAccessRequest property.
     * 
     * @return
     *     possible object is
     *     {@link SetSharedAccessRequest }
     *     
     */
    public SetSharedAccessRequest getSetSharedAccessRequest() {
        return setSharedAccessRequest;
    }

    /**
     * Sets the value of the setSharedAccessRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link SetSharedAccessRequest }
     *     
     */
    public void setSetSharedAccessRequest(SetSharedAccessRequest value) {
        this.setSharedAccessRequest = value;
    }

}
