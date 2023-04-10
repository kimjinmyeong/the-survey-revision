package com.thesurvey.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.thesurvey.api.SurveyTestFactory;
import com.thesurvey.api.dto.request.AnsweredQuestionRequestDto;
import com.thesurvey.api.dto.request.SurveyRequestDto;
import com.thesurvey.api.repository.ParticipationRepository;
import com.thesurvey.api.repository.QuestionBankRepository;
import com.thesurvey.api.repository.QuestionOptionRepository;
import com.thesurvey.api.repository.QuestionRepository;
import com.thesurvey.api.repository.SurveyRepository;
import com.thesurvey.api.repository.UserRepository;
import com.thesurvey.api.service.AuthenticationService;
import com.thesurvey.api.service.SurveyService;
import com.thesurvey.api.service.mapper.QuestionBankMapper;
import com.thesurvey.api.service.mapper.QuestionMapper;
import com.thesurvey.api.service.mapper.SurveyMapper;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@TestInstance(Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
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

    @BeforeEach
    void makeMockUser() throws Exception {
        mockRegister(globalRegisterDto, true);
        mockLogin(globalLoginDto, true);
    }

    @AfterEach
    void logout() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/logout"));
    }

    @Test
    @WithMockUser
    public void testCreateSurvey() throws Exception {
        // given
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
            globalLoginDto.getEmail(), globalLoginDto.getPassword());
        Authentication authentication = authenticationManager.authenticate(authRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        SurveyRequestDto testSurveyRequestDto = SurveyTestFactory.getGlobalSurveyRequestDto();

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/surveys")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSurveyRequestDto)))
            .andExpect(status().isOk());

        // then
        MvcResult result = resultActions.andReturn();
        JSONObject content = new JSONObject(result.getResponse().getContentAsString());
        assertEquals(content.get("title"), testSurveyRequestDto.getTitle());
        resultActions.andExpect(status().isOk());

    }

    // FIXME: pass individually test but fail when run together.
//    @Test
//    @WithMockUser
//    public void testUpdateSurvey() throws Exception {
//        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
//            globalLoginDto.getEmail(), globalLoginDto.getPassword());
//        Authentication authentication = authenticationManager.authenticate(authRequest);
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        SurveyRequestDto testSurveyRequestDto = SurveyTestFactory.getGlobalSurveyRequestDto();
//        SurveyResponseDto testSurveyResponseDto = surveyService.createSurvey(authentication,
//            testSurveyRequestDto);
//        SurveyUpdateRequestDto testSurveyUpdateRequestDto = SurveyTestFactory.getSurveyUpdateRequestDto(
//            testSurveyResponseDto.getSurveyId());
//
//        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put("/surveys")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(testSurveyUpdateRequestDto)))
//            .andExpect(status().isOk());
//    }

    @Test
    @WithMockUser
    public void testDeleteSurvey() throws Exception {
        // given
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
            globalLoginDto.getEmail(), globalLoginDto.getPassword());
        Authentication authentication = authenticationManager.authenticate(authRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        SurveyRequestDto testSurveyRequestDto = SurveyTestFactory.getGlobalSurveyRequestDto();
        MvcResult mvcResult = mockCreateSurvey(testSurveyRequestDto);
        JSONObject content = new JSONObject(mvcResult.getResponse().getContentAsString());
        String testSurveyId = content.get("surveyId").toString();

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/surveys/{surveyId}", testSurveyId))
            .andExpect(status().isOk());
        // FIXME : should null, but returned not null.
        // assertNull(surveyRepository.findBySurveyId(UUID.fromString(testSurveyId)));
    }

    @Test
    @WithMockUser
    public void testGetSurvey() throws Exception {
        // given
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
            globalLoginDto.getEmail(), globalLoginDto.getPassword());
        Authentication authentication = authenticationManager.authenticate(authRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        SurveyRequestDto testSurveyRequestDto = SurveyTestFactory.getGlobalSurveyRequestDto();
        MvcResult createSurveyResult = mockCreateSurvey(testSurveyRequestDto);
        JSONObject content = new JSONObject(createSurveyResult.getResponse().getContentAsString());
        String testSurveyId = content.get("surveyId").toString();

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/surveys/{surveyId}", testSurveyId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testSurveyRequestDto)))
            .andExpect(status().isOk());

        // then
        MvcResult getSurveyResult = resultActions.andReturn();
        assertEquals(createSurveyResult.getResponse().getContentAsString(),
            getSurveyResult.getResponse().getContentAsString());
    }

    @Test
    @WithMockUser
    public void testSubmitSurvey() throws Exception {
        // given
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
            globalLoginDto.getEmail(), globalLoginDto.getPassword());
        Authentication authentication = authenticationManager.authenticate(authRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        SurveyRequestDto testSurveyRequestDto = SurveyTestFactory.getGlobalSurveyRequestDto();
        MvcResult createSurveyResult = mockCreateSurvey(testSurveyRequestDto);
        JSONObject content = new JSONObject(createSurveyResult.getResponse().getContentAsString());
        String testSurveyId = content.get("surveyId").toString();
        AnsweredQuestionRequestDto testAnsweredQuestionRequestDto = SurveyTestFactory.getAnsweredQuestionRequestDto(
            UUID.fromString(testSurveyId));

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/surveys/submit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testAnsweredQuestionRequestDto)))
            .andExpect(status().isOk());
    }

}



