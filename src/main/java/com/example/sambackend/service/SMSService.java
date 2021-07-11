package com.example.sambackend.service;

import com.example.sambackend.entity.SmsPojo;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

@Component
public class SMSService {
    private final String ACCOUNT_SID = "ACa24be5620755478dfb23813b78a33317";
    private final String AUTH_TOKEN = "c7b47961aacc947d2dfee3f18c91fff1";
    private final String FROM_NUMBER = "+18622596159";

    public void send(SmsPojo sms) {
        Twilio.init(ACCOUNT_SID,AUTH_TOKEN);
        Message message = Message.creator(new PhoneNumber(sms.getTo()), new PhoneNumber(FROM_NUMBER), sms.getMessage()).create();
        System.out.println("Here is my id: "+message.getSid());
    }

    public void receive(MultiValueMap<String,String> smsCallback) {

    }
}
