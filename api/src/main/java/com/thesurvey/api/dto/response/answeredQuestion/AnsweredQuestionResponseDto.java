package com.thesurvey.api.dto.response.answeredQuestion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AnsweredQuestionResponseDto {

    List<AnsweredQuestionInfoDto> questions;

    @Schema(example = "1", description = "참여한 설문조사의 아이디입니다.")
    private Long surveyId;

}
