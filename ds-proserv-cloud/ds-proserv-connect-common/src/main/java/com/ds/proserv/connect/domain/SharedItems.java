
package com.ds.proserv.connect.domain;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SharedItems complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SharedItems">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="User" type="{http://www.docusign.net/API/3.0}SharedUserInfoRequest"/>
 *         &lt;element name="SharedItem" type="{http://www.docusign.net/API/3.0}SharedItemRequest" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SharedItems", propOrder = {
    "user",
    "sharedItem"
})
public class SharedItems {

    @XmlElement(name = "User", required = true, nillable = true)
    protected SharedUserInfoRequest user;
    @XmlElement(name = "SharedItem", nillable = true)
    protected List<SharedItemRequest> sharedItem;

    /**
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link SharedUserInfoRequest }
     *     
     */
    public SharedUserInfoRequest getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link SharedUserInfoRequest }
     *     
     */
    public void setUser(SharedUserInfoRequest value) {
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
     * {@link SharedItemRequest }
     * 
     * 
     */
    public List<SharedItemRequest> getSharedItem() {
        if (sharedItem == null) {
            sharedItem = new ArrayList<SharedItemRequest>();
        }
        return this.sharedItem;
    }

}
