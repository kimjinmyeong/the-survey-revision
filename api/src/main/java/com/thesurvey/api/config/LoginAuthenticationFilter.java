package com.thesurvey.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thesurvey.api.dto.request.user.UserLoginRequestDto;
import com.thesurvey.api.dto.response.user.UserResponseDto;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.UnauthorizedRequestExceptionMapper;
import com.thesurvey.api.service.UserService;
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

public class LoginAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private final UserService userService;

    private final ObjectMapper objectMapper;

    public LoginAuthenticationFilter(AuthenticationManager authenticationManager, UserService userService, ObjectMapper objectMapper) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            UserLoginRequestDto userLoginRequestDto = new ObjectMapper().readValue(request.getInputStream(), UserLoginRequestDto.class);
            String email = userLoginRequestDto.getEmail();
            String password = userLoginRequestDto.getPassword();

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);

            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authentication) throws IOException {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json; charset=UTF-8");

        UserResponseDto userResponseDto = userService.getUserByName(authentication.getName());
        String jsonResponse = objectMapper.writeValueAsString(userResponseDto);
        PrintWriter writer = response.getWriter();
        writer.write(jsonResponse);
        writer.flush();
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) {
        throw new UnauthorizedRequestExceptionMapper(ErrorMessage.INVALID_CREDENTIALS);
    }

}
