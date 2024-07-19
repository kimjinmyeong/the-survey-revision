package com.thesurvey.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thesurvey.api.domain.User;
import com.thesurvey.api.dto.request.user.UserLoginRequestDto;
import com.thesurvey.api.dto.response.user.UserResponseDto;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.BadRequestExceptionMapper;
import com.thesurvey.api.service.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class LoginAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;

    public LoginAuthenticationFilter(AuthenticationManager authenticationManager, ObjectMapper objectMapper, UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.objectMapper = objectMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        log.info("Attempting authentication");
        UserLoginRequestDto userLoginRequestDto;
        try {
            userLoginRequestDto = new ObjectMapper().readValue(request.getInputStream(), UserLoginRequestDto.class);
        } catch (IOException e) {
            log.error("Failed to parse login request", e);
            throw new BadRequestExceptionMapper(ErrorMessage.INVALID_REQUEST);
        }

        String email = userLoginRequestDto.getEmail();
        String password = userLoginRequestDto.getPassword();
        log.debug("Authenticating user with email: {}", email);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);
        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authentication) throws IOException {
        log.info("Authentication successful for user: {}", authentication.getName());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;");
        response.setCharacterEncoding("UTF-8");

        UserResponseDto userResponseDto = userMapper.toUserResponseDto((User) authentication.getPrincipal());
        String jsonResponse = objectMapper.writeValueAsString(userResponseDto);
        PrintWriter writer = response.getWriter();
        writer.write(jsonResponse);
        writer.flush();
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        log.warn("Authentication failed for user: {}", request.getParameter("email"));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        writer.write(ErrorMessage.INVALID_CREDENTIALS.getMessage());
        writer.flush();
    }
}
