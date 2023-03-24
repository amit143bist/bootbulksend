
package com.ds.proserv.connect.domain;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetSharedAccessResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetSharedAccessResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AccountId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ResultSetSize" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *         &lt;element name="TotalSetSize" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *         &lt;element name="StartAtIndex" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *         &lt;element name="EndAtIndex" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *         &lt;element name="SharedItems" type="{http://www.docusign.net/API/3.0}SharedAccessItemResult" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "GetSharedAccessResult", propOrder = {
    "accountId",
    "resultSetSize",
    "totalSetSize",
    "startAtIndex",
    "endAtIndex",
    "sharedItems",
    "errorDetails"
})
public class GetSharedAccessResult {

    @XmlElement(name = "AccountId")
    protected String accountId;
    @XmlElement(name = "ResultSetSize", required = true, nillable = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger resultSetSize;
    @XmlElement(name = "TotalSetSize", required = true, nillable = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger totalSetSize;
    @XmlElement(name = "StartAtIndex", required = true, nillable = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger startAtIndex;
    @XmlElement(name = "EndAtIndex", required = true, nillable = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger endAtIndex;
    @XmlElement(name = "SharedItems", nillable = true)
    protected List<SharedAccessItemResult> sharedItems;
    @XmlElement(name = "ErrorDetails")
    protected ErrorDetails errorDetails;

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
     * Gets the value of the resultSetSize property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getResultSetSize() {
        return resultSetSize;
    }

    /**
     * Sets the value of the resultSetSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setResultSetSize(BigInteger value) {
        this.resultSetSize = value;
    }

    /**
     * Gets the value of the totalSetSize property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTotalSetSize() {
        return totalSetSize;
    }

    /**
     * Sets the value of the totalSetSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTotalSetSize(BigInteger value) {
        this.totalSetSize = value;
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
     * Gets the value of the endAtIndex property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getEndAtIndex() {
        return endAtIndex;
    }

    /**
     * Sets the value of the endAtIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setEndAtIndex(BigInteger value) {
        this.endAtIndex = value;
    }

    /**
     * Gets the value of the sharedItems property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sharedItems property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSharedItems().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SharedAccessItemResult }
     * 
     * 
     */
    public List<SharedAccessItemResult> getSharedItems() {
        if (sharedItems == null) {
            sharedItems = new ArrayList<SharedAccessItemResult>();
        }
        return this.sharedItems;
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
