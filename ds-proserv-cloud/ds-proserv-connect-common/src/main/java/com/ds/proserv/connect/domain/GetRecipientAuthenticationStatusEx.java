
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
 *         &lt;element name="Arg" type="{http://www.docusign.net/API/3.0}GetRecipientAuthenticationStatusExArg" minOccurs="0"/>
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
    "arg"
})
@XmlRootElement(name = "GetRecipientAuthenticationStatusEx")
public class GetRecipientAuthenticationStatusEx {

    @XmlElement(name = "Arg")
    protected GetRecipientAuthenticationStatusExArg arg;

    /**
     * Gets the value of the arg property.
     * 
     * @return
     *     possible object is
     *     {@link GetRecipientAuthenticationStatusExArg }
     *     
     */
    public GetRecipientAuthenticationStatusExArg getArg() {
        return arg;
    }

    /**
     * Sets the value of the arg property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetRecipientAuthenticationStatusExArg }
     *     
     */
    public void setArg(GetRecipientAuthenticationStatusExArg value) {
        this.arg = value;
    }

}
