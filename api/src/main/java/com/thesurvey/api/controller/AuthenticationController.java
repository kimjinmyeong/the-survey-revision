package com.thesurvey.api.controller;

import com.thesurvey.api.dto.request.user.UserLoginRequestDto;
import com.thesurvey.api.dto.request.user.UserRegisterRequestDto;
import com.thesurvey.api.dto.response.user.UserResponseDto;
import com.thesurvey.api.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "인증", description = "Authentication Controller")
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "회원가입", description = "회원가입을 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청 성공", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "요청한 리소스 찾을 수 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(
            @Valid @RequestBody UserRegisterRequestDto userRegisterRequestDto) {
        log.info("Received request to register user with email: {}", userRegisterRequestDto.getEmail());
        UserResponseDto userResponseDto = authenticationService.register(userRegisterRequestDto);
        log.info("User registered successfully with email: {}", userResponseDto.getEmail());
        return ResponseEntity.ok(userResponseDto);
    }

    @Operation(summary = "로그인", description = "로그인을 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청 성공", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 인증정보", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "요청한 리소스 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/login")
    public void login(@RequestBody UserLoginRequestDto loginRequestDto) {
        // This method is intentionally left empty because the login functionality
        // is handled by the filter chain configuration in Spring Security.
        // The LoginAuthenticationFilter will handle the actual login process.
    }

    @Operation(summary = "로그아웃", description = "로그아웃을 요청합니다.")
    @ApiResponse(responseCode = "200", description = "요청 성공", useReturnTypeSchema = true)
    @GetMapping("/logout")
    public void logout() {
        // This method is intentionally left empty because the logout functionality
        // is handled by the filter chain configuration in Spring Security.
        // The SecurityContextLogoutHandler will handle the actual logout process.
    }
}
