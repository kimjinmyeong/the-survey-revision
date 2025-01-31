package com.thesurvey.api.controller;

import com.thesurvey.api.dto.request.user.UserCertificationUpdateRequestDto;
import com.thesurvey.api.dto.request.user.UserUpdateRequestDto;
import com.thesurvey.api.dto.response.user.UserResponseDto;
import com.thesurvey.api.dto.response.user.UserSurveyResultDto;
import com.thesurvey.api.dto.response.user.UserSurveyTitleDto;
import com.thesurvey.api.dto.response.userCertification.UserCertificationListDto;
import com.thesurvey.api.service.SurveyService;
import com.thesurvey.api.service.UserCertificationService;
import com.thesurvey.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@Tag(name = "사용자", description = "User Controller")
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserCertificationService userCertificationService;
    private final SurveyService surveyService;

    public UserController(UserService userService,
                          UserCertificationService userCertificationService, SurveyService surveyService) {
        this.userService = userService;
        this.userCertificationService = userCertificationService;
        this.surveyService = surveyService;
    }

    @Operation(summary = "사용자 정보 조회", description = "요청한 사용자의 정보를 가져옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "요청한 리소스 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/profile")
    public ResponseEntity<UserResponseDto> getUserProfile(
            @Parameter(hidden = true) Authentication authentication) {
        log.info("Fetching profile for authenticated user");
        UserResponseDto userResponseDto = userService.getUserProfile(authentication);
        log.info("Returning profile for user Email: {}", userResponseDto.getEmail());
        return ResponseEntity.ok(userResponseDto);
    }

    @Operation(summary = "사용자 설문조사 목록 조회", description = "사용자가 생성한 설문조사 목록을 가져옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "요청한 리소스 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/surveys")
    public ResponseEntity<List<UserSurveyTitleDto>> getUserCreatedSurveys(
            @Parameter(hidden = true) Authentication authentication) {
        log.info("Fetching created surveys for authenticated user");
        List<UserSurveyTitleDto> surveyTitles = surveyService.getUserCreatedSurveys(authentication);
        log.info("Returning {} created surveys for authenticated user", surveyTitles.size());
        return ResponseEntity.ok(surveyTitles);
    }

    @Operation(summary = "사용자 설문조사 결과 조회", description = "사용자가 생성한 설문조사 결과를 가져옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "요청한 리소스 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/surveys/{surveyId}")
    public ResponseEntity<UserSurveyResultDto> getUserCreatedSurveyResult(@PathVariable("surveyId") Long surveyId) {
        log.info("Fetching results for survey ID: {}", surveyId);
        UserSurveyResultDto surveyResult = surveyService.getUserCreatedSurveyResult(surveyId);
        log.info("Returning results for survey ID: {}", surveyId);
        return ResponseEntity.ok(surveyResult);
    }

    @Operation(summary = "사용자 정보 수정", description = "요청한 사용자의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "요청한 리소스 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/profile")
    public ResponseEntity<UserResponseDto> updateUserProfile(Authentication authentication, @RequestBody UserUpdateRequestDto userUpdateRequestDto) {
        log.info("Updating profile for user Email: {}", authentication.getName());
        UserResponseDto updatedUserResponseDto = userService.updateUserProfile(userUpdateRequestDto);
        log.info("Profile updated for user ID: {}", authentication.getName());
        return ResponseEntity.ok(updatedUserResponseDto);
    }

    @Operation(summary = "사용자 인증 정보 조회", description = "사용자의 인증 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "요청한 리소스 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/profile/certifications")
    public ResponseEntity<UserCertificationListDto> getUserCertification() {
        log.info("Fetching certifications for authenticated user");
        UserCertificationListDto certifications = userCertificationService.getUserCertifications();
        log.info("Returning {} certifications for authenticated user", certifications.getCertificationInfoList().size());
        return ResponseEntity.ok(certifications);
    }

    @Operation(summary = "사용자 인증 정보 수정", description = "사용자의 인증 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "요청한 리소스 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/profile/certifications")
    public ResponseEntity<UserCertificationListDto> updateUserCertification(
            @Parameter(hidden = true) Authentication authentication,
            @RequestBody @Valid UserCertificationUpdateRequestDto userCertificationUpdateRequestDto) {
        UserCertificationListDto updatedCertifications = userCertificationService.updateUserCertification(authentication, userCertificationUpdateRequestDto);
        return ResponseEntity.ok(updatedCertifications);
    }

    @Operation(summary = "사용자 삭제", description = "요청한 사용자의 정보를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "요청한 리소스 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(
            @Parameter(hidden = true) Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        log.info("Deleting user with authentication details: {}", authentication.getName());
        userService.deleteUser(authentication);
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        log.info("User deleted and logged out: {}", authentication.getName());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
