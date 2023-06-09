
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RecipientSMSAuthentication complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RecipientSMSAuthentication">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SenderProvidedNumbers" type="{http://www.docusign.net/API/3.0}ArrayOfString" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecipientSMSAuthentication", propOrder = {
    "senderProvidedNumbers"
})
public class RecipientSMSAuthentication {

    @XmlElement(name = "SenderProvidedNumbers")
    protected ArrayOfString senderProvidedNumbers;

    /**
     * Gets the value of the senderProvidedNumbers property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfString }
     *     
     */
    public ArrayOfString getSenderProvidedNumbers() {
        return senderProvidedNumbers;
    }

    /**
     * Sets the value of the senderProvidedNumbers property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfString }
     *     
     */
    public void setSenderProvidedNumbers(ArrayOfString value) {
        this.senderProvidedNumbers = value;
    }

}
