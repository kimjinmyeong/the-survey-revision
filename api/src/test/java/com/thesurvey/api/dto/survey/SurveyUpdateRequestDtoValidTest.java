package com.thesurvey.api.dto.survey;

import com.thesurvey.api.controller.SurveyController;
import com.thesurvey.api.domain.EnumTypeEntity.QuestionType;
import com.thesurvey.api.dto.request.question.QuestionBankUpdateRequestDto;
import com.thesurvey.api.dto.request.survey.SurveyUpdateRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(value = SurveyController.class, useDefaultFilters = false)
@MockBean(JpaMetamodelMappingContext.class)
public class SurveyUpdateRequestDtoValidTest {

    final Long surveyId = 1L;

    @Autowired
    private Validator validator;

    @Test
    public void testCorrectInput() {
        // given
        QuestionBankUpdateRequestDto questionBankUpdateRequestDto = QuestionBankUpdateRequestDto.builder()
            .questionBankId(1L)
            .title("This is test QuestionBankUpdateRequestDto")
            .description("This is test QuestionBankUpdateRequestDto")
            .questionType(QuestionType.LONG_ANSWER)
            .questionNo(1)
            .isRequired(true)
            .build();

        SurveyUpdateRequestDto surveyUpdateRequestDto = SurveyUpdateRequestDto.builder()
            .surveyId(surveyId)
            .title("This is test title.")
            .description("This is test description")
            .startedDate(LocalDateTime.now())
            .endedDate(LocalDateTime.now().plusDays(1))
            .questions(Arrays.asList(questionBankUpdateRequestDto))
            .build();

        // when
        Set<ConstraintViolation<SurveyUpdateRequestDto>> validateSet = validator.validate(
            surveyUpdateRequestDto);

        // then
        assertEquals(validateSet.size(), 0);

    }

    @Test
    public void testValidateNotNull() {
        // given
        SurveyUpdateRequestDto surveyUpdateRequestDto = SurveyUpdateRequestDto.builder()
            .surveyId(null) // violated by @NotNull
            .build();

        // when
        Set<ConstraintViolation<SurveyUpdateRequestDto>> validateSet = validator.validate(
            surveyUpdateRequestDto);

        // then
        assertEquals(validateSet.size(), 1); // violated total 1 constraint
    }

    @Test
    public void testValidateOverMaxStringLength() {
        // given
        StringBuilder maxLengthStringBuilder = new StringBuilder();
        while (maxLengthStringBuilder.length() < 256) {
            maxLengthStringBuilder.append("Test String.....");
        }
        String maxLengthString = maxLengthStringBuilder.toString();

        SurveyUpdateRequestDto surveyUpdateRequestDto = SurveyUpdateRequestDto.builder()
            .surveyId(surveyId)
            .title(maxLengthString) // violated by @Size
            .description(maxLengthString) // violated by @Size
            .build();

        // when
        Set<ConstraintViolation<SurveyUpdateRequestDto>> validateSet = validator.validate(
            surveyUpdateRequestDto);

        // then
        assertEquals(validateSet.size(), 2); // violated total 2 constraints
    }

    @Test
    public void testValidateTime() {
        // given
        SurveyUpdateRequestDto surveyUpdateRequestDto = SurveyUpdateRequestDto.builder()
            .surveyId(surveyId)
            .startedDate(LocalDateTime.now())
            .endedDate(LocalDateTime.now().minusDays(1))
            .build();

        // when
        Set<ConstraintViolation<SurveyUpdateRequestDto>> validateSet = validator.validate(
            surveyUpdateRequestDto);

        // then
        assertEquals(validateSet.size(), 1);
    }

}
