package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminUserController adminUserController;

    @Test
    void getAllUsers_ShouldReturnPageOfUsers() {
        Pageable pageable = Pageable.ofSize(10);
        UserDto userDto = UserDto.builder()
                .id(1L)
                .login("testuser")
                .phone("+79991234567")
                .build();
        Page<UserDto> userPage = new PageImpl<>(List.of(userDto));

        when(userService.findAll(pageable)).thenReturn(userPage);

        ResponseEntity<Page<UserDto>> response = adminUserController.getAllUsers(pageable);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("testuser", response.getBody().getContent().get(0).getLogin());
        verify(userService).findAll(pageable);
    }

    @Test
    void deleteUser_ExistingUser_ShouldReturnNoContent() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<?> response = adminUserController.deleteUser(1L);

        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(userService).deleteUser(1L);
    }

    @Test
    void setBanned_True_ShouldBanUser() {
        doNothing().when(userService).setBanned(1L, true);

        ResponseEntity<?> response = adminUserController.setBanned(1L, true);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(userService).setBanned(1L, true);
    }

    @Test
    void setBanned_False_ShouldUnbanUser() {
        doNothing().when(userService).setBanned(1L, false);

        ResponseEntity<?> response = adminUserController.setBanned(1L, false);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(userService).setBanned(1L, false);
    }
}
