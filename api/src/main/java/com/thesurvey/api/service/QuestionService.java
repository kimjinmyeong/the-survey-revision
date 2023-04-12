package com.thesurvey.api.service;

import com.thesurvey.api.domain.Question;
import com.thesurvey.api.domain.QuestionBank;
import com.thesurvey.api.domain.Survey;
import com.thesurvey.api.dto.response.QuestionBankResponseDto;
import com.thesurvey.api.dto.request.QuestionBankUpdateRequestDto;
import com.thesurvey.api.dto.request.QuestionRequestDto;
import com.thesurvey.api.dto.request.SurveyRequestDto;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.BadRequestExceptionMapper;
import com.thesurvey.api.repository.QuestionBankRepository;
import com.thesurvey.api.repository.QuestionRepository;
import com.thesurvey.api.service.mapper.QuestionBankMapper;
import com.thesurvey.api.service.mapper.QuestionMapper;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionService {

    private final QuestionBankRepository questionBankRepository;
    private final QuestionBankMapper questionBankMapper;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final QuestionOptionService questionOptionService;

    public QuestionService(QuestionBankRepository questionBankRepository,
        QuestionBankMapper questionBankMapper, QuestionRepository questionRepository,
        QuestionMapper questionMapper,
        QuestionOptionService questionOptionService) {
        this.questionBankRepository = questionBankRepository;
        this.questionBankMapper = questionBankMapper;
        this.questionRepository = questionRepository;
        this.questionMapper = questionMapper;
        this.questionOptionService = questionOptionService;
    }

    @Transactional(readOnly = true)
    public List<QuestionBankResponseDto> getQuestionBankInfoDtoListBySurveyId(UUID surveyId) {
        return questionBankRepository.findAllBySurveyId(surveyId)
            .stream()
            .map(questionBankMapper::toQuestionBankResponseDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void createQuestion(SurveyRequestDto surveyRequestDto, Survey survey) {
        for (QuestionRequestDto questionRequestDto : surveyRequestDto.getQuestions()) {
            QuestionBank questionBank = questionBankRepository.save(
                questionBankMapper.toQuestionBank(questionRequestDto));
            questionRepository.save(
                questionMapper.toQuestion(questionRequestDto, survey, questionBank));
            if (questionRequestDto.getQuestionOptions() != null) {
                questionOptionService.createQuestionOption(questionRequestDto, questionBank);
            }
        }
    }

    @Transactional
    public void updateQuestion(
        List<QuestionBankUpdateRequestDto> questionBankUpdateRequestDtoList) {
        for (QuestionBankUpdateRequestDto questionBankUpdateRequestDto : questionBankUpdateRequestDtoList) {
            QuestionBank questionBank = questionBankRepository.findByQuestionBankId(
                    questionBankUpdateRequestDto.getQuestionBankId())
                .orElseThrow(() -> new BadRequestExceptionMapper(ErrorMessage.QUESTION_BANK_NOT_FOUND));

            if (questionBankUpdateRequestDto.getTitle() != null) {
                questionBank.changeTitle(questionBankUpdateRequestDto.getTitle().trim());
            }
            if (questionBankUpdateRequestDto.getDescription() != null) {
                questionBank.changeDescription(questionBankUpdateRequestDto.getDescription().trim());
            }
            if (questionBankUpdateRequestDto.getQuestionType() != null) {
                questionBank.changeQuestionType(questionBankUpdateRequestDto.getQuestionType());
            }

            Question question = questionRepository.findByQuestionBankId(
                questionBank.getQuestionBankId()).orElseThrow(() -> new BadRequestExceptionMapper(
                ErrorMessage.QUESTION_NOT_FOUND));

            if (questionBankUpdateRequestDto.getIsRequired() != null) {
                question.changeIsRequired(questionBankUpdateRequestDto.getIsRequired());
            }
            if (questionBankUpdateRequestDto.getQuestionNo() != null) {
                question.changeQuestionNo(questionBankUpdateRequestDto.getQuestionNo());
            }
            if (questionBankUpdateRequestDto.getQuestionOptions() != null) {
                questionOptionService.updateQuestionOption(
                    questionBankUpdateRequestDto.getQuestionOptions());
            }
        }
    }

    @Transactional
    public void deleteQuestion(UUID surveyId) {
        List<Question> questionList = questionRepository.findAllBySurveyId(surveyId);
        questionRepository.deleteAll(questionList);
    }

}
