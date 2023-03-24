package com.ds.proserv.common.constant;

public enum TemplateRole {

    LANDLORD("landlord"),
    TENANT("tenant");

    public final String label;

    private TemplateRole(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
