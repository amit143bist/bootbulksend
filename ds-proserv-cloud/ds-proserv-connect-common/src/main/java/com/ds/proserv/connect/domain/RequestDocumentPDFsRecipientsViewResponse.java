
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
 *         &lt;element name="RequestDocumentPDFsRecipientsViewResult" type="{http://www.docusign.net/API/3.0}DocumentPDFs" minOccurs="0"/>
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
    "requestDocumentPDFsRecipientsViewResult"
})
@XmlRootElement(name = "RequestDocumentPDFsRecipientsViewResponse")
public class RequestDocumentPDFsRecipientsViewResponse {

    @XmlElement(name = "RequestDocumentPDFsRecipientsViewResult")
    protected DocumentPDFs requestDocumentPDFsRecipientsViewResult;

    /**
     * Gets the value of the requestDocumentPDFsRecipientsViewResult property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentPDFs }
     *     
     */
    public DocumentPDFs getRequestDocumentPDFsRecipientsViewResult() {
        return requestDocumentPDFsRecipientsViewResult;
    }

    /**
     * Sets the value of the requestDocumentPDFsRecipientsViewResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentPDFs }
     *     
     */
    public void setRequestDocumentPDFsRecipientsViewResult(DocumentPDFs value) {
        this.requestDocumentPDFsRecipientsViewResult = value;
    }

}
