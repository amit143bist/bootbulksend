
package com.ds.proserv.connect.domain;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SharedAccessItemResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SharedAccessItemResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="User" type="{http://www.docusign.net/API/3.0}SharedUserInfoResult"/>
 *         &lt;element name="SharedItem" type="{http://www.docusign.net/API/3.0}SharedItemResult" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "SharedAccessItemResult", propOrder = {
    "user",
    "sharedItem",
    "errorDetails"
})
public class SharedAccessItemResult {

    @XmlElement(name = "User", required = true, nillable = true)
    protected SharedUserInfoResult user;
    @XmlElement(name = "SharedItem", nillable = true)
    protected List<SharedItemResult> sharedItem;
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
     * Gets the value of the sharedItem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sharedItem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSharedItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SharedItemResult }
     * 
     * 
     */
    public List<SharedItemResult> getSharedItem() {
        if (sharedItem == null) {
            sharedItem = new ArrayList<SharedItemResult>();
        }
        return this.sharedItem;
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
