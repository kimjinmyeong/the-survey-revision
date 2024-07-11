package com.thesurvey.api.service;

import com.thesurvey.api.controller.BaseControllerTest;
import com.thesurvey.api.domain.EnumTypeEntity;
import com.thesurvey.api.domain.User;
import com.thesurvey.api.dto.request.question.QuestionBankUpdateRequestDto;
import com.thesurvey.api.dto.request.question.QuestionOptionRequestDto;
import com.thesurvey.api.dto.request.question.QuestionOptionUpdateRequestDto;
import com.thesurvey.api.dto.request.question.QuestionRequestDto;
import com.thesurvey.api.dto.request.survey.SurveyRequestDto;
import com.thesurvey.api.dto.request.survey.SurveyUpdateRequestDto;
import com.thesurvey.api.dto.request.user.UserLoginRequestDto;
import com.thesurvey.api.dto.request.user.UserRegisterRequestDto;
import com.thesurvey.api.dto.response.survey.SurveyListPageDto;
import com.thesurvey.api.dto.response.survey.SurveyResponseDto;
import com.thesurvey.api.repository.SurveyRepository;
import com.thesurvey.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ActiveProfiles("revision")
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SurveyServiceCacheTest extends BaseControllerTest {

    @Autowired
    SurveyService surveyService;

    @Autowired
    SurveyRepository surveyRepository;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CacheManager cacheManager;

    Authentication authentication;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    User user;

    QuestionOptionRequestDto questionOptionRequestDto;

    QuestionRequestDto questionRequestDto;

    SurveyRequestDto surveyRequestDto;

    @PersistenceContext
    EntityManager entityManager;

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
    public void testSurveyCreateCache() throws Exception {
        // given
        SecurityContextHolder.getContext().setAuthentication(authentication);
        surveyService.createSurvey(surveyRequestDto);
        assertThat(cacheManager.getCache("surveyListCache").get(1)).isNull();

        // when
        SurveyListPageDto firstCall = surveyService.getAllSurvey(1);

        // then
        assertThat(cacheManager.getCache("surveyListCache").get(1)).isNotNull();
    }

    @Test
    public void testSurveyUpdateCacheEvict() throws Exception {
        // given
        SecurityContextHolder.getContext().setAuthentication(authentication);
        SurveyResponseDto surveyResponseDto = surveyService.createSurvey(surveyRequestDto);
        Long questionId = surveyResponseDto.getQuestions().get(0).getQuestionBankId();

        QuestionOptionUpdateRequestDto questionOptionUpdateRequestDto = QuestionOptionUpdateRequestDto.builder()
                .optionId(questionId)
                .option("This is test update option")
                .description("This is test update option description")
                .build();

        QuestionBankUpdateRequestDto questionBankUpdateRequestDto = QuestionBankUpdateRequestDto.builder()
                .questionBankId(questionId)
                .title("This is test update question title")
                .description("This is test update question description")
                .questionNo(1)
                .questionOptions(List.of(questionOptionUpdateRequestDto))
                .build();

        SurveyUpdateRequestDto surveyUpdateRequestDto = SurveyUpdateRequestDto.builder()
                .surveyId(surveyResponseDto.getSurveyId())
                .title("This is test update title.")
                .description("This is test update description")
                .startedDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(3))
                .endedDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(4))
                .certificationTypes(List.of(EnumTypeEntity.CertificationType.DRIVER_LICENSE))
                .questions(List.of(questionBankUpdateRequestDto))
                .build();

        SurveyListPageDto firstCall = surveyService.getAllSurvey(1);
        assertThat(cacheManager.getCache("surveyListCache").get(1)).isNotNull();

        // when
        surveyService.updateSurvey(surveyUpdateRequestDto);

        // then
        assertThat(cacheManager.getCache("surveyListCache").get(1)).isNull();
    }

    @Test
    public void testSurveyDeleteCacheEvict() throws Exception {
        // given
        SecurityContextHolder.getContext().setAuthentication(authentication);
        SurveyResponseDto surveyResponseDto = surveyService.createSurvey(surveyRequestDto);

        SurveyListPageDto firstCall = surveyService.getAllSurvey(1);
        assertThat(cacheManager.getCache("surveyListCache").get(1)).isNotNull();

        // when
        surveyService.deleteSurvey(authentication, surveyResponseDto.getSurveyId());

        // then
        assertThat(cacheManager.getCache("surveyListCache").get(1)).isNull();
    }
}
