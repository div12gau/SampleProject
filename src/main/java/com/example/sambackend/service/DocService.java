package com.example.sambackend.service;

import com.example.sambackend.entity.Doctor;
import com.example.sambackend.entity.Signup;
import com.example.sambackend.entity.SmsPojo;
import com.example.sambackend.repository.DocRepo;
import javax.mail.internet.MimeMessage;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.print.Doc;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DocService {

    @Autowired
    private DocRepo docRepo;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private OTPService otpService;

    @Autowired
    private SMSService smsService;

    @Autowired
    private SimpMessagingTemplate webSocket;

    private final String TOPIC_DESTINATION = "/lesson/sms";

    public Doctor saveDoc(Doctor doctor) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(doctor.getPassword());
        doctor.setPassword(encodedPassword);
        return docRepo.save(doctor);
    }

    public void registerUsingEmail(Doctor doctor) throws Exception {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(doctor.getPassword());
        doctor.setPassword(encodedPassword);
        String randomCode = RandomString.make(64);
        doctor.setVerificationCode(randomCode);
        doctor.setEnabled(false);
        docRepo.save(doctor);
        sendVerification(doctor);
    }

    public void smsSubmit(@RequestBody SmsPojo sms) {
        try {
            smsService.send(sms);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        webSocket.convertAndSend(TOPIC_DESTINATION,getTimeStamp() + ": SMS has been sent!: "+sms.getTo());

    }

    private String getTimeStamp() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
    }

    public String registerUsingMobileOTP(Doctor doctor) throws Exception {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodePassword = passwordEncoder.encode(doctor.getPassword());
        doctor.setPassword(encodePassword);
        doctor.setEnabled(false);
        String randomCode = RandomString.make(64);
        doctor.setVerificationCode(randomCode);
        docRepo.save(doctor);
        String mobile = doctor.getMobile();
        int otp = otpService.generateOTP(randomCode);
        SmsPojo smsPojo = new SmsPojo();
        smsPojo.setTo(mobile);
        smsPojo.setMessage(String.valueOf(otp));
        smsSubmit(smsPojo);
        return randomCode;
    }

    public boolean validateOTP(Signup request) {
        String key = request.getToken();
        int otp = request.getOtp();
        int OTP = otpService.getOTP(key);
        if(OTP == otp) {
            Doctor doctor = docRepo.findByVerificationCode(key);
            doctor.setVerificationCode(null);
            docRepo.save(doctor);
            String username = doctor.getMobile().substring(3);
            doctor.setUsername(username);
            otpService.clearOTP(key);
            doctor.setEnabled(true);
            docRepo.save(doctor);
            return true;
        }
        else {
            return false;
        }
    }

    private void sendVerification(Doctor doctor) throws Exception {
        String toAddress = doctor.getEmail();
        String fromAddress = "srivastavg826@gmail.com";
        String senderName = "The verification mail is from DocLine";
        String subject = "Please verify your registration ";
        String content = "Dear [[name]],<br>"+
                "Please click the link below to verify your registration: <br>"+
                "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"+
                "Thank you, <br>"+
                "DocLine";
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(fromAddress,senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);
        content = content.replace("[[name]]",doctor.getFirstName());
        String verifyURL ="http://localhost:8080/register/verify?code=" + doctor.getVerificationCode();
        content = content.replace("[[URL]]",verifyURL);
        helper.setText(content,true);
        mailSender.send(message);
        System.out.println("The email has been sent");



    }

    public boolean verify(String code) {
        Doctor doctor = docRepo.findByVerificationCode(code);
        if(doctor == null || doctor.isEnabled()) {
            return false;
        }
        else
        {
            doctor.setVerificationCode(null);
            doctor.setEnabled(true);
            docRepo.save(doctor);
            return true;
        }
    }

    public List<Doctor> getDoctors() {
        return docRepo.findByEnabled();
    }

    public Doctor getDocById(int id) {
        return docRepo.findById(id).orElse(null);
    }

    public Doctor updateDoctor(Doctor doctor) {
        int id = doctor.getId();
        Doctor doc = docRepo.findById(id).orElse(null);
        doc.setFirstName(doctor.getFirstName());
        doc.setLastName(doctor.getLastName());
        doc.setGender(doctor.getGender());
        doc.setAge(doctor.getAge());
        doc.setMobile(doctor.getMobile());
        doc.setLicenseID(doctor.getLicenseID());
        return docRepo.save(doc);
    }

    public Doctor loadDoctorByUsername(String username) {
        Doctor doctor = docRepo.findByUsername(username);
        return doctor;
    }

    public Doctor loadDoctorByUsernameAndPassword(String username,String password) {
        Doctor doctor = docRepo.findByUsernameAndPassword(username,password);
        return doctor;
    }
}
