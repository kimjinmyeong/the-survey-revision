package com.thesurvey.api.controller;

import com.thesurvey.api.dto.request.answeredQuestion.AnsweredQuestionRequestDto;
import com.thesurvey.api.dto.request.survey.SurveyRequestDto;
import com.thesurvey.api.dto.request.survey.SurveyUpdateRequestDto;
import com.thesurvey.api.dto.response.answeredQuestion.AnsweredQuestionRewardPointDto;
import com.thesurvey.api.dto.response.survey.SurveyListPageDto;
import com.thesurvey.api.dto.response.survey.SurveyResponseDto;
import com.thesurvey.api.service.AnsweredQuestionService;
import com.thesurvey.api.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "설문조사", description = "Survey Controller")
@RestController
@RequestMapping("/surveys")
public class SurveyController {

    private final SurveyService surveyService;

    private final AnsweredQuestionService answeredQuestionService;

    public SurveyController(SurveyService surveyService,
        AnsweredQuestionService answeredQuestionService) {
        this.surveyService = surveyService;
        this.answeredQuestionService = answeredQuestionService;
    }

    @Operation(summary = "페이지별 설문조사 조회", description = "모든 설문조사를 페이지별로 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "요청 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "요청한 리소스 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping
    public ResponseEntity<SurveyListPageDto> getAllSurvey(
        @Parameter(name = "페이지 번호", example = "1", description = "기본값인 1부터 시작합니다.") @RequestParam(name = "page", defaultValue = "1") int page) {
        return ResponseEntity.ok(surveyService.getAllSurvey(page));
    }

    @Operation(summary = "개별 설문조사 조회", description = "파라미터로 전달 받은 ID에 해당하는 설문조사를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "요청 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "요청한 리소스 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{surveyId}")
    public ResponseEntity<SurveyResponseDto> getSurvey(@PathVariable Long surveyId) {
        return ResponseEntity.ok(surveyService.getSurveyBySurveyIdWithRelatedQuestion(surveyId));
    }

    @Operation(summary = "설문조사 생성", description = "새로운 설문조사를 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "요청 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "요청한 리소스 찾을 수 없음", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping
    public ResponseEntity<SurveyResponseDto> createSurvey(@Valid @RequestBody SurveyRequestDto surveyRequestDto) {
        return ResponseEntity.ok(surveyService.createSurvey(surveyRequestDto));
    }

    @Operation(summary = "설문조사 수정", description = "설문조사 내용을 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "요청 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "요청한 리소스 찾을 수 없음", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping
    public ResponseEntity<SurveyResponseDto> updateSurvey(
        @Valid @RequestBody SurveyUpdateRequestDto surveyUpdateRequestDto) {
        return ResponseEntity.ok(
            surveyService.updateSurvey(surveyUpdateRequestDto));
    }

    @Operation(summary = "설문조사 삭제", description = "파라미터로 전달 받은 ID에 해당하는 설문조사를 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "요청 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "요청한 리소스 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{surveyId}")
    public ResponseEntity<Void> deleteSurvey(
        @Parameter(hidden = true) Authentication authentication,
        @PathVariable("surveyId") Long surveyId) {
        surveyService.deleteSurvey(authentication, surveyId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "설문조사 응답 제출", description = "설문조사 응답을 제출합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "요청 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "요청한 리소스 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/submit")
    public ResponseEntity<AnsweredQuestionRewardPointDto> submitSurvey(
            @Valid @RequestBody AnsweredQuestionRequestDto answeredQuestionRequestDto) {
        AnsweredQuestionRewardPointDto rewardPointDto =
            answeredQuestionService.createAnswer(answeredQuestionRequestDto);
        return ResponseEntity.ok(rewardPointDto);
    }

}
