package com.thesurvey.api.service;

import com.thesurvey.api.domain.User;
import com.thesurvey.api.dto.response.user.UserResponseDto;
import com.thesurvey.api.dto.request.user.UserUpdateRequestDto;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.NotFoundExceptionMapper;
import com.thesurvey.api.repository.UserRepository;
import com.thesurvey.api.service.mapper.UserMapper;
import com.thesurvey.api.util.StringUtil;
import com.thesurvey.api.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponseDto getUserByName(String name) {
        log.info("Fetching user by name: {}", name);
        User user = userRepository.findByName(name)
                .orElseThrow(() -> {
                    log.error("User not found with name: {}", name);
                    return new NotFoundExceptionMapper(ErrorMessage.USER_NAME_NOT_FOUND, name);
                });
        log.info("User found with name: {}", name);
        return userMapper.toUserResponseDto(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserProfile(Authentication authentication) {
        log.info("Fetching user profile for authenticated user");
        User user = UserUtil.getUserFromAuthentication(authentication);
        log.info("User profile fetched for user: {}", user.getEmail());
        return userMapper.toUserResponseDto(user);
    }

    @Transactional
    public UserResponseDto updateUserProfile(UserUpdateRequestDto userUpdateRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = UserUtil.getUserFromAuthentication(authentication);
        log.info("Updating profile for user: {}", user.getUserId());

        if (userUpdateRequestDto.getPassword() != null) {
            log.debug("Updating password for user: {}", user.getUserId());
            user.changePassword(userUpdateRequestDto.getPassword());
        }

        if (userUpdateRequestDto.getPhoneNumber() != null) {
            log.debug("Updating phone number for user: {}", user.getUserId());
            user.changePhoneNumber(userUpdateRequestDto.getPhoneNumber());
        }

        if (userUpdateRequestDto.getProfileImage() != null) {
            log.debug("Updating profile image for user: {}", user.getUserId());
            user.changeProfileImage(userUpdateRequestDto.getProfileImage());
        }

        if (userUpdateRequestDto.getAddress() != null) {
            log.debug("Updating address for user: {}", user.getUserId());
            user.changeAddress(StringUtil.trim(userUpdateRequestDto.getAddress()));
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", user.getUserId());
        return userMapper.toUserResponseDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Authentication authentication) {
        User user = UserUtil.getUserFromAuthentication(authentication);
        log.info("Deleting user: {}", user.getEmail());
        userRepository.delete(user);
        log.info("User deleted: {}", user.getEmail());
    }
}
