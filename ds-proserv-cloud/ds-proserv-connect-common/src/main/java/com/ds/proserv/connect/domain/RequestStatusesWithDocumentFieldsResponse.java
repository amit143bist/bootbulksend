
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
 *         &lt;element name="RequestStatusesWithDocumentFieldsResult" type="{http://www.docusign.net/API/3.0}FilteredEnvelopeStatuses" minOccurs="0"/>
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
    "requestStatusesWithDocumentFieldsResult"
})
@XmlRootElement(name = "RequestStatusesWithDocumentFieldsResponse")
public class RequestStatusesWithDocumentFieldsResponse {

    @XmlElement(name = "RequestStatusesWithDocumentFieldsResult")
    protected FilteredEnvelopeStatuses requestStatusesWithDocumentFieldsResult;

    /**
     * Gets the value of the requestStatusesWithDocumentFieldsResult property.
     * 
     * @return
     *     possible object is
     *     {@link FilteredEnvelopeStatuses }
     *     
     */
    public FilteredEnvelopeStatuses getRequestStatusesWithDocumentFieldsResult() {
        return requestStatusesWithDocumentFieldsResult;
    }

    /**
     * Sets the value of the requestStatusesWithDocumentFieldsResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link FilteredEnvelopeStatuses }
     *     
     */
    public void setRequestStatusesWithDocumentFieldsResult(FilteredEnvelopeStatuses value) {
        this.requestStatusesWithDocumentFieldsResult = value;
    }

}
