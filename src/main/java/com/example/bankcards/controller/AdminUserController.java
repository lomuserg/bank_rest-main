package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    // GET /users — все пользователи
    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    // DELETE /users/{id} — удалить пользователя
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // PUT /users/{id}/ban — заблокировать/разблокировать
    @PutMapping("/{id}/ban")
    public ResponseEntity<?> setBanned(@PathVariable Long id, @RequestParam boolean banned) {
        userService.setBanned(id, banned);
        return ResponseEntity.ok().build();
    }
}
