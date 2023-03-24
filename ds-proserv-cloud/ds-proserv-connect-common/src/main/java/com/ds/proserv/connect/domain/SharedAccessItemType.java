
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SharedAccessItemType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SharedAccessItemType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Envelopes"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SharedAccessItemType")
@XmlEnum
public enum SharedAccessItemType {

    @XmlEnumValue("Envelopes")
    ENVELOPES("Envelopes");
    private final String value;

    SharedAccessItemType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SharedAccessItemType fromValue(String v) {
        for (SharedAccessItemType c: SharedAccessItemType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
