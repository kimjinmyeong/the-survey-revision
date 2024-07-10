package com.thesurvey.api.service;

import com.thesurvey.api.domain.Survey;
import com.thesurvey.api.dto.response.survey.SurveyListPageDto;
import com.thesurvey.api.repository.SurveyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
public class SurveyServiceCacheTest {

    @Autowired
    private SurveyService surveyService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    SurveyRepository surveyRepository;

    @BeforeEach
    public void setup() {
        cacheManager.getCache("surveyListCache").clear();
    }

    @Test
    public void testCaching() {
        Survey survey = Survey.builder()
                .title("This is test survey title")
                .description("This is test survey description")
                .authorId(1L)
                .startedDate(LocalDateTime.now())
                .endedDate(LocalDateTime.now().plusDays(1))
                .build();
        surveyRepository.save(survey);

        assertThat(cacheManager.getCache("surveyListCache").get(1)).isNull();
        SurveyListPageDto firstCall = surveyService.getAllSurvey(1);
        assertThat(cacheManager.getCache("surveyListCache").get(1)).isNotNull();
    }
}
