
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
 *         &lt;element name="GetSharedAccessResult" type="{http://www.docusign.net/API/3.0}GetSharedAccessResult" minOccurs="0"/>
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
    "getSharedAccessResult"
})
@XmlRootElement(name = "GetSharedAccessResponse")
public class GetSharedAccessResponse {

    @XmlElement(name = "GetSharedAccessResult")
    protected GetSharedAccessResult getSharedAccessResult;

    /**
     * Gets the value of the getSharedAccessResult property.
     * 
     * @return
     *     possible object is
     *     {@link GetSharedAccessResult }
     *     
     */
    public GetSharedAccessResult getGetSharedAccessResult() {
        return getSharedAccessResult;
    }

    /**
     * Sets the value of the getSharedAccessResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetSharedAccessResult }
     *     
     */
    public void setGetSharedAccessResult(GetSharedAccessResult value) {
        this.getSharedAccessResult = value;
    }

}
