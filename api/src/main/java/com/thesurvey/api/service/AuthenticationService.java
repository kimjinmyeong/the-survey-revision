package com.thesurvey.api.service;

import com.thesurvey.api.domain.PointHistory;
import com.thesurvey.api.domain.User;
import com.thesurvey.api.dto.request.user.UserLoginRequestDto;
import com.thesurvey.api.dto.request.user.UserRegisterRequestDto;
import com.thesurvey.api.dto.response.user.UserResponseDto;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.BadRequestExceptionMapper;
import com.thesurvey.api.exception.mapper.UnauthorizedRequestExceptionMapper;
import com.thesurvey.api.repository.PointHistoryRepository;
import com.thesurvey.api.repository.UserRepository;
import com.thesurvey.api.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserDetailsService userDetailsService;

    private final UserService userService;

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PointHistoryRepository pointHistoryRepository;

    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        UserDetails userDetails = userDetailsService.loadUserByUsername(authentication.getName());

        checkPassword(authentication.getCredentials().toString(), userDetails.getPassword());

        return new UsernamePasswordAuthenticationToken(userDetails.getUsername(),
            userDetails.getPassword(),
            userDetails.getAuthorities());
    }

    @Transactional
    public UserResponseDto register(UserRegisterRequestDto userRegisterRequestDto) {
        log.info("Registering new user with email: {}", userRegisterRequestDto.getEmail());

        User user = userRepository.save(userMapper.toUser(userRegisterRequestDto));
        log.info("User registered with ID: {}", user.getUserId());

        pointHistoryRepository.save(
                PointHistory.builder()
                        .user(user)
                        .transactionDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                        .build()
        );
        log.info("Point history created for user ID: {}", user.getUserId());

        UserResponseDto userResponseDto = userMapper.toUserResponseDto(user);
        log.info("Returning UserResponseDto for user ID: {}", user.getUserId());
        return userResponseDto;
    }

    @Transactional(readOnly = true)
    public UserResponseDto login(UserLoginRequestDto userLoginRequestDto) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userLoginRequestDto.getEmail(), userLoginRequestDto.getPassword());

        try {
            Authentication authenticated = authenticate(authentication);
            SecurityContextHolder.getContext().setAuthentication(authenticated);
            return userService.getUserByName(authenticated.getName());
        } catch (AuthenticationException e) {
            throw new UnauthorizedRequestExceptionMapper(ErrorMessage.UNAUTHORIZED_REQUEST);
        }
    }

    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void checkPassword(CharSequence rawPassword, String encodedPassword) {
        if (!passwordEncoder().matches(rawPassword, encodedPassword)) {
            throw new BadRequestExceptionMapper(ErrorMessage.INVALID_CREDENTIALS);
        }
    }
}
