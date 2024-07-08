package com.thesurvey.api.repository;

import com.thesurvey.api.domain.Survey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("revision")
@DirtiesContext
public class SurveyRepositoryTest {

    @Autowired
    private SurveyRepository surveyRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    public void setup() {
        // given
        for (int i = 1; i < 1000; i++) {
            Survey survey = Survey.builder()
                    .title("This is test survey title " + i)
                    .authorId(1L)
                    .description("This is test survey description")
                    .startedDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(i))
                    .endedDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(i * i))
                    .createdDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(i * i % 23))
                    .build();
            entityManager.persist(survey);
        }
        entityManager.flush();
    }

    @Test
    public void testQueryPerformanceUsingIndex() {
//        entityManager.createNativeQuery("DROP INDEX IF EXISTS idx_survey_created_date_desc").executeUpdate();
//        entityManager.createNativeQuery("DROP INDEX IF EXISTS idx_survey_ended_date").executeUpdate();
//        entityManager.flush();

        long startTime = System.currentTimeMillis();

        List<Survey> surveys = surveyRepository.findAllInDescendingOrder(PageRequest.of(0, 10)).getContent();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Execution time before index: " + duration + " ms");
        assertThat(surveys).isNotNull();
    }

}
