package com.cuponation.android.model;

/**
 * Created by goran on 9/1/17.
 */

public class CountryConfig {

    private String countryCode;
    private String countryName;
    private String clientId;
    private Integer flagResourceId;
    private String dbFilePrefix;
    private String baseUrl;
    private String buttonsPubRef;

    public CountryConfig(String countryCode, String countryName, String countryClientId, Integer flagResourceId, String dbFilePrefix, String baseUrl, String buttonsPubRef) {
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.clientId = countryClientId;
        this.flagResourceId = flagResourceId;
        this.dbFilePrefix = dbFilePrefix;
        this.baseUrl = baseUrl;
        this.buttonsPubRef = buttonsPubRef;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Integer getFlagResourceId() {
        return flagResourceId;
    }

    public void setFlagResourceId(Integer flagResourceId) {
        this.flagResourceId = flagResourceId;
    }

    public String getDbFilePrefix() {
        return dbFilePrefix;
    }

    public void setDbFilePrefix(String dbFilePrefix) {
        this.dbFilePrefix = dbFilePrefix;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getButtonsPubRef() {
        return buttonsPubRef;
    }

    public void setButtonsPubRef(String buttonsPubRef) {
        this.buttonsPubRef = buttonsPubRef;
    }
}
