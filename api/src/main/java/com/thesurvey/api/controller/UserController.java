package com.thesurvey.api.controller;

import com.thesurvey.api.domain.User;
import com.thesurvey.api.dto.UserInfoDto;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.ExceptionMapper;
import com.thesurvey.api.repository.UserRepository;
import com.thesurvey.api.service.UserService;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public ResponseEntity<Optional<List<User>>> getAllUsersWithAnsweredQuestions() {
        return ResponseEntity.ok(userService.getAllUsersWithAnsweredQuestions());
    }

    @GetMapping("/profile")
    public ResponseEntity<UserInfoDto> getUserProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserProfile(authentication));
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

}
