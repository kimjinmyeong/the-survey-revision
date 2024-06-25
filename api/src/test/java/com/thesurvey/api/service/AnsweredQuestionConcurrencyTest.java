package com.thesurvey.api.service;

import com.thesurvey.api.controller.BaseControllerTest;
import com.thesurvey.api.domain.AnsweredQuestion;
import com.thesurvey.api.domain.EnumTypeEntity;
import com.thesurvey.api.domain.User;
import com.thesurvey.api.domain.UserCertification;
import com.thesurvey.api.dto.request.answeredQuestion.AnsweredQuestionDto;
import com.thesurvey.api.dto.request.answeredQuestion.AnsweredQuestionRequestDto;
import com.thesurvey.api.dto.request.question.QuestionOptionRequestDto;
import com.thesurvey.api.dto.request.question.QuestionRequestDto;
import com.thesurvey.api.dto.request.survey.SurveyRequestDto;
import com.thesurvey.api.dto.request.user.UserLoginRequestDto;
import com.thesurvey.api.dto.request.user.UserRegisterRequestDto;
import com.thesurvey.api.repository.AnsweredQuestionRepository;
import com.thesurvey.api.repository.UserCertificationRepository;
import com.thesurvey.api.repository.UserRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
@WithMockUser
public class AnsweredQuestionConcurrencyTest extends BaseControllerTest {

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SurveyService surveyService;

    @Autowired
    AnsweredQuestionService answeredQuestionService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    AuthenticationManager authenticationManager;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    ExecutorService executorService;

    CountDownLatch latch;

    JSONObject mockSurvey;

    QuestionOptionRequestDto questionOptionRequestDto;

    QuestionRequestDto questionRequestDto;

    SurveyRequestDto surveyRequestDto;

    @Autowired
    private UserCertificationRepository userCertificationRepository;
    @Autowired
    private AnsweredQuestionRepository answeredQuestionRepository;

    @BeforeAll
    void setupBeforeAll() throws Exception {
        UserRegisterRequestDto userRegisterRequestDto = UserRegisterRequestDto.builder()
                .name("author")
                .email("author@gmail.com")
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
                .questionType(EnumTypeEntity.QuestionType.SINGLE_CHOICE) //
                .questionOptions(List.of(questionOptionRequestDto))
                .isRequired(true)
                .build();

        surveyRequestDto = SurveyRequestDto.builder()
                .title("This is test survey title")
                .description("This is test survey description")
                .startedDate(LocalDateTime.parse(LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                        .format(formatter)))
                .endedDate(LocalDateTime.parse(LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                        .plusDays(2).format(formatter)))
                .certificationTypes(List.of(EnumTypeEntity.CertificationType.GOOGLE))
                .questions(List.of(questionRequestDto))
                .build();

    }

    @BeforeEach
    void setupBeforeEach() throws Exception {
        UserLoginRequestDto userLoginRequestDto = UserLoginRequestDto.builder()
                .email("author@gmail.com")
                .password("Password40@")
                .build();
        mockLogin(userLoginRequestDto, true);

        MvcResult createdSurvey = mockCreateSurvey(surveyRequestDto);
        mockSurvey = new JSONObject(createdSurvey.getResponse().getContentAsString());
    }

    @Test
    public void testConcurrentSubmitAnswer() throws Exception {
        // given
        // Create a user to submit survey.
        UserRegisterRequestDto submitRegisterRequestDto = UserRegisterRequestDto.builder()
                .name("submit")
                .email("submit@gmail.com")
                .password("Password40@")
                .phoneNumber("01012345678")
                .build();
        mockRegister(submitRegisterRequestDto, true);
        UserLoginRequestDto submitLoginRequestDto = UserLoginRequestDto.builder()
                .email("submit@gmail.com")
                .password("Password40@")
                .build();
        mockLogin(submitLoginRequestDto, true);

        User submitUser = userRepository.findByEmail("submit@gmail.com").get();

        Authentication submitAuthentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                submitLoginRequestDto.getEmail(),
                submitLoginRequestDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(submitAuthentication);

        userCertificationRepository.save(UserCertification.builder()
                .user(submitUser)
                .certificationType(EnumTypeEntity.CertificationType.GOOGLE)
                .certificationDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .expirationDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusYears(2))
                .build());

        JSONArray questions = mockSurvey.getJSONArray("questions");
        JSONObject questionBank = questions.getJSONObject(0);
        JSONArray questionOptions = questionBank.getJSONArray("questionOptions");
        JSONObject questionOption = questionOptions.getJSONObject(0);

        AnsweredQuestionDto answeredQuestionDto = AnsweredQuestionDto.builder()
                .questionBankId(questionBank.getLong("questionBankId"))
                .singleChoice(questionOption.getLong("questionOptionId"))
                .isRequired(true)
                .questionType(EnumTypeEntity.QuestionType.SINGLE_CHOICE)
                .build();

        AnsweredQuestionRequestDto answeredQuestionRequestDto = AnsweredQuestionRequestDto.builder()
                .surveyId(mockSurvey.getLong("surveyId"))
                .answers(List.of(answeredQuestionDto))
                .build();

        int threadCount = 20;
        executorService = Executors.newFixedThreadPool(threadCount);
        latch = new CountDownLatch(threadCount);

        Runnable task = () -> {
            try {
                SecurityContextHolder.getContext().setAuthentication(submitAuthentication);
                answeredQuestionService.createAnswer(answeredQuestionRequestDto);
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
        User afterSubmitUser = userRepository.findByEmail(submitLoginRequestDto.getEmail()).get();
        assertThat(afterSubmitUser.getPoint()).isEqualTo(51); // User Initial Point 50 + Single Choice reward 1
        List<AnsweredQuestion> answeredQuestionList = answeredQuestionRepository.findAll();
        assertThat(answeredQuestionList.size()).isEqualTo(1);

    }

}
