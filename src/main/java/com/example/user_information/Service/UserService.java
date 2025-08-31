package com.example.user_information.Service;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.example.user_information.ApiResponse;
import com.example.user_information.DTO.UserRegisterDTO;
import com.example.user_information.DTO.UserResponseDTO;

import jakarta.servlet.http.HttpServletRequest;

public interface UserService {

    ApiResponse<UserResponseDTO> addUser(UserRegisterDTO dto);

    Page<UserResponseDTO> getUsers(Pageable pageable);

    UserResponseDTO getUserById(Long id);

    UserResponseDTO updateUser(Long id, UserRegisterDTO dto);

    void deleteUserById(Long id);

}
