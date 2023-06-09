
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
 *         &lt;element name="PublishConnectFailuresResult" type="{http://www.docusign.net/API/3.0}ArrayOfPublishConnectFailuresResult" minOccurs="0"/>
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
    "publishConnectFailuresResult"
})
@XmlRootElement(name = "PublishConnectFailuresResponse")
public class PublishConnectFailuresResponse {

    @XmlElement(name = "PublishConnectFailuresResult")
    protected ArrayOfPublishConnectFailuresResult publishConnectFailuresResult;

    /**
     * Gets the value of the publishConnectFailuresResult property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfPublishConnectFailuresResult }
     *     
     */
    public ArrayOfPublishConnectFailuresResult getPublishConnectFailuresResult() {
        return publishConnectFailuresResult;
    }

    /**
     * Sets the value of the publishConnectFailuresResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfPublishConnectFailuresResult }
     *     
     */
    public void setPublishConnectFailuresResult(ArrayOfPublishConnectFailuresResult value) {
        this.publishConnectFailuresResult = value;
    }

}
