package com.alya.aplikasilansia.ui.healthcare;

public class HealthCare {
    private String name;
    private String address;
    private String city;
    private String url;

    public HealthCare(String name, String address, String city, String url) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getUrl() {
        return url;
    }

}
