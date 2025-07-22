package com.example.user_information.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.user_information.Entity.User;

@Repository

public interface UserRepo extends JpaRepository<User, Long> {
	
    boolean existsByEmail(String email);
    
    boolean existsByPhoneNo(String phoneNo);


}
