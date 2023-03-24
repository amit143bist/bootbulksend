
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SharedItemResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SharedItemResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="User" type="{http://www.docusign.net/API/3.0}SharedUserInfoResult" minOccurs="0"/>
 *         &lt;element name="Shared" type="{http://www.docusign.net/API/3.0}SharedAccessShareType"/>
 *         &lt;element name="ItemType" type="{http://www.docusign.net/API/3.0}SharedAccessItemType"/>
 *         &lt;element name="ErrorDetails" type="{http://www.docusign.net/API/3.0}ErrorDetails" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SharedItemResult", propOrder = {
    "user",
    "shared",
    "itemType",
    "errorDetails"
})
public class SharedItemResult {

    @XmlElement(name = "User")
    protected SharedUserInfoResult user;
    @XmlElement(name = "Shared", required = true)
    protected SharedAccessShareType shared;
    @XmlElement(name = "ItemType", required = true)
    protected SharedAccessItemType itemType;
    @XmlElement(name = "ErrorDetails")
    protected ErrorDetails errorDetails;

    /**
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link SharedUserInfoResult }
     *     
     */
    public SharedUserInfoResult getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link SharedUserInfoResult }
     *     
     */
    public void setUser(SharedUserInfoResult value) {
        this.user = value;
    }

    /**
     * Gets the value of the shared property.
     * 
     * @return
     *     possible object is
     *     {@link SharedAccessShareType }
     *     
     */
    public SharedAccessShareType getShared() {
        return shared;
    }

    /**
     * Sets the value of the shared property.
     * 
     * @param value
     *     allowed object is
     *     {@link SharedAccessShareType }
     *     
     */
    public void setShared(SharedAccessShareType value) {
        this.shared = value;
    }

    /**
     * Gets the value of the itemType property.
     * 
     * @return
     *     possible object is
     *     {@link SharedAccessItemType }
     *     
     */
    public SharedAccessItemType getItemType() {
        return itemType;
    }

    /**
     * Sets the value of the itemType property.
     * 
     * @param value
     *     allowed object is
     *     {@link SharedAccessItemType }
     *     
     */
    public void setItemType(SharedAccessItemType value) {
        this.itemType = value;
    }

    /**
     * Gets the value of the errorDetails property.
     * 
     * @return
     *     possible object is
     *     {@link ErrorDetails }
     *     
     */
    public ErrorDetails getErrorDetails() {
        return errorDetails;
    }

    /**
     * Sets the value of the errorDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link ErrorDetails }
     *     
     */
    public void setErrorDetails(ErrorDetails value) {
        this.errorDetails = value;
    }

}
