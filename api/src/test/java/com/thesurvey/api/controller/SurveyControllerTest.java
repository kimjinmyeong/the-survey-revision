package com.thesurvey.api.controller;

import com.thesurvey.api.domain.AnsweredQuestion;
import com.thesurvey.api.domain.EnumTypeEntity.CertificationType;
import com.thesurvey.api.domain.EnumTypeEntity.PointTransactionType;
import com.thesurvey.api.domain.EnumTypeEntity.QuestionType;
import com.thesurvey.api.domain.Survey;
import com.thesurvey.api.dto.request.answeredQuestion.AnsweredQuestionDto;
import com.thesurvey.api.dto.request.answeredQuestion.AnsweredQuestionRequestDto;
import com.thesurvey.api.dto.request.question.QuestionBankUpdateRequestDto;
import com.thesurvey.api.dto.request.question.QuestionOptionRequestDto;
import com.thesurvey.api.dto.request.question.QuestionOptionUpdateRequestDto;
import com.thesurvey.api.dto.request.question.QuestionRequestDto;
import com.thesurvey.api.dto.request.survey.SurveyRequestDto;
import com.thesurvey.api.dto.request.survey.SurveyUpdateRequestDto;
import com.thesurvey.api.dto.request.user.UserCertificationUpdateRequestDto;
import com.thesurvey.api.dto.request.user.UserLoginRequestDto;
import com.thesurvey.api.dto.request.user.UserRegisterRequestDto;
import com.thesurvey.api.repository.*;
import com.thesurvey.api.service.AnsweredQuestionService;
import com.thesurvey.api.service.AuthenticationService;
import com.thesurvey.api.service.SurveyService;
import com.thesurvey.api.service.mapper.QuestionBankMapper;
import com.thesurvey.api.service.mapper.QuestionMapper;
import com.thesurvey.api.service.mapper.SurveyMapper;
import com.thesurvey.api.util.UserUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestInstance(Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
@WithMockUser
public class SurveyControllerTest extends BaseControllerTest {

    @Autowired
    SurveyRepository surveyRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    QuestionBankRepository questionBankRepository;

    @Autowired
    SurveyService surveyService;

    @Autowired
    SurveyMapper surveyMapper;

    @Autowired
    QuestionMapper questionMapper;

    @Autowired
    QuestionBankMapper questionBankMapper;

    @Autowired
    ParticipationRepository participationRepository;

    @Autowired
    QuestionOptionRepository questionOptionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    AnsweredQuestionService answeredQuestionService;

    SurveyRequestDto surveyRequestDto;

    QuestionRequestDto questionRequestDto;

    QuestionOptionRequestDto questionOptionRequestDto;

    JSONObject mockSurvey;

    Authentication authentication;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @BeforeAll
    void setupBeforeAll() throws Exception {
        UserRegisterRequestDto userRegisterRequestDto = UserRegisterRequestDto.builder()
                .name("test1")
                .email("test1@gmail.com")
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
                .questionType(QuestionType.SINGLE_CHOICE)
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
                .certificationTypes(List.of(CertificationType.GOOGLE))
                .questions(List.of(questionRequestDto))
                .build();
    }

    @BeforeEach
    void setupBeforeEach() throws Exception {
        UserLoginRequestDto userLoginRequestDto = UserLoginRequestDto.builder()
                .email("test1@gmail.com")
                .password("Password40@")
                .build();
        mockLogin(userLoginRequestDto, true);

        authentication = authenticationService.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginRequestDto.getEmail(),
                        userLoginRequestDto.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        MvcResult createdSurvey = mockCreateSurvey(surveyRequestDto);
        mockSurvey = new JSONObject(createdSurvey.getResponse().getContentAsString());

    }

    @Test
    void testGetSpecificSurvey() throws Exception {
        // given
        Long surveyId = mockSurvey.getLong("surveyId");

        // when
        MvcResult result = mockMvc.perform(get("/surveys/" + surveyId))
                .andExpect(status().isOk()).andReturn();
        JSONObject content = new JSONObject(result.getResponse().getContentAsString());

        // then
        assertThat(surveyId.toString()).isEqualTo(content.getString("surveyId"));
        assertThat(surveyRequestDto.getTitle()).isEqualTo(content.get("title"));
        assertThat(surveyRequestDto.getStartedDate().format(formatter))
                .isEqualTo(content.getString("startedDate"));
        assertThat(surveyRequestDto.getEndedDate().format(formatter))
                .isEqualTo(content.getString("endedDate"));
        assertThat(LocalDateTime.parse(content.getString("startedDate"))).isBefore(
                LocalDateTime.parse(content.getString("endedDate")));
        assertThat(content.getJSONArray("questions")).isNotNull();
        assertThat(content.get("rewardPoints")).isEqualTo(1);
    }

    @Test
    void testSubmitSurvey() throws Exception {
        // given
        // Create a survey in progress.
        UserRegisterRequestDto submitAnswerUserRegisterRequestDto = UserRegisterRequestDto.builder()
                .name("submitAnswerUser")
                .email("submitAnswerUser@gmail.com")
                .password("Password40@")
                .phoneNumber("01012345678")
                .build();
        mockRegister(submitAnswerUserRegisterRequestDto, true);

        UserLoginRequestDto submitAnswerUserLoginRequestDto = UserLoginRequestDto.builder()
                .email("submitAnswerUser@gmail.com")
                .password("Password40@")
                .build();
        mockLogin(submitAnswerUserLoginRequestDto, true);
        Authentication authentication = authenticationService.authenticate(
                new UsernamePasswordAuthenticationToken(submitAnswerUserLoginRequestDto.getEmail(),
                        submitAnswerUserLoginRequestDto.getPassword())
        );

        JSONArray questions = mockSurvey.getJSONArray("questions");
        JSONObject questionBank = questions.getJSONObject(0);
        JSONArray questionOptions = questionBank.getJSONArray("questionOptions");
        JSONObject questionOption = questionOptions.getJSONObject(0);
        AnsweredQuestionDto answeredQuestionDto = AnsweredQuestionDto.builder()
                .questionBankId(questionBank.getLong("questionBankId"))
                .singleChoice(questionOption.getLong("questionOptionId"))
                .isRequired(true)
                .questionType(QuestionType.SINGLE_CHOICE)
                .build();

        AnsweredQuestionRequestDto answeredQuestionRequestDto = AnsweredQuestionRequestDto.builder()
                .surveyId(mockSurvey.getLong("surveyId"))
                .answers(List.of(answeredQuestionDto))
                .build();

        UserCertificationUpdateRequestDto userCertificationUpdateRequestDto = UserCertificationUpdateRequestDto.builder()
                .isKakaoCertificated(true)
                .isNaverCertificated(true)
                .isGoogleCertificated(true)
                .isWebMailCertificated(true)
                .isDriverLicenseCertificated(true)
                .isIdentityCardCertificated(true)
                .build();

        mockMvc.perform(patch("/users/profile/certifications")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCertificationUpdateRequestDto)))
                .andExpect(status().isOk());

        // when
        MvcResult submitResult = mockMvc.perform(post("/surveys/submit")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answeredQuestionRequestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // then
        List<AnsweredQuestion> resultAnsweredQuestion = answeredQuestionService.getAnswerQuestionByQuestionBankId(
                questionBank.getLong("questionBankId"));
        JSONObject rewardPoints = new JSONObject(submitResult.getResponse().getContentAsString());
        assertThat(rewardPoints.get("rewardPoints")).isEqualTo(PointTransactionType.SINGLE_CHOICE_REWARD.getTransactionPoint());
        assertThat(resultAnsweredQuestion.size()).isEqualTo(1);
        assertThat(resultAnsweredQuestion.get(0).getSingleChoice()).isEqualTo(questionOption.getLong("questionOptionId"));
    }

    @Test
    void testUpdateSurvey() throws Exception {
        // given
        Long surveyId = mockSurvey.getLong("surveyId");
        Long userId = UserUtil.getUserIdFromAuthentication(authentication);
        Long authorId = mockSurvey.getLong("authorId");

        JSONArray questions = mockSurvey.getJSONArray("questions");
        JSONObject questionBank = (JSONObject) questions.get(0);
        Long questionBankId = questionBank.getLong("questionBankId");

        JSONArray questionOptions = questionBank.getJSONArray("questionOptions");
        JSONObject questionOption = (JSONObject) questionOptions.get(0);
        Long questionOptionId = questionOption.getLong("questionOptionId");

        QuestionOptionUpdateRequestDto questionOptionUpdateRequestDto = QuestionOptionUpdateRequestDto.builder()
                .optionId(questionOptionId)
                .option("This is test update option")
                .description("This is test update option description")
                .build();

        QuestionBankUpdateRequestDto questionBankUpdateRequestDto = QuestionBankUpdateRequestDto.builder()
                .questionBankId(questionBankId)
                .title("This is test update question title")
                .description("This is test update question description")
                .questionNo(1)
                .questionOptions(List.of(questionOptionUpdateRequestDto))
                .build();

        SurveyUpdateRequestDto surveyUpdateRequestDto = SurveyUpdateRequestDto.builder()
                .surveyId(surveyId)
                .title("This is test update title.")
                .description("This is test update description")
                .startedDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(3))
                .endedDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(4))
                .certificationTypes(List.of(CertificationType.DRIVER_LICENSE))
                .questions(List.of(questionBankUpdateRequestDto))
                .build();

        // when
        MvcResult result = mockMvc.perform(patch("/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(surveyUpdateRequestDto)))
                .andExpect(status().isOk())
                .andReturn();
        JSONObject content = new JSONObject(result.getResponse().getContentAsString());

        // then
        assertThat(userId).isEqualTo(authorId);
        assertThat(surveyId.toString()).isEqualTo(content.getString("surveyId"));
        assertThat(surveyUpdateRequestDto.getTitle()).isEqualTo(content.get("title"));
        assertThat(surveyUpdateRequestDto.getStartedDate().format(formatter)).isEqualTo(
                content.getString("startedDate"));
        assertThat(surveyUpdateRequestDto.getEndedDate().format(formatter)).isEqualTo(
                content.getString("endedDate"));
        assertThat(LocalDateTime.parse(content.getString("startedDate"))).isBefore(
                LocalDateTime.parse(content.getString("endedDate")));
    }

    @Test
    void testGetAllSurvey() throws Exception {
        // given
        for (int i = 1; i < 16; i++) { // create test 15 surveys
            surveyRepository.save(Survey.builder()
                    .title("This is test survey title " + i)
                    .authorId(1L)
                    .description("This is test survey description")
                    .startedDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(1))
                    .endedDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(2))
                    .build());
        }

        // when
        MvcResult result = mockMvc.perform(get("/surveys")
                        .param("page", "2"))
                .andExpect(status().isOk()).andReturn();
        JSONObject content = new JSONObject(result.getResponse().getContentAsString());
        JSONArray paginatedSurveyList = content.getJSONArray("surveys");

        // then
        assertThat(content.get("totalSurveys")).isEqualTo(16); // mockSurvey 1 + test surveys 15
        assertThat(content.get("totalPages")).isEqualTo(2); // total elements / page size
    }

    @Test
    void testDeleteSurvey() throws Exception {
        // given
        UserRegisterRequestDto userRegisterRequestDto = UserRegisterRequestDto.builder()
                .name("test2")
                .email("test2@gmail.com")
                .password("Password40@")
                .phoneNumber("01012345678")
                .build();
        mockRegister(userRegisterRequestDto, true);

        UserLoginRequestDto userLoginRequestDto = UserLoginRequestDto.builder()
                .email("test2@gmail.com")
                .password("Password40@")
                .build();
        mockLogin(userLoginRequestDto, true);

        authentication = authenticationService.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginRequestDto.getEmail(),
                        userLoginRequestDto.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        SurveyRequestDto surveyRequestDto = SurveyRequestDto.builder()
                .title("This is test survey title")
                .description("This is test survey description")
                .startedDate(LocalDateTime.parse(LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                        .format(formatter)).plusDays(1))
                .endedDate(LocalDateTime.parse(LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                        .plusDays(2).format(formatter)))
                .certificationTypes(List.of(CertificationType.GOOGLE))
                .questions(List.of(questionRequestDto))
                .build();

        MvcResult createdSurvey = mockCreateSurvey(surveyRequestDto);
        mockSurvey = new JSONObject(createdSurvey.getResponse().getContentAsString());

        Long surveyId = mockSurvey.getLong("surveyId");
        Long authorId = mockSurvey.getLong("authorId");
        Long userId = UserUtil.getUserIdFromAuthentication(authentication);

        // when
        mockMvc.perform(delete("/surveys/" + surveyId)).andExpect(status().isNoContent());

        // then
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(userId).isEqualTo(authorId);
        assertThat(surveyRepository.findBySurveyId(surveyId).isEmpty()).isTrue();

    }
}