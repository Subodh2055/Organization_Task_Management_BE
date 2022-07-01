package com.example.client.user.service;

import com.example.client.user.model.User;
import com.example.client.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

//    public void deleteUserById(Long id) {
//        userRepository.deleteUserById(id);
//    }
//
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User addUser(User user) {
        return userRepository.save(user);
    }


    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName).orElse(null);
    }
}


