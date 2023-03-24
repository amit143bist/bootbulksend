
package com.ds.proserv.connect.domain;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SharedAccessFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SharedAccessFilter">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="UserIds" type="{http://www.docusign.net/API/3.0}ArrayOfString6" minOccurs="0"/>
 *         &lt;element name="ItemType" type="{http://www.docusign.net/API/3.0}SharedAccessItemType"/>
 *         &lt;element name="SearchText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Shared" type="{http://www.docusign.net/API/3.0}SharedAccessShareType"/>
 *         &lt;element name="StartAtIndex" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *         &lt;element name="Count" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SharedAccessFilter", propOrder = {
    "userIds",
    "itemType",
    "searchText",
    "shared",
    "startAtIndex",
    "count"
})
public class SharedAccessFilter {

    @XmlElement(name = "UserIds")
    protected ArrayOfString6 userIds;
    @XmlElement(name = "ItemType", required = true)
    protected SharedAccessItemType itemType;
    @XmlElement(name = "SearchText")
    protected String searchText;
    @XmlElement(name = "Shared", required = true, nillable = true)
    protected SharedAccessShareType shared;
    @XmlElement(name = "StartAtIndex", required = true, nillable = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger startAtIndex;
    @XmlElement(name = "Count", required = true, nillable = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger count;

    /**
     * Gets the value of the userIds property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfString6 }
     *     
     */
    public ArrayOfString6 getUserIds() {
        return userIds;
    }

    /**
     * Sets the value of the userIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfString6 }
     *     
     */
    public void setUserIds(ArrayOfString6 value) {
        this.userIds = value;
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
     * Gets the value of the searchText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSearchText() {
        return searchText;
    }

    /**
     * Sets the value of the searchText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSearchText(String value) {
        this.searchText = value;
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
     * Gets the value of the startAtIndex property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getStartAtIndex() {
        return startAtIndex;
    }

    /**
     * Sets the value of the startAtIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setStartAtIndex(BigInteger value) {
        this.startAtIndex = value;
    }

    /**
     * Gets the value of the count property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getCount() {
        return count;
    }

    /**
     * Sets the value of the count property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCount(BigInteger value) {
        this.count = value;
    }

}
