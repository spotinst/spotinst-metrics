package com.spotinst.metrics.dal.models.core.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DbAccount {

    private String     id;
    private String     name;
    private String     preferences;
    private BigInteger organization;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public BigInteger getOrganization() {
        return organization;
    }

    public void setOrganization(BigInteger organization) {
        this.organization = organization;
    }
}
