package com.thesurvey.api.service;

import com.thesurvey.api.domain.Question;
import com.thesurvey.api.domain.QuestionBank;
import com.thesurvey.api.domain.QuestionOption;
import com.thesurvey.api.domain.Survey;
import com.thesurvey.api.dto.request.question.QuestionBankUpdateRequestDto;
import com.thesurvey.api.dto.request.question.QuestionRequestDto;
import com.thesurvey.api.dto.response.question.QuestionBankResponseDto;
import com.thesurvey.api.dto.response.question.QuestionOptionResponseDto;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.BadRequestExceptionMapper;
import com.thesurvey.api.exception.mapper.NotFoundExceptionMapper;
import com.thesurvey.api.repository.QuestionBankRepository;
import com.thesurvey.api.repository.QuestionOptionRepository;
import com.thesurvey.api.repository.QuestionRepository;
import com.thesurvey.api.service.mapper.QuestionBankMapper;
import com.thesurvey.api.service.mapper.QuestionMapper;
import com.thesurvey.api.service.mapper.QuestionOptionMapper;
import com.thesurvey.api.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionBankRepository questionBankRepository;
    private final QuestionBankMapper questionBankMapper;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final QuestionOptionService questionOptionService;
    private final QuestionOptionMapper questionOptionMapper;
    private final QuestionOptionRepository questionOptionRepository;

    @Transactional(readOnly = true)
    public List<QuestionBank> getAllQuestionBankBySurveyId(Long surveyId) {
        log.info("Fetching all question banks for survey ID: {}", surveyId);
        return questionBankRepository.findAllBySurveyId(surveyId);
    }

    @Transactional(readOnly = true)
    public List<QuestionBankResponseDto> getQuestionBankInfoDtoListBySurveyId(Long surveyId) {
        log.info("Fetching question bank info DTO list for survey ID: {}", surveyId);
        return questionBankRepository.findAllBySurveyId(surveyId)
                .stream()
                .map(questionBank -> {
                    List<QuestionOption> questionOptionList = questionOptionRepository
                            .findAllByQuestionBankId(questionBank.getQuestionBankId());

                    List<QuestionOptionResponseDto> questionOptionResponseDtoList = questionOptionList
                            .stream()
                            .map(questionOptionMapper::toQuestionOptionResponseDto)
                            .collect(Collectors.toList());

                    return questionBankMapper.toQuestionBankResponseDto(questionBank,
                            questionOptionResponseDtoList);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Integer getQuestionNoByQuestionBankId(Long questionBankId) {
        log.info("Fetching question number for question bank ID: {}", questionBankId);
        return questionRepository.findQuestionNoByQuestionBankId(questionBankId).orElseThrow(
                () -> {
                    log.error("Question not found for question bank ID: {}", questionBankId);
                    return new NotFoundExceptionMapper(ErrorMessage.QUESTION_NOT_FOUND);
                }
        );
    }

    @Transactional
    public void createQuestion(List<QuestionRequestDto> questionRequestDtoList, Survey survey) {
        log.info("Creating questions for survey ID: {}", survey.getSurveyId());
        for (QuestionRequestDto questionRequestDto : questionRequestDtoList) {
            QuestionBank questionBank = questionBankRepository.save(
                    questionBankMapper.toQuestionBank(questionRequestDto));

            questionRepository.save(questionMapper.toQuestion(questionRequestDto, survey,
                    questionBank));

            if (questionRequestDto.getQuestionOptions() != null) {
                questionOptionService.createQuestionOption(questionRequestDto, questionBank);
            }
        }
        log.info("Questions created for survey ID: {}", survey.getSurveyId());
    }

    @Transactional
    public void updateQuestion(Long surveyId,
                               List<QuestionBankUpdateRequestDto> questionBankUpdateRequestDtoList) {
        log.info("Updating questions for survey ID: {}", surveyId);
        for (QuestionBankUpdateRequestDto questionBankUpdateRequestDto : questionBankUpdateRequestDtoList) {
            QuestionBank questionBank = questionBankRepository.findByQuestionBankId(
                    questionBankUpdateRequestDto.getQuestionBankId()).orElseThrow(
                    () -> {
                        log.error("Question bank not found with ID: {}", questionBankUpdateRequestDto.getQuestionBankId());
                        return new BadRequestExceptionMapper(ErrorMessage.QUESTION_BANK_NOT_FOUND);
                    });

            Optional<Question> question = questionRepository.findBySurveyIdAndQuestionBankId(surveyId, questionBank.getQuestionBankId());
            if (question.isEmpty()) {
                log.error("Question not found for survey ID: {} and question bank ID: {}", surveyId, questionBank.getQuestionBankId());
                throw new BadRequestExceptionMapper(ErrorMessage.QUESTION_NOT_FOUND);
            }

            questionBank.changeTitle(StringUtil.trim(questionBankUpdateRequestDto.getTitle()));
            questionBank.changeDescription(
                    StringUtil.trim(questionBankUpdateRequestDto.getDescription()));

            if (questionBankUpdateRequestDto.getQuestionType() != null) {
                questionBank.changeQuestionType(questionBankUpdateRequestDto.getQuestionType());
            }

            if (questionBankUpdateRequestDto.getIsRequired() != null) {
                question.get().changeIsRequired(questionBankUpdateRequestDto.getIsRequired());
            }
            if (questionBankUpdateRequestDto.getQuestionNo() != null) {
                question.get().changeQuestionNo(questionBankUpdateRequestDto.getQuestionNo());
            }
            if (questionBankUpdateRequestDto.getQuestionOptions() != null) {
                questionOptionService.updateQuestionOption(questionBank.getQuestionBankId(),
                        questionBankUpdateRequestDto.getQuestionOptions());
            }
        }
        log.info("Questions updated for survey ID: {}", surveyId);
    }

    @Transactional
    public void deleteQuestion(Long surveyId) {
        log.info("Deleting questions for survey ID: {}", surveyId);
        List<Question> questionList = questionRepository.findAllBySurveyId(surveyId);
        questionRepository.deleteAll(questionList);
        log.info("Questions deleted for survey ID: {}", surveyId);
    }
}