package com.thesurvey.api.util;

import com.thesurvey.api.domain.User;
import com.thesurvey.api.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserUtil {

    public static UserRepository userRepository;

    public UserUtil(UserRepository userRepository) {
        UserUtil.userRepository = userRepository;
    }

    public static User getUserFromAuthentication(Authentication authentication) {
        String name = authentication.getPrincipal().toString();
        return userRepository.findByName(name).orElseThrow();
    }

    public static Long getUserIdFromAuthentication(Authentication authentication) {
        String name = authentication.getPrincipal().toString();
        return userRepository.findByName(name).orElseThrow().getUserId();
    }
}
