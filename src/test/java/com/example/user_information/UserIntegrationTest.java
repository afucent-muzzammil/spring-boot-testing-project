package com.example.user_information;

import com.example.user_information.Entity.User;
import com.example.user_information.Repository.UserRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private User userJohn;
    private User userAlice;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();

        userJohn = new User(null, "John", 24, "john@example.com", "987676543", "Abcd@1224");
        userAlice = new User(null, "Alice", 23, "alice@example.com", "9876543212", "Pass@1234");

        userRepo.save(userJohn);
    }

    @Test
    void testAddUser_Success() throws Exception {
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userAlice)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Alice"))
                .andExpect(jsonPath("$.data.email").value("alice@example.com"));
    }

    @Test
    void testAddUser_DuplicateEmail() throws Exception{
        User newUser = new User(null, "Doe", 30, "john@example.com", "8897564321", "Pass@12343");

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"))
                .andExpect(jsonPath("$.message").value("Business validation failed"))
                .andExpect(jsonPath("$.data[0]").value("Email already registered"));
    }

    @Test
    void testGetAllUser_Success() throws Exception{
        mockMvc.perform(get("/user")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("john@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testGetUserById() throws Exception {
        mockMvc.perform(get("/user/{id}", userJohn.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.phoneNo").value("987676543"));
    }

    @Test
    void testUpdateUser() throws Exception {
        userJohn.setName("John Doe");

        mockMvc.perform(put("/user/{id}", userJohn.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userJohn)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.phoneNo").value("987676543"));
    }

    @Test
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/user/{id}", userJohn.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/user/{id}", userJohn.getId()))
                .andExpect(status().isNotFound());
    }


}
