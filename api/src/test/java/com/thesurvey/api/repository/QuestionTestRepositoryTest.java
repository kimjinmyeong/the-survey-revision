package com.thesurvey.api.repository;

import com.thesurvey.api.domain.QuestionTest;
import com.thesurvey.api.domain.Survey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class QuestionTestRepositoryTest {
    @Autowired
    SurveyRepository surveyRepository;

    @Autowired
    QuestionTestRepository questionTestRepository;

    @Test
    void testCreate(){
        Survey survey = surveyRepository.save(Survey.builder().build());

        QuestionTest given = QuestionTest.builder()
                .survey(survey)
                .build();

        questionTestRepository.save(given);
    }

}