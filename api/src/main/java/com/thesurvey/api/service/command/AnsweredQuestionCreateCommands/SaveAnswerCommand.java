package com.thesurvey.api.service.command.AnsweredQuestionCreateCommands;

import com.thesurvey.api.domain.*;
import com.thesurvey.api.dto.request.answeredQuestion.AnsweredQuestionDto;
import com.thesurvey.api.dto.request.answeredQuestion.AnsweredQuestionRequestDto;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.BadRequestExceptionMapper;
import com.thesurvey.api.exception.mapper.NotFoundExceptionMapper;
import com.thesurvey.api.repository.AnsweredQuestionRepository;
import com.thesurvey.api.repository.QuestionBankRepository;
import com.thesurvey.api.repository.QuestionRepository;
import com.thesurvey.api.service.command.Command;
import com.thesurvey.api.service.mapper.AnsweredQuestionMapper;
import com.thesurvey.api.util.PointUtil;
import com.thesurvey.api.util.StringUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SaveAnswerCommand implements Command {

    private final AnsweredQuestionRepository answeredQuestionRepository;
    private final AnsweredQuestionMapper answeredQuestionMapper;
    private final QuestionRepository questionRepository;
    private final QuestionBankRepository questionBankRepository;
    private final AnsweredQuestionRequestDto answeredQuestionRequestDto;
    private final User user;
    private final Survey survey;

    @Getter
    private int rewardPoints;

    @Override
    public void execute() {
        boolean isAnswered = false;
        for (AnsweredQuestionDto answeredQuestionDto : answeredQuestionRequestDto.getAnswers()) {

            // Check if the question is required and has an empty answer.
            if (answeredQuestionDto.getIsRequired() && validateEmptyAnswer(answeredQuestionDto)) {
                throw new BadRequestExceptionMapper(ErrorMessage.NOT_ANSWER_TO_REQUIRED_QUESTION);
            }

            // Set isAnswered to true if there is at least one non-empty answer.
            if (!isAnswered && !validateEmptyAnswer(answeredQuestionDto)) {
                isAnswered = true;
            }

            QuestionBank questionBank = questionBankRepository.findByQuestionBankId(answeredQuestionDto.getQuestionBankId())
                    .orElseThrow(() -> new NotFoundExceptionMapper(ErrorMessage.QUESTION_BANK_NOT_FOUND));
            Optional<Question> question = questionRepository.findBySurveyIdAndQuestionBankId(survey.getSurveyId(), questionBank.getQuestionBankId());
            if (question.isEmpty()) {
                throw new BadRequestExceptionMapper(ErrorMessage.NOT_SURVEY_QUESTION);
            }

            // If there are no multiple choices, save the answered question
            if (answeredQuestionDto.getMultipleChoices() == null || answeredQuestionDto.getMultipleChoices().isEmpty()) {
                answeredQuestionRepository.save(answeredQuestionMapper.toAnsweredQuestion(answeredQuestionDto, user, question.get()));
            } else {
                // If there are multiple choices, map each choice to an answered question and save them all
                List<AnsweredQuestion> answeredQuestionList = answeredQuestionDto.getMultipleChoices()
                        .stream()
                        .map(choice -> answeredQuestionMapper.toAnsweredQuestionWithMultipleChoices(user, question.get(), choice))
                        .collect(Collectors.toList());
                answeredQuestionRepository.saveAll(answeredQuestionList);
            }
            // Accumulate reward points based on the answered question
            rewardPoints += getQuestionBankRewardPoints(answeredQuestionDto);
        }
        // Throw an exception if no question was answered
        if (!isAnswered) {
            throw new BadRequestExceptionMapper(ErrorMessage.ANSWER_AT_LEAST_ONE_QUESTION);
        }
    }

    private boolean validateEmptyAnswer(AnsweredQuestionDto answeredQuestionDto) {
        return (answeredQuestionDto.getLongAnswer() == null
                || StringUtil.trimShortLongAnswer(answeredQuestionDto.getLongAnswer(),
                answeredQuestionDto.getIsRequired()).isEmpty())
                && (answeredQuestionDto.getShortAnswer() == null
                || StringUtil.trimShortLongAnswer(answeredQuestionDto.getShortAnswer(),
                answeredQuestionDto.getIsRequired()).isEmpty())
                && answeredQuestionDto.getSingleChoice() == null
                && (answeredQuestionDto.getMultipleChoices() == null || answeredQuestionDto.getMultipleChoices().isEmpty());
    }

    private int getQuestionBankRewardPoints(AnsweredQuestionDto answeredQuestionDto) {
        if (!validateEmptyAnswer(answeredQuestionDto)) {
            return PointUtil.calculateSurveyMaxRewardPoints(answeredQuestionDto.getQuestionType());
        }
        return 0;
    }
}

