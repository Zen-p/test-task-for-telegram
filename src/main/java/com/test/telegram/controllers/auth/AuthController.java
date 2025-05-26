package com.test.telegram.controllers.auth;

import com.test.telegram.DTOs.AuthRequest;
import com.test.telegram.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


// тут, кажется, много времени уйдет
// счетчик потраченных часов: 1

@RestController
public class AuthController {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@RequestBody AuthRequest request) {
        return ResponseEntity.ok("Smthng");
    }

}
