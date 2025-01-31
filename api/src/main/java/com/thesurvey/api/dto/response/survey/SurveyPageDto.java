package com.thesurvey.api.dto.response.survey;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thesurvey.api.domain.EnumTypeEntity.CertificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SurveyPageDto {

    @Schema(example = "1", description = "조회된 설문조사의 아이디입니다.")
    private Long surveyId;

    @Schema(example = "1", description = "조회된 설문조사의 생성자 아이디입니다.")
    private Long authorId;

    @Schema(example = "카카오 사용자분들께 설문 부탁드립니다!", description = "조회된 설문조사의 제목입니다.")
    private String title;

    @Schema(example = "카카오 앱 서비스에 대한 전반적인 만족도 조사입니다.", description = "조회된 설문조사의 상세내용입니다.")
    private String description;

    @Schema(example = "2030-12-01T00:00:00", description = "조회된 설문조사의 시작일입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedDate;

    @Schema(example = "2030-12-12T00:00:00", description = "조회된 설문조사의 종료일입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endedDate;

    @Schema(example = "[\"NAVER\", \"KAKAO\"]", description = "조회된 설문조사의 필수인증 목록입니다.")
    private List<CertificationType> certificationTypes;

    @Schema(example = "2023-04-22T00:00:00", description = "조회된 설문조사의 생성일입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    @Schema(example = "2023-04-22T00:00:00", description = "조회된 설문조사의 수정일입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime modifiedDate;

    @Schema(example = "1", description = "설문조사 완료시 획득할 수 있는 포인트입니다.")
    private Integer rewardPoints;

}
