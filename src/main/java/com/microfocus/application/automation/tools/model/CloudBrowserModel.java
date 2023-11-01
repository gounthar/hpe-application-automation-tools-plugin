package com.microfocus.application.automation.tools.model;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

public class CloudBrowserModel implements Serializable {
    private String version;
    private String type;
    private String region;
    private String os;
    private String uftOneVersion;

    @DataBoundConstructor
    public CloudBrowserModel(String uftOneVersion, String cloudBrowserType, String cloudBrowserVersion, String cloudBrowserRegion, String cloudBrowserOs) {
        this.uftOneVersion = uftOneVersion;
        this.type = cloudBrowserType;
        this.version = cloudBrowserVersion;
        this.os = cloudBrowserOs;
        this.region = cloudBrowserRegion;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String getRegion() {
        return region;
    }

    public String getOs() {
        return os;
    }

    public String getUftOneVersion() {
        return uftOneVersion;
    }

}
