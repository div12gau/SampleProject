package com.example.sambackend.service;

import com.example.sambackend.entity.SmsPojo;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

@Component
public class SMSService {
    private final String ACCOUNT_SID = "ACba49c253228679f937f74a251b36f5ad";
    private final String AUTH_TOKEN = "e3efdd1dd636429af4ed734ea17945e6";
    private final String FROM_NUMBER = "+14805684016";

    public void send(SmsPojo sms) {
        Twilio.init(ACCOUNT_SID,AUTH_TOKEN);
        Message message = Message.creator(new PhoneNumber(sms.getTo()), new PhoneNumber(FROM_NUMBER), sms.getMessage()).create();
        System.out.println("Here is my id: "+message.getSid());
    }

    public void receive(MultiValueMap<String,String> smsCallback) {

    }
}
