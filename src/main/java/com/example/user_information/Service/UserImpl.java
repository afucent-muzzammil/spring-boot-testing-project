package com.example.user_information.Service;

import java.time.LocalDateTime;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.user_information.ApiResponse;
import com.example.user_information.Common.Status;
import com.example.user_information.DTO.UserRegisterDTO;
import com.example.user_information.DTO.UserResponseDTO;
import com.example.user_information.Entity.User;
import com.example.user_information.Exception.ResourceNotFoundException;
import com.example.user_information.Repository.UserRepo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j // -> Simple Logging Facade for Java.
@Service
@Transactional

public class UserImpl implements UserService {
	
    @Autowired
    private UserRepo userRepo;
    
    
    @Override
    public ResponseEntity<ApiResponse<UserResponseDTO>> addUser(UserRegisterDTO dto, HttpServletRequest request) {

    	// TRACE: Log method entry, especially if you want to track the flow
        log.trace("Entering addUser method with parameters: {}", dto);

        
        // Check if the email already exists
        if (userRepo.existsByEmail(dto.getEmail())) {
        	log.warn("Email {} already registered: {}", dto.getEmail());
        	throw new IllegalArgumentException("Email already registered: ");	// -> Throw error if email is already registered
        }

        // Check if the phone number already exists
        if (userRepo.existsByPhoneNo(dto.getPhoneNo())) {
        	log.warn("Phone number {} already registered: {}", dto.getPhoneNo());
            throw new IllegalArgumentException("Phone number already registered: ");  // Throw error if phone number is already registered
        }

        // If validations pass, create a new User entity
        User user = new User();
        user.setName(dto.getName());
        user.setAge(dto.getAge());
        user.setEmail(dto.getEmail());
        user.setPhoneNo(dto.getPhoneNo());
        user.setPassword(dto.getPassword());

        log.info("User Registered Successfully: {}" , dto);
        
        // Save the new user to the database
        userRepo.save(user);

        // Prepare the response DTO to return (excluding sensitive information like password)
        UserResponseDTO responseDto = new UserResponseDTO(
                user.getName(),
                user.getEmail(),
                user.getPhoneNo()
        );

        
        // Build the ApiResponse to return
        ApiResponse<UserResponseDTO> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),  // Status 201 (Created)
                Status.SUCCESS,              // Success status
                "User registered successfully", // Message indicating success
                responseDto,                 // Data to return (user details)
                request.getRequestURI(),     // Path of the request
                LocalDateTime.now()          // Timestamp for when the request was made
        );

        // Return the response with HTTP status 201 (Created)
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    
    
    
    @Override
    public Page<UserResponseDTO> getUsers(Pageable pageable){
    	
    	// -> Step 1: Fetch paginated list of User entities from the database
    	Page<User> userPage = userRepo.findAll(pageable);
    	
    	if (userPage.isEmpty()) {
    		log.warn("User not found");
            throw new ResourceNotFoundException("No users found.");
    	}
    	// -> Step 2: Convert each User entity to a UserResponseDTO using map()
    	Page<UserResponseDTO> dto = userPage.map(user -> new UserResponseDTO(
    			user.getName(),
    			user.getEmail(),
    			user.getPhoneNo()
    			));
    	
    	log.info("User get Successfully: {}");
    	
    	return dto;
        
    	
    	}
    
    
    
}    	
    

