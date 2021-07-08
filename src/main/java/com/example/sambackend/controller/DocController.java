package com.example.sambackend.controller;

import com.example.sambackend.entity.*;
import com.example.sambackend.service.DocService;
import com.example.sambackend.service.OTPService;
import com.example.sambackend.service.SMSService;
import com.example.sambackend.utility.JWTUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class DocController {

    @Autowired
    private JWTUtility jwtUtility;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private DocService docService;

    @Autowired
    SMSService smsService;

    @Autowired
    private SimpMessagingTemplate webSocket;

    @Autowired
    private OTPService otpService;

    private final String TOPIC_DESTINATION = "/lesson/sms";

    @PostMapping("/addDoctor")
    public Doctor addDoctor(@RequestBody Doctor doctor) {
        return docService.saveDoc(doctor);
    }

    private String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(),"");
    }

    @PostMapping("/register")
    public String processRegister(@RequestBody Doctor doctor) throws Exception {
        return docService.registerUsingMobileOTP(doctor);
    }

    @GetMapping("/register/verify")
    public String verifyDoctor(@Param("code") String code) {
        if(docService.verify(code)) {
            return "Verification Successfull";
        }
        else {
            return "Verification failed. ";
        }
    }

    @PostMapping("/authenticate")
    public JwtResponse authenticate(@RequestBody JwtRequest jwtRequest) throws Exception {
        try {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String password = passwordEncoder.encode(jwtRequest.getPassword());
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(jwtRequest.getUsername(),jwtRequest.getPassword()));
        }
        catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }

        final Doctor userDetails = docService.loadDoctorByUsername(jwtRequest.getUsername());

        final String token = jwtUtility.generateToken(userDetails);

        return new JwtResponse(token);
    }

    @PostMapping("/register/validateOTP")
    public String validateOTP(@RequestBody Signup request) {
        String SUCCESS = "OTP verification is successfull.";
        String FAIL = "Invalid OTP";
        if(docService.validateOTP(request)){
            return SUCCESS;
        }
        else {
            return FAIL;
        }
    }

    @PostMapping("/sms")
    public void smsSubmit(@RequestBody SmsPojo sms) {
        try {
            smsService.send(sms);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        webSocket.convertAndSend(TOPIC_DESTINATION,getTimeStamp() + ": SMS has been sent!: "+sms.getTo());

    }

    @PostMapping("/smscallback")
    public void smsCallback(@RequestBody MultiValueMap<String,String> map) {
        smsService.receive(map);
        webSocket.convertAndSend(TOPIC_DESTINATION,getTimeStamp() + ": Twilio has made a callback request");
    }

    private String getTimeStamp() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
    }

    @GetMapping("/doctors")
    public List<Doctor> findAllDoctors() {
        return docService.getDoctors();
    }

    @GetMapping("doctor/{id}")
    public Doctor findDoctorById(@PathVariable int id) {
        return docService.getDocById(id);
    }

    @PostMapping("/updateDoctor")
    public Doctor updateDoctor(@RequestBody Doctor doctor) {
        return docService.updateDoctor(doctor);
    }
}
