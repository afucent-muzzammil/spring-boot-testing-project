package com.example.user_information;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.user_information.Common.Status;
import com.example.user_information.DTO.UserRegisterDTO;
import com.example.user_information.DTO.UserResponseDTO;
import com.example.user_information.Entity.User;
import com.example.user_information.Repository.UserRepo;
import com.example.user_information.Service.UserImpl;

import jakarta.servlet.http.HttpServletRequest;

public class UserServiceTest {

	@InjectMocks
	private UserImpl userImpl;
		
	@Mock
	private UserRepo userRepo;	
	
	@Mock
	private HttpServletRequest request;
	
	private UserRegisterDTO dto;
	
	
	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		
		dto = new UserRegisterDTO(
				"Qais Khan",
				"qais123@example.com",
				25,
				"Password@786",
				"9876523430"
				);		
	}
	
	
	@Test
	public void testAddUser_Success() {
		
		when(userRepo.existsByEmail(dto.getEmail())).thenReturn(false);
		when(userRepo.existsByPhoneNo(dto.getPhoneNo())).thenReturn(false);
		
		User savedUser = new User();
		savedUser.setId(1);
		savedUser.setName(dto.getName());
		savedUser.setEmail(dto.getEmail());
		savedUser.setAge(dto.getAge());
		savedUser.setPassword(dto.getPassword());
		savedUser.setPhoneNo(dto.getPhoneNo());
		
		when(userRepo.save(any(User.class))).thenReturn(savedUser);
		
		when(request.getRequestURI()).thenReturn("/user");
		
		ResponseEntity<ApiResponse<UserResponseDTO>> response = userImpl.addUser(dto, request);
		
		assertEquals(HttpStatus.CREATED, response.getStatusCode());		
		assertEquals(Status.SUCCESS, response.getBody().getStatus());
		assertNotNull(response.getBody().getData());
		assertEquals(savedUser.getEmail(), response.getBody().getData().getEmail());
		verify(userRepo, times(1)).save(any(User.class));
	}
	
	

	@Test
	public void testAddUser_EmailAlreadyExist() {
		
	    // Mocks
	    when(userRepo.existsByEmail(dto.getEmail())).thenReturn(true);
	    

	    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
	        userImpl.addUser(dto, request);
	    });
	    
	    //System.out.println("Assertion message: " + ex.getMessage());
	    assertEquals("Email already registered: ", ex.getMessage());

	    verify(userRepo, times(0)).save(any(User.class));
	}

	
	@Test
	public void testAddUser_phoneNoAlreadyExist() {
		when(userRepo.existsByPhoneNo(dto.getPhoneNo())).thenReturn(true);
		
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
			userImpl.addUser(dto, request);
		});
		
		assertEquals("Phone number already registered: ", ex.getMessage());
		
		verify(userRepo, times(0)).save(any(User.class));
	}
	
	
	@Test
	public void getUser_findAll() {
		
		
		User savedUser = new User();
		savedUser.setName("Qais Khan");
		savedUser.setEmail("qais123@example.com");
		savedUser.setPhoneNo("9876523430");
		
		List<User> users = List.of(savedUser);
		Page<User> userPage = new PageImpl<>(users);
		
		Pageable pageable = PageRequest.of(0, 10);
		
		
		
		when(userRepo.findAll(pageable)).thenReturn(userPage);
		
		Page<UserResponseDTO> response = userImpl.getUsers(pageable);
						
		assertEquals(1, response.getContent().size());
		assertEquals("Qais Khan", response.getContent().get(0).getName());
		
		verify(userRepo, times(1)).findAll(pageable);
		
	}
	
	
}
