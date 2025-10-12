package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {
    @GetMapping
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal UserDto userDto) {
        if (userDto == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }
        return ResponseEntity.ok(userDto);
    }

    @PutMapping
    public ResponseEntity<?> updateUserInfo(@AuthenticationPrincipal UserDto userDto, @RequestBody UserDto updatedUserDto) {
        if (userDto == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }
        userDto.setPhone(updatedUserDto.getPhone());

        return ResponseEntity.ok(userDto);
    }
}
