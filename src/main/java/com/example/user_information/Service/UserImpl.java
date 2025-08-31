package com.example.user_information.Service;

import java.time.LocalDateTime;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.user_information.ApiResponse;
import com.example.user_information.Common.Status;
import com.example.user_information.DTO.UserRegisterDTO;
import com.example.user_information.DTO.UserResponseDTO;
import com.example.user_information.Entity.User;
import com.example.user_information.Exception.ResourceNotFoundException;
import com.example.user_information.Repository.UserRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j // -> Simple Logging Facade for Java.
@Service
@RequiredArgsConstructor
public class UserImpl implements UserService {

    private final UserRepo userRepo;

    @Override
    public ApiResponse<UserResponseDTO> addUser(UserRegisterDTO dto) {

        log.trace("Entering addUser method with parameters: {}", dto);

        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (userRepo.existsByPhoneNo(dto.getPhoneNo())) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        User user = new User();

        user.setName(dto.getName());
        user.setAge(dto.getAge());
        user.setEmail(dto.getEmail());
        user.setPhoneNo(dto.getPhoneNo());
        user.setPassword(dto.getPassword());

        log.info("User Registered Successfully{}" , dto);

        userRepo.save(user);

        UserResponseDTO responseDto = new UserResponseDTO(user.getName(), user.getEmail(), user.getPhoneNo());

        return new ApiResponse<>(
                201,
                Status.SUCCESS,
                "User registered successfully",
                responseDto,
                null,
                LocalDateTime.now()
        );
    }


    @Override
    public Page<UserResponseDTO> getUsers(Pageable pageable){
    	
    	Page<User> userPage = userRepo.findAll(pageable);
    	
    	if (userPage.isEmpty()) {
    		log.warn("User not found");
            throw new ResourceNotFoundException("No users found.");
    	}
    	Page<UserResponseDTO> dto = userPage.map(user -> new UserResponseDTO(
    			user.getName(),
    			user.getEmail(),
    			user.getPhoneNo()
    			));
    	log.info("User get Successfully: {}", userPage);
    	
    	return dto;

    	}

    @Override
    public UserResponseDTO getUserById(Long id) {
         User user = userRepo.findById(id)
                 .orElseThrow(() -> {
                     log.warn("User Not Found with ID");
                     return new ResourceNotFoundException("User Not Found with ID" + id);
                 });
         UserResponseDTO response = new UserResponseDTO(
                 user.getName(),
                 user.getEmail(),
                 user.getPhoneNo()
         );
         log.info("User Fetched Successfully{}", response);
         return response;
    }
    
    @Override
    public UserResponseDTO updateUser(Long id, UserRegisterDTO dto) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Id" + id));

        user.setName(dto.getName());
        user.setAge(dto.getAge());
        user.setEmail(dto.getEmail());
        user.setPhoneNo(dto.getPhoneNo());
        user.setPassword(dto.getPassword());

        userRepo.save(user);
        log.info("User Update Successfully: {}", user);

        return new UserResponseDTO(user.getName(), user.getEmail(), user.getPhoneNo());
    }


    @Override
    public void deleteUserById(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Id" + id));
        userRepo.delete(user);
        log.info("User deleted Successfully: Id {}", id);
    }

    
}    	
    

