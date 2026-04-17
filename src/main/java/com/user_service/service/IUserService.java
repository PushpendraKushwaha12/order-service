package com.user_service.service;

import com.user_service.dto.UserDTO;
import com.user_service.entity.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    User createUser(User user);
    List<UserDTO> getAllUsers();
    UserDTO getUserById(Long id);
    UserDTO updateUser(Long id, User userDetails);
    void deleteUser(Long id);
    Optional<User> getUserByEmail(String email);
}
