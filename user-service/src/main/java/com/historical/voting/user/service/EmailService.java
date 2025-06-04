package com.historical.voting.user.service;


public interface EmailService {
    public boolean verifyCode(String email, String code);
    public void sendVerificationCode(String email);


}