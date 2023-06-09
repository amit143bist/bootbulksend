
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
 *         &lt;element name="FolderFilter" type="{http://www.docusign.net/API/3.0}FolderFilter" minOccurs="0"/>
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
    "folderFilter"
})
@XmlRootElement(name = "GetFolderItems")
public class GetFolderItems {

    @XmlElement(name = "FolderFilter")
    protected FolderFilter folderFilter;

    /**
     * Gets the value of the folderFilter property.
     * 
     * @return
     *     possible object is
     *     {@link FolderFilter }
     *     
     */
    public FolderFilter getFolderFilter() {
        return folderFilter;
    }

    /**
     * Sets the value of the folderFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderFilter }
     *     
     */
    public void setFolderFilter(FolderFilter value) {
        this.folderFilter = value;
    }

}
