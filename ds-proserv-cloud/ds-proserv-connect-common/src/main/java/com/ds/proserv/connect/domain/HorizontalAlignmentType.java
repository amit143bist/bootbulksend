
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HorizontalAlignmentType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="HorizontalAlignmentType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Left"/>
 *     &lt;enumeration value="Right"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "HorizontalAlignmentType")
@XmlEnum
public enum HorizontalAlignmentType {

    @XmlEnumValue("Left")
    LEFT("Left"),
    @XmlEnumValue("Right")
    RIGHT("Right");
    private final String value;

    HorizontalAlignmentType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static HorizontalAlignmentType fromValue(String v) {
        for (HorizontalAlignmentType c: HorizontalAlignmentType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
