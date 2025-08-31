package com.example.user_information;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.example.user_information.Exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.user_information.DTO.UserRegisterDTO;
import com.example.user_information.DTO.UserResponseDTO;
import com.example.user_information.Entity.User;
import com.example.user_information.Repository.UserRepo;
import com.example.user_information.Service.UserImpl;

public class UserServiceTest {

	@InjectMocks
	private UserImpl userImpl;
		
	@Mock
	private UserRepo userRepo;

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
		savedUser.setId(1L);
		savedUser.setName(dto.getName());
		savedUser.setEmail(dto.getEmail());
		savedUser.setAge(dto.getAge());
		savedUser.setPassword(dto.getPassword());
		savedUser.setPhoneNo(dto.getPhoneNo());
		
		when(userRepo.save(any(User.class))).thenReturn(savedUser);

		ApiResponse<UserResponseDTO> response = userImpl.addUser(dto);

        assertNotNull(response);
        assertEquals(201, response.getCode());
        assertEquals("User registered successfully", response.getMessage());
        assertEquals(dto.getEmail(), response.getData().getEmail());
        assertEquals(dto.getName(), response.getData().getName());
        verify(userRepo, times(1)).save(any(User.class));
	}
	
	

	@Test
	public void testAddUser_EmailAlreadyExist() {

	    when(userRepo.existsByEmail(dto.getEmail())).thenReturn(true);

	    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userImpl.addUser(dto));

        assertTrue(ex.getMessage().contains("Email already registered"));

        verify(userRepo, times(0)).save(any(User.class));
	}


	@Test
	public void testAddUser_phoneNoAlreadyExist() {
		when(userRepo.existsByPhoneNo(dto.getPhoneNo())).thenReturn(true);
		
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userImpl.addUser(dto));

        assertTrue(ex.getMessage().contains("Phone number already registered"));

		verify(userRepo, times(0)).save(any(User.class));
	}
	
    @Test
    public void testGetUser_Success(){
        User user = new User(1L, "John Doe", 26 , "john@example.com"  , "9874563210" , "Abcd@1234");
        Page<User> page = new PageImpl<>(List.of(user));
        Pageable pageable = PageRequest.of(0 , 10);

        when(userRepo.findAll(pageable)).thenReturn(page);

        Page<UserResponseDTO> response = userImpl.getUsers(pageable);

        assertEquals(1 , response.getTotalElements());
        assertEquals("john@example.com" , response.getContent().get(0).getEmail());
    }

    @Test
    public void test_GetUserFailed() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> emptyPage = new PageImpl<>(List.of());
        when(userRepo.findAll(pageable)).thenReturn(emptyPage);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> userImpl.getUsers(pageable));
        assertEquals("No users found.", ex.getMessage());
    }
	
	@Test
    public void test_GetUserById() {

        User user = new User(1L, "John Doe", 26, "john@example.com", "9874563210" , "Abcd@1234");
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDTO response = userImpl.getUserById(1L);

        assertNotNull(response);
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com" , response.getEmail());
        assertEquals("9874563210", response.getPhoneNo());

        verify(userRepo, times(1)).findById(1L);
    }

    @Test
    public void test_getUserByIdFailed() {
        Long userId = 2L;

        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> userImpl.getUserById(userId));

        assertEquals("User Not Found with ID" + userId, ex.getMessage());
        verify(userRepo, times(1)).findById(userId);
    }

    @Test
    public void test_UpdateUserSuccess() {
        Long userId = 1L;
        User user = new User(userId, "Fazil khan", 23, "fazil@example.com", "9786541230", "Pass@12345");

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        UserRegisterDTO dto = new UserRegisterDTO("Qais Khan", "qais@example.com" , 24, "Asdfgh@123", "9874546123");

        UserResponseDTO response = userImpl.updateUser(userId, dto);

        assertEquals("Qais Khan" , response.getName());
        assertEquals("qais@example.com" , response.getEmail());
        assertEquals("9874546123" , response.getPhoneNo());

        verify(userRepo, times(1)).save(user);
    }

    @Test
    public void test_UpdateUserFailed() {
        Long userId = 2L;
        UserRegisterDTO dto = new UserRegisterDTO("Qais Khan", "qais@example.com", 24 , "Asdfgh@123", "9874546123");

        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> userImpl.updateUser(userId, dto));

        assertEquals("User not found with Id" + userId, ex.getMessage());
    }

    @Test
    public void test_deleteUserSuccess() {
        Long userId = 1L;
        User user = new User(userId, "John", 26, "john@example.com" , "9874546123" , "Pass@1234");

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        userImpl.deleteUserById(userId);

        verify(userRepo, times(1)).delete(user);
    }

    @Test
    public void test_deleteUserFailed() {
        Long userId = 2L;

        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> userImpl.deleteUserById(userId));

        assertEquals("User not found with Id" + userId, ex.getMessage());
    }


}
