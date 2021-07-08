package com.example.sambackend.service;

import com.example.sambackend.entity.Doctor;
import com.example.sambackend.repository.DocRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class DoctorService implements UserDetailsService {

    @Autowired
    private DocRepo docRepo;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Doctor doctor = docRepo.findByUsername(username);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodePassword = passwordEncoder.encode(doctor.getPassword());
        return new org.springframework.security.core.userdetails.User(doctor.getUsername(),doctor.getPassword(),new ArrayList<>());
    }


}
