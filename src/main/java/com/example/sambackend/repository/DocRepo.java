package com.example.sambackend.repository;

import com.example.sambackend.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.print.Doc;
import java.util.List;

public interface DocRepo extends JpaRepository<Doctor,Integer> {

     @Query("select d from Doctor d where d.verificationCode = ?1")
     Doctor findByVerificationCode(String code);

     @Query(value = "select * from Doctor where enabled = true",nativeQuery = true)
     List<Doctor> findByEnabled();

     @Query("select d from Doctor d where d.username = ?1")
     Doctor findByUsername(String username);

     @Query("select d from Doctor d where d.username = ?1 and d.password = ?2")
     Doctor findByUsernameAndPassword(String username, String password);

     @Query("select d from Doctor d where d.mobile = ?1")
     Doctor findByMobile(String mobile);
}
