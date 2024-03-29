package com.example.financemanager.auth.service;

import com.example.financemanager.auth.controller.AuthRepository;
import com.example.financemanager.auth.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PasswordEncoder bCryptPasswordEncoder;
    private final AuthRepository authRepository;

    @Autowired
    public AuthService(PasswordEncoder bCryptPasswordEncoder, AuthRepository authRepository) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authRepository = authRepository;
    }

    public void registerUser(Customer userDetails) {
        String encodedPassword = bCryptPasswordEncoder.encode(userDetails.getPassword());
        userDetails.setPassword(encodedPassword);

        authRepository.save(userDetails);
    }

    public boolean validateUser(String usernameOrEmail, String password) {
        Customer user = authRepository.findByUsernameOrEmail(usernameOrEmail);
        if (user != null) {
            return bCryptPasswordEncoder.matches(password, user.getPassword());
        }
        return false;
    }
}