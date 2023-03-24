
package com.ds.proserv.connect.domain;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AnchorTabProcessorVersion.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="AnchorTabProcessorVersion">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="V1_0"/>
 *     &lt;enumeration value="V1_1"/>
 *     &lt;enumeration value="V1_2"/>
 *     &lt;enumeration value="V1_3"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "AnchorTabProcessorVersion")
@XmlEnum
public enum AnchorTabProcessorVersion {

    @XmlEnumValue("V1_0")
    V_1_0("V1_0"),
    @XmlEnumValue("V1_1")
    V_1_1("V1_1"),
    @XmlEnumValue("V1_2")
    V_1_2("V1_2"),
    @XmlEnumValue("V1_3")
    V_1_3("V1_3");
    private final String value;

    AnchorTabProcessorVersion(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AnchorTabProcessorVersion fromValue(String v) {
        for (AnchorTabProcessorVersion c: AnchorTabProcessorVersion.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
