package com.example.sambackend.entity;

import java.io.Serializable;

public class Signup implements Serializable {

    private String token;
    private Integer otp;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getOtp() {
        return otp;
    }

    public void setOtp(Integer otp) {
        this.otp = otp;
    }
}
