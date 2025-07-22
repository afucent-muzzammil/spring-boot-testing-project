package com.example.user_information.Controller;

import org.slf4j.Logger;


import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.user_information.ApiResponse;
import com.example.user_information.DTO.UserRegisterDTO;
import com.example.user_information.DTO.UserResponseDTO;
import com.example.user_information.Service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")

public class UserController {

	@Autowired
	private UserService userService;
	
	
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	
	
	@PostMapping
	public ResponseEntity<ApiResponse<UserResponseDTO>> addUser(@Valid @RequestBody UserRegisterDTO dto, HttpServletRequest request){
		return userService.addUser(dto, request);
		
	}
	
	
	@GetMapping
	public ResponseEntity<Page<UserResponseDTO>> getUsers(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "asc") String direction
			) {		
		
		logger.info("Fetching users: page = {}, size = {}, sortBy = {}, direction = {}", page, size, sortBy, direction);

		Sort sort = direction.equalsIgnoreCase("desc") ?
				Sort.by(sortBy).descending():
				Sort.by(sortBy).ascending();	
		
		Pageable pageable = PageRequest.of(page, size, sort);
		
		Page<UserResponseDTO> dto = userService.getUsers(pageable);
		
		logger.info("Successfully fetched {} users", dto.getTotalElements());
		
		return ResponseEntity.ok(dto);
	}	
	
	
}

