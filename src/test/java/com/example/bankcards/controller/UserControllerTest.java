package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Test
    void getUserInfo_AuthenticatedUser_ShouldReturnUserInfo() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .login("testuser")
                .phone("+79991234567")
                .token("jwt-token")
                .build();

        ResponseEntity<?> response = userController.getUserInfo(userDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(userDto, response.getBody());
    }

    @Test
    void getUserInfo_UnauthenticatedUser_ShouldReturnUnauthorized() {
        ResponseEntity<?> response = userController.getUserInfo(null);

        assertNotNull(response);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("User is not authenticated", response.getBody());
    }

    @Test
    void updateUserInfo_AuthenticatedUser_ShouldUpdatePhone() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .login("testuser")
                .phone("+79991234567")
                .token("jwt-token")
                .build();

        UserDto updatedUserDto = UserDto.builder()
                .phone("+79998887766")
                .build();

        ResponseEntity<?> response = userController.updateUserInfo(userDto, updatedUserDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("+79998887766", userDto.getPhone());
        assertEquals("testuser", userDto.getLogin());
        assertEquals("jwt-token", userDto.getToken());
    }

    @Test
    void updateUserInfo_UnauthenticatedUser_ShouldReturnUnauthorized() {
        UserDto updatedUserDto = UserDto.builder()
                .phone("+79998887766")
                .build();

        ResponseEntity<?> response = userController.updateUserInfo(null, updatedUserDto);

        assertNotNull(response);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("User is not authenticated", response.getBody());
    }

    @Test
    void updateUserInfo_AuthenticatedUser_ShouldPreserveOtherFields() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .login("testuser")
                .phone("+79991234567")
                .token("jwt-token")
                .build();

        UserDto updatedUserDto = UserDto.builder()
                .phone("+79998887766")
                .login("newlogin")
                .token("new-token")
                .build();

        ResponseEntity<?> response = userController.updateUserInfo(userDto, updatedUserDto);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("+79998887766", userDto.getPhone());
        assertEquals("testuser", userDto.getLogin());
        assertEquals("jwt-token", userDto.getToken());
    }
}
