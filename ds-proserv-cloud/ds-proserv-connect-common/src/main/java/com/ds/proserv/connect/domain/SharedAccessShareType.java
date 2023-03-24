
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SharedAccessShareType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SharedAccessShareType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="NotShared"/>
 *     &lt;enumeration value="SharedFrom"/>
 *     &lt;enumeration value="SharedTo"/>
 *     &lt;enumeration value="SharedToAndFrom"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SharedAccessShareType")
@XmlEnum
public enum SharedAccessShareType {

    @XmlEnumValue("NotShared")
    NOT_SHARED("NotShared"),
    @XmlEnumValue("SharedFrom")
    SHARED_FROM("SharedFrom"),
    @XmlEnumValue("SharedTo")
    SHARED_TO("SharedTo"),
    @XmlEnumValue("SharedToAndFrom")
    SHARED_TO_AND_FROM("SharedToAndFrom");
    private final String value;

    SharedAccessShareType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SharedAccessShareType fromValue(String v) {
        for (SharedAccessShareType c: SharedAccessShareType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
