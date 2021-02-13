package com.upgrad.quora.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class UserController {



    public ResponseEntity<SignupUserResponse> signup(final SignupUserRequest signupUserRequest) {

    }
}
