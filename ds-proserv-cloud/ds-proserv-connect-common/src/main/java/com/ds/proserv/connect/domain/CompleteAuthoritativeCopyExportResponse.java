
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
 *         &lt;element name="CompleteAuthoritativeCopyExportResult" type="{http://www.docusign.net/API/3.0}AuthoritativeCopyExportStatus" minOccurs="0"/>
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
    "completeAuthoritativeCopyExportResult"
})
@XmlRootElement(name = "CompleteAuthoritativeCopyExportResponse")
public class CompleteAuthoritativeCopyExportResponse {

    @XmlElement(name = "CompleteAuthoritativeCopyExportResult")
    protected AuthoritativeCopyExportStatus completeAuthoritativeCopyExportResult;

    /**
     * Gets the value of the completeAuthoritativeCopyExportResult property.
     * 
     * @return
     *     possible object is
     *     {@link AuthoritativeCopyExportStatus }
     *     
     */
    public AuthoritativeCopyExportStatus getCompleteAuthoritativeCopyExportResult() {
        return completeAuthoritativeCopyExportResult;
    }

    /**
     * Sets the value of the completeAuthoritativeCopyExportResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link AuthoritativeCopyExportStatus }
     *     
     */
    public void setCompleteAuthoritativeCopyExportResult(AuthoritativeCopyExportStatus value) {
        this.completeAuthoritativeCopyExportResult = value;
    }

}
