package com.thesurvey.api.util;

import com.thesurvey.api.domain.User;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.UnauthorizedRequestExceptionMapper;
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
        validateUserAuthentication(authentication);
        return (User) authentication.getPrincipal();
    }

    public static Long getUserIdFromAuthentication(Authentication authentication) {
        validateUserAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        return user.getUserId();
    }

    public static void validateUserAuthentication(Authentication authentication) {
        if (authentication != null && !authentication.isAuthenticated()) {
            throw new UnauthorizedRequestExceptionMapper(ErrorMessage.FAILED_AUTHENTICATION);
        }
    }
}
