package com.thesurvey.api.service;

import com.thesurvey.api.controller.BaseControllerTest;
import com.thesurvey.api.domain.EnumTypeEntity;
import com.thesurvey.api.domain.User;
import com.thesurvey.api.dto.request.question.QuestionOptionRequestDto;
import com.thesurvey.api.dto.request.question.QuestionRequestDto;
import com.thesurvey.api.dto.request.survey.SurveyRequestDto;
import com.thesurvey.api.dto.request.user.UserLoginRequestDto;
import com.thesurvey.api.dto.request.user.UserRegisterRequestDto;
import com.thesurvey.api.repository.PointHistoryRepository;
import com.thesurvey.api.repository.SurveyRepository;
import com.thesurvey.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SurveyConcurrencyTest extends BaseControllerTest {

    @Autowired
    SurveyService surveyService;

    @Autowired
    SurveyRepository surveyRepository;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    PointHistoryRepository pointHistoryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PointHistoryService pointHistoryService;

    Authentication authentication;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    User user;

    QuestionOptionRequestDto questionOptionRequestDto;

    QuestionRequestDto questionRequestDto;

    SurveyRequestDto surveyRequestDto;

    ExecutorService executorService;

    CountDownLatch latch;

    @BeforeAll
    void setUpBeforeAll() throws Exception {
        UserRegisterRequestDto userRegisterRequestDto = UserRegisterRequestDto.builder()
                .name("surveyServiceConcurrent")
                .email("surveyServiceConcurrent@gmail.com")
                .password("Password40@")
                .phoneNumber("01012345678")
                .build();
        mockRegister(userRegisterRequestDto, true);

        questionOptionRequestDto = QuestionOptionRequestDto.builder()
                .option("This is test option")
                .description("This is test option description")
                .build();

        questionRequestDto = QuestionRequestDto.builder()
                .title("This is test question title")
                .description("This is test question description")
                .questionNo(1)
                .questionType(EnumTypeEntity.QuestionType.SINGLE_CHOICE) // need 2 points
                .questionOptions(List.of(questionOptionRequestDto))
                .isRequired(true)
                .build();

        surveyRequestDto = SurveyRequestDto.builder()
                .title("This is test survey title")
                .description("This is test survey description")
                .startedDate(LocalDateTime.parse(LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                        .format(formatter)).plusDays(1))
                .endedDate(LocalDateTime.parse(LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                        .plusDays(2).format(formatter)))
                .certificationTypes(List.of(EnumTypeEntity.CertificationType.GOOGLE))
                .questions(List.of(questionRequestDto))
                .build();
    }
    @BeforeEach
    void setupBeforeEach() throws Exception {

        UserLoginRequestDto userLoginRequestDto = UserLoginRequestDto.builder()
                .email("surveyServiceConcurrent@gmail.com")
                .password("Password40@")
                .build();
        mockLogin(userLoginRequestDto, true);
        user = userRepository.findByEmail(userLoginRequestDto.getEmail()).get();

        authentication = authenticationService.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginRequestDto.getEmail(),
                        userLoginRequestDto.getPassword())
        );
    }

    @Test
    public void testConcurrentSurveyCreation() throws Exception {
        // given
        int threadCount = 20;
        executorService = Executors.newFixedThreadPool(threadCount);
        latch = new CountDownLatch(threadCount);

        Runnable task = () -> {
            try {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                surveyService.createSurvey(surveyRequestDto);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        };

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(task);
        }
        latch.await();

        // then
        long surveys = surveyRepository.count();
        assertThat(surveys).isEqualTo(1);

        Integer userPoint = userRepository.findPointByUserId(user.getUserId());
        assertThat(userPoint).isEqualTo(48);

    }
}
