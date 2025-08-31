package com.example.user_information;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.user_information.Common.Status;
import com.example.user_information.Controller.UserController;
import com.example.user_information.DTO.UserRegisterDTO;
import com.example.user_information.DTO.UserResponseDTO;
import com.example.user_information.Exception.ResourceNotFoundException;
import com.example.user_information.Service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private ObjectMapper objectMapper;

    private UserRegisterDTO registerDto;
    private UserResponseDTO response;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();

        registerDto = new UserRegisterDTO(
                "John",
                "john@example.com",
                25,
                "Abcde@1244",
                "9874546123"
        );
        response = new UserResponseDTO(
                "John",
                "john@example.com",
                "9874546123"
        );
    }

    @Test
    public void testAddUSer_Success() throws Exception{
        when(userService.addUser(any(UserRegisterDTO.class))).thenReturn(
                new ApiResponse<>(201, Status.SUCCESS, "User Registered Successfully", response, null, null)
        );
        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("John"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"))
                .andExpect(jsonPath("$.data.phoneNo").value("9874546123"));
    }

    @Test
    public void testAddUSer_EmailAlreadyRegistered() throws Exception {
        when(userService.addUser(any(UserRegisterDTO.class)))
                .thenThrow(new IllegalArgumentException("Email already Registered"));
        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof IllegalArgumentException))
                .andExpect(result ->
                        assertEquals("Email already Registered",
                                result.getResolvedException().getMessage()));
    }

    @Test
    public void testAddUSer_PhoneNoAlreadyRegistered() throws Exception {
        when(userService.addUser(any(UserRegisterDTO.class)))
                .thenThrow(new IllegalArgumentException("Phone number already Registered"));
        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof IllegalArgumentException))
                .andExpect(result ->
                        assertEquals("Phone number already Registered",
                                result.getResolvedException().getMessage()));
    }

    @Test
    public void testGetAllUser_success() throws Exception {
        Page<UserResponseDTO> page = new PageImpl<>(List.of(response));

        when(userService.getUsers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/user?page=0&size=10&sortBy=id&direction=asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John"))
                .andExpect(jsonPath("$.content[0].email").value("john@example.com"))
                .andExpect(jsonPath("$.content[0].phoneNo").value("9874546123"));
    }

    @Test
    public void testGetAllUser_Failed() throws Exception{
        when(userService.getUsers(any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("No USer found."));
        mockMvc.perform(get("/user")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "id")
                .param("direction", "asc"))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof ResourceNotFoundException))
                .andExpect(result ->
                        assertEquals("No USer found.", result.getResolvedException().getMessage()));
    }

    @Test
    public void testGetUserById_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    public void testGetUserById_Failed() throws Exception{
        when(userService.getUserById(2L))
                .thenThrow(new ResourceNotFoundException("User Not Found With Id 2"));

        mockMvc.perform(get("/user/2"))
                .andExpect(status().isNotFound())
                .andExpect(result -> result.getResolvedException().getMessage()
                        .equals("User Not Found With Id 2"));
    }

    @Test
    public void testUpdateUser_Success() throws Exception {
        when(userService.updateUser(eq(1L), any(UserRegisterDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/user/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    public void testUpdateUser_NotFound() throws Exception {
        when(userService.updateUser(eq(2L), any(UserRegisterDTO.class)))
                .thenThrow(new ResourceNotFoundException("User not found with Id 2"));

        mockMvc.perform(put("/user/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteUser_Success() throws Exception{
        doNothing().when(userService).deleteUserById(1L);

        mockMvc.perform(delete("/user/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteUser_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("User not found with Id 2"))
                .when(userService).deleteUserById(2L);

        mockMvc.perform(delete("/user/2"))
                .andExpect(status().isNotFound());
    }

}
