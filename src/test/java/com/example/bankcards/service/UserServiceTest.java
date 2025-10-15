package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.auth.CredentialsDto;
import com.example.bankcards.dto.auth.SignUpDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.AppException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.CharBuffer;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void login_ValidCredentials_ShouldReturnUserDto() {
        CredentialsDto credentials = CredentialsDto.builder()
                .login("testuser")
                .password("password")
                .build();
        User user = new User();
        user.setLogin("testuser");
        user.setPassword("encodedPassword");
        UserDto userDto = UserDto.builder()
                .id(1L)
                .login("testuser")
                .phone("+79991234567")
                .token("jwt-token")
                .build();

        when(userRepository.getByLogin("testuser")).thenReturn(user);
        when(passwordEncoder.matches(CharBuffer.wrap("password"), "encodedPassword")).thenReturn(true);
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        UserDto result = userService.login(credentials);

        assertNotNull(result);
        assertEquals("testuser", result.getLogin());
        assertEquals("+79991234567", result.getPhone());
        assertEquals("jwt-token", result.getToken());
        verify(userRepository).getByLogin("testuser");
        verify(passwordEncoder).matches(CharBuffer.wrap("password"), "encodedPassword");
    }

    @Test
    void login_InvalidPassword_ShouldThrowException() {
        CredentialsDto credentials = CredentialsDto.builder()
                .login("testuser")
                .password("wrongpassword")
                .build();
        User user = new User();
        user.setLogin("testuser");
        user.setPassword("encodedPassword");

        when(userRepository.getByLogin("testuser")).thenReturn(user);
        when(passwordEncoder.matches(CharBuffer.wrap("wrongpassword"), "encodedPassword")).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () ->
                userService.login(credentials));

        assertEquals("Invalid password", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void register_NewUser_ShouldCreateUserSuccessfully() {
        SignUpDto signUpDto = SignUpDto.builder()
                .login("newuser")
                .password("password")
                .phone("+79991234567")
                .build();
        User user = new User();
        user.setLogin("newuser");
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setLogin("newuser");
        UserDto userDto = UserDto.builder()
                .id(1L)
                .login("newuser")
                .phone("+79991234567")
                .token("jwt-token")
                .build();

        when(userRepository.findByLogin("newuser")).thenReturn(Optional.empty());
        when(userMapper.signUpToUser(signUpDto)).thenReturn(user);
        when(passwordEncoder.encode(CharBuffer.wrap("password"))).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toUserDto(savedUser)).thenReturn(userDto);

        UserDto result = userService.register(signUpDto);

        assertNotNull(result);
        assertEquals("newuser", result.getLogin());
        assertEquals("+79991234567", result.getPhone());
        assertEquals("jwt-token", result.getToken());
        verify(userRepository).findByLogin("newuser");
        verify(userRepository).save(user);
        assertEquals("encodedPassword", user.getPassword());
        assertEquals(UserRole.USER, user.getRole());
    }

    @Test
    void register_ExistingLogin_ShouldThrowException() {
        SignUpDto signUpDto = SignUpDto.builder()
                .login("existinguser")
                .password("password")
                .phone("+79991234567")
                .build();
        User existingUser = new User();

        when(userRepository.findByLogin("existinguser")).thenReturn(Optional.of(existingUser));

        AppException exception = assertThrows(AppException.class, () ->
                userService.register(signUpDto));

        assertEquals("Login already exists", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(userRepository, never()).save(any());
    }

    @Test
    void findByLogin_ExistingUser_ShouldReturnUser() {
        User user = new User();
        user.setLogin("testuser");

        when(userRepository.getByLogin("testuser")).thenReturn(user);

        User result = userService.findByLogin("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getLogin());
        verify(userRepository).getByLogin("testuser");
    }

    @Test
    void getUserById_ExistingUser_ShouldReturnUser() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(user);

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).findById(1L);
    }

    @Test
    void findAll_WithPageable_ShouldReturnPageOfUsers() {
        Pageable pageable = Pageable.ofSize(10);
        User user = new User();
        user.setId(1L);
        user.setLogin("testuser");
        Page<User> userPage = new PageImpl<>(List.of(user));
        UserDto userDto = UserDto.builder()
                .id(1L)
                .login("testuser")
                .phone("+79991234567")
                .build();

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        Page<UserDto> result = userService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("testuser", result.getContent().get(0).getLogin());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void deleteUser_ExistingUser_ShouldDeleteUser() {
        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void setBanned_ShouldUpdateBannedStatus() {
        userService.setBanned(1L, true);

        verify(userRepository).updateBannedStatus(1L, true);
    }

    @Test
    void getUserByLogin_ExistingUser_ShouldReturnUser() {
        User user = new User();
        user.setLogin("testuser");

        when(userRepository.getByLogin("testuser")).thenReturn(user);

        User result = userService.getUserByLogin("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getLogin());
        verify(userRepository).getByLogin("testuser");
    }
}