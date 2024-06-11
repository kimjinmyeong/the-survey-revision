package com.thesurvey.api.service.mapper;

import com.thesurvey.api.domain.User;
import com.thesurvey.api.dto.request.user.UserRegisterRequestDto;
import com.thesurvey.api.dto.response.user.UserResponseDto;
import com.thesurvey.api.util.StringUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toUser(UserRegisterRequestDto userRegisterRequestDto) {
        return User
            .builder()
            .name(StringUtil.trim(userRegisterRequestDto.getName()))
            .email(StringUtil.trim(userRegisterRequestDto.getEmail()))
            .password(passwordEncoder().encode(userRegisterRequestDto.getPassword()))
            .phoneNumber(userRegisterRequestDto.getPhoneNumber())
            .address(StringUtil.trim(userRegisterRequestDto.getAddress()))
            .profileImage(userRegisterRequestDto.getProfileImage())
            .point(User.USER_INITIAL_POINT)
            .build();
    }

    public UserResponseDto toUserResponseDto(User user) {
        return UserResponseDto.builder()
            .userId(user.getUserId())
            .name(user.getName())
            .email(user.getEmail())
            .address(user.getAddress())
            .role(user.getRole())
            .phoneNumber(user.getPhoneNumber())
            .profileImage(user.getProfileImage())
            .createdDate(user.getCreatedDate())
            .modifiedDate(user.getModifiedDate())
            .point(user.getPoint())
            .build();
    }

    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
