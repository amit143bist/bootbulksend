
package com.ds.proserv.connect.domain;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfEnvelopeAuditEvent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfEnvelopeAuditEvent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="EnvelopeAuditEvent" type="{http://www.docusign.net/API/3.0}EnvelopeAuditEvent" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfEnvelopeAuditEvent", propOrder = {
    "envelopeAuditEvent"
})
public class ArrayOfEnvelopeAuditEvent {

    @XmlElement(name = "EnvelopeAuditEvent", nillable = true)
    protected List<EnvelopeAuditEvent> envelopeAuditEvent;

    /**
     * Gets the value of the envelopeAuditEvent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the envelopeAuditEvent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEnvelopeAuditEvent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EnvelopeAuditEvent }
     * 
     * 
     */
    public List<EnvelopeAuditEvent> getEnvelopeAuditEvent() {
        if (envelopeAuditEvent == null) {
            envelopeAuditEvent = new ArrayList<EnvelopeAuditEvent>();
        }
        return this.envelopeAuditEvent;
    }

}
