
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
 *         &lt;element name="AccountId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SharedAccessFilter" type="{http://www.docusign.net/API/3.0}SharedAccessFilter" minOccurs="0"/>
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
    "accountId",
    "sharedAccessFilter"
})
@XmlRootElement(name = "GetSharedAccess")
public class GetSharedAccess {

    @XmlElement(name = "AccountId")
    protected String accountId;
    @XmlElement(name = "SharedAccessFilter")
    protected SharedAccessFilter sharedAccessFilter;

    /**
     * Gets the value of the accountId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Sets the value of the accountId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccountId(String value) {
        this.accountId = value;
    }

    /**
     * Gets the value of the sharedAccessFilter property.
     * 
     * @return
     *     possible object is
     *     {@link SharedAccessFilter }
     *     
     */
    public SharedAccessFilter getSharedAccessFilter() {
        return sharedAccessFilter;
    }

    /**
     * Sets the value of the sharedAccessFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link SharedAccessFilter }
     *     
     */
    public void setSharedAccessFilter(SharedAccessFilter value) {
        this.sharedAccessFilter = value;
    }

}
