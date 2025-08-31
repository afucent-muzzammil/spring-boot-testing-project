package com.example.user_information;

import com.example.user_information.Entity.User;
import com.example.user_information.Repository.UserRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerServiceTopDownTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepo userRepo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User userJohn;
    private User userAlice;

    @BeforeEach
    void setUp() {
        userJohn = addUser(1L, "John" ,24, "john@example.com", "9876543210", "Abcd@123");
        userAlice = addUser(2L, "Alice", 23, "alice@example.com", "9876754323", "Pass@123");
    }


    private User addUser(Long id, String name, int age, String email, String phoneNo, String password) {
        return new User(id, name, age, email, phoneNo, password);
    }


    @Test
    public void testGetUserById_Success() throws Exception {
        when(userRepo.findById(1L))
                .thenReturn(Optional.of(userJohn));

        mockMvc.perform(get("/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.phoneNo").value("9876543210"));
    }

    @Test
    public void testGetUserById_NotFound() throws Exception {
        when(userRepo.findById(3L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/user/3"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resources Not Found"))
                .andExpect(jsonPath("$.data[0]").value("User Not Found with ID3"));
    }

    @Test
    public void testAddUser_Success() throws Exception {
        when(userRepo.existsByEmail(userAlice.getEmail())).thenReturn(false);
        when(userRepo.existsByPhoneNo(userAlice.getPhoneNo())).thenReturn(false);
        when(userRepo.save(any(User.class))).thenReturn(userAlice);

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userAlice)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Alice"))
                .andExpect(jsonPath("$.data.email").value("alice@example.com"));
    }

    @Test
    public void testUpdateUser_Success() throws Exception {
        User updateUser = addUser(2L, "Alice Khan", 24, "alice13@example.com", "9988774563", "Abcde@1234");

        when(userRepo.findById(2L)).thenReturn(Optional.of(userAlice));
        when(userRepo.save(any(User.class))).thenReturn(updateUser);

        mockMvc.perform(put("/user/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Khan"));
    }

    @Test
    public void testDeleteUser() throws Exception {
        when(userRepo.findById(1L)).thenReturn(Optional.of(userJohn));
        doNothing().when(userRepo).delete(userJohn);

        mockMvc.perform(delete("/user/1"))
                .andExpect(status().isNoContent());
        verify(userRepo, times(1)).delete(userJohn);
    }

    @Test
    public void testGetAllUser_Success() throws Exception {
        when(userRepo.findAll(PageRequest.of(0, 10, Sort.by("id").ascending())))
                .thenReturn(new PageImpl<>(List.of(userJohn)));

        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John"));
    }

}
