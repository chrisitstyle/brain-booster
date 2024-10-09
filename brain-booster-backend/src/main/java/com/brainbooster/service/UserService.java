package com.brainbooster.service;

import com.brainbooster.model.User;
import com.brainbooster.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;



    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

}
