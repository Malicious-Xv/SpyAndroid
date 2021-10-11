package com.example.spyapp;

import java.util.List;

public class SpyModel {
    private String version;
    private int sdk;
    private int battery;
    private long memory;

    private List<String> running;
    private List<String> installed;
    private List<String> accounts;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getSdk() {
        return sdk;
    }

    public void setSdk(int sdk) {
        this.sdk = sdk;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public long getMemory() {
        return memory;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

    public List<String> getRunning() {
        return running;
    }

    public void setRunning(List<String> running) {
        this.running = running;
    }

    public List<String> getInstalled() {
        return installed;
    }

    public void setInstalled(List<String> installed) {
        this.installed = installed;
    }

    public List<String> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<String> accounts) {
        this.accounts = accounts;
    }
}
