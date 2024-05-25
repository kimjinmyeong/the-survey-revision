package com.thesurvey.api.dto.response.user;

import com.thesurvey.api.dto.response.question.QuestionBankAnswerDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSurveyResultDto {

    @Schema(example = "1", description = "조회된 설문조사 아이디입니다.")
    private Long surveyId;

    @Schema(example = "카카오 사용자분들께 설문 부탁드립니다!", description = "조회된 설문조사의 제목입니다.")
    private String surveyTitle;

    private List<QuestionBankAnswerDto> results;

}
