package com.thesurvey.api.service.mapper;

import com.thesurvey.api.domain.EnumTypeEntity.CertificationType;
import com.thesurvey.api.domain.QuestionBank;
import com.thesurvey.api.domain.Survey;
import com.thesurvey.api.dto.request.survey.SurveyRequestDto;
import com.thesurvey.api.dto.response.question.QuestionBankAnswerDto;
import com.thesurvey.api.dto.response.question.QuestionBankResponseDto;
import com.thesurvey.api.dto.response.survey.SurveyListPageDto;
import com.thesurvey.api.dto.response.survey.SurveyPageDto;
import com.thesurvey.api.dto.response.survey.SurveyResponseDto;
import com.thesurvey.api.dto.response.user.UserSurveyResultDto;
import com.thesurvey.api.repository.SurveyRepository;
import com.thesurvey.api.service.QuestionService;
import com.thesurvey.api.service.converter.CertificationTypeConverter;
import com.thesurvey.api.util.PointUtil;
import com.thesurvey.api.util.StringUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SurveyMapper {

    private final SurveyRepository surveyRepository;

    private final QuestionService questionService;

    private final CertificationTypeConverter certificationTypeConverter;

    private final PointUtil pointUtil;


    public SurveyMapper(SurveyRepository surveyRepository, QuestionService questionService,
        CertificationTypeConverter certificationTypeConverter, PointUtil pointUtil) {
        this.surveyRepository = surveyRepository;
        this.questionService = questionService;
        this.certificationTypeConverter = certificationTypeConverter;
        this.pointUtil = pointUtil;
    }

    public SurveyResponseDto toSurveyResponseDto(Survey survey, Long authorId) {
        List<QuestionBank> questionBankList = questionService.getAllQuestionBanksBySurveyId(survey.getSurveyId());
        int maxRewardPoints = PointUtil.getSurveyMaxRewardPoints(questionBankList);
        List<QuestionBankResponseDto> questionBankResponseDtoList = questionService.getQuestionBankInfoDtoListBySurveyId(
            survey.getSurveyId());
        return SurveyResponseDto.builder()
            .surveyId(survey.getSurveyId())
            .authorId(authorId)
            .title(survey.getTitle())
            .description(survey.getDescription())
            .startedDate(survey.getStartedDate())
            .endedDate(survey.getEndedDate())
            .createdDate(survey.getCreatedDate())
            .modifiedDate(survey.getModifiedDate())
            .certificationTypes(getConvertedCertificationTypes(survey.getSurveyId(), survey.getAuthorId()))
            .questions(questionBankResponseDtoList)
            .rewardPoints(maxRewardPoints)
            .build();
    }

    public Survey toSurvey(SurveyRequestDto surveyRequestDto, Long authorId) {
        return Survey
            .builder()
            .authorId(authorId)
            .title(StringUtil.trim(surveyRequestDto.getTitle()))
            .description(StringUtil.trim(surveyRequestDto.getDescription().trim()))
            .startedDate(surveyRequestDto.getStartedDate())
            .endedDate(surveyRequestDto.getEndedDate())
            .build();
    }

    public SurveyPageDto toSurveyPageDto(Survey survey) {
        List<QuestionBank> questionBankList = questionService.getAllQuestionBanksBySurveyId(survey.getSurveyId());
        int maxRewardPoints = PointUtil.getSurveyMaxRewardPoints(questionBankList);
        return SurveyPageDto.builder()
            .surveyId(survey.getSurveyId())
            .authorId(survey.getAuthorId())
            .title(survey.getTitle())
            .description(survey.getDescription())
            .startedDate(survey.getStartedDate())
            .createdDate(survey.getCreatedDate())
            .endedDate(survey.getEndedDate())
            .certificationTypes(getConvertedCertificationTypes(survey.getSurveyId(), survey.getAuthorId()))
            .modifiedDate(survey.getModifiedDate())
            .rewardPoints(maxRewardPoints)
            .build();
    }

    public SurveyListPageDto toSurveyListPageDto(List<SurveyPageDto> surveyPageDto,
        Page<Survey> surveyPage) {
        return SurveyListPageDto.builder()
            .surveys(surveyPageDto)
            .page(surveyPage.getPageable().getPageNumber() + 1)
            .totalSurveys(surveyPage.getTotalElements())
            .totalPages(surveyPage.getTotalPages())
            .build();
    }

    public UserSurveyResultDto toUserSurveyResultDto(Survey survey, List<QuestionBankAnswerDto> questionBankAnswerDtoList) {
        return UserSurveyResultDto.builder()
            .surveyId(survey.getSurveyId())
            .surveyTitle(survey.getTitle())
            .results(questionBankAnswerDtoList)
            .build();
    }

    private List<CertificationType> getConvertedCertificationTypes(Long surveyId, Long authorId) {
        List<Integer> certificationTypes =
            surveyRepository.findCertificationTypeBySurveyIdAndAuthorId(surveyId, authorId);
        if (certificationTypes.contains(CertificationType.NONE.getCertificationTypeId())) {
            return new ArrayList<>();
        }
        return certificationTypeConverter.toCertificationTypeList(
            certificationTypes);
    }

}
