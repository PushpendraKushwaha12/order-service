package com.user_service.controller;

import com.user_service.config.JwtTokenProvider;
import com.user_service.dto.ApiResponse;
import com.user_service.dto.LoginRequest;
import com.user_service.dto.LoginResponse;
import com.user_service.dto.UserDTO;
import com.user_service.entity.User;
import com.user_service.exception.ResourceNotFoundException;
import com.user_service.exception.UnauthorizedException;
import com.user_service.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@RequestBody User user) {
        User savedUser = userService.createUser(user);
        String token = jwtTokenProvider.generateToken(savedUser.getEmail(), savedUser.getRole());
        LoginResponse loginResponse = new LoginResponse(token, savedUser.getEmail(), savedUser.getRole(), "User registered successfully");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "User registered successfully", loginResponse, System.currentTimeMillis()));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.getUserByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + loginRequest.getEmail()));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole());
        LoginResponse loginResponse = new LoginResponse(token, user.getEmail(), user.getRole(), "Login successful");
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Login successful", loginResponse, System.currentTimeMillis()));
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> create(@RequestBody User user) {
        User savedUser = userService.createUser(user);
        UserDTO userDTO = new UserDTO(savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getRole());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "User created successfully", userDTO, System.currentTimeMillis()));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Users retrieved successfully", userService.getAllUsers(), System.currentTimeMillis()));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User retrieved successfully", userService.getUserById(id), System.currentTimeMillis()));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User updated successfully", userService.updateUser(id, userDetails), System.currentTimeMillis()));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User deleted successfully", null, System.currentTimeMillis()));
    }

//    @GetMapping("/auth/health")
//    public ResponseEntity<ApiResponse<String>> health() {
//        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User service is healthy", "OK", System.currentTimeMillis()));
//    }
}

