package me.kagura.enums;

public enum PropertiesEnum {

    LOGININFO_TIMEOUT("jjsoup.logininfo.timeout");

    private String value;

    PropertiesEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
