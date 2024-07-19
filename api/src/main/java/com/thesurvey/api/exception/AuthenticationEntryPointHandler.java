package com.thesurvey.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles authentication errors that occur during the Spring Security filter chain, such as when a
 * user attempts to access a secured endpoint without authentication credentials.
 */
public class AuthenticationEntryPointHandler implements AuthenticationEntryPoint {

    // method is called by the Spring Security filter chain when an authentication error occurs.
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("권한이 없습니다.");
    }
}
