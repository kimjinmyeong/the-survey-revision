package simulations;

import io.gatling.javaapi.core.ScenarioBuilder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class SurveySubmitAnswerSimulation extends BaseSimulation {

    private final ScenarioBuilder createSurveyScn;
    private final ScenarioBuilder submitSurveyScn;

    public SurveySubmitAnswerSimulation() {
        createSurveyScn = scenario("Survey Create Scenario")
                .exec(http("User Registration")
                        .post("/auth/register")
                        .headers(headers)
                        .body(RawFileBody("data/register_request.json")).asJson()
                        .check(status().is(200)))
                .pause(1)
                .exec(http("User Login")
                        .post("/auth/login")
                        .headers(headers)
                        .body(RawFileBody("data/login_request.json")).asJson()
                        .check(status().is(200)))
                .pause(1)
                .exec(http("Create Survey")
                        .post("/surveys")
                        .headers(headers)
                        .body(StringBody(session -> {
                            String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            return "{"
                                    + "\"title\": \"카카오 사용자분들께 설문 부탁드립니다!\","
                                    + "\"description\": \"카카오 앱 서비스에 대한 전반적인 만족도 조사입니다.\","
                                    + "\"startedDate\": \"" + now + "\","
                                    + "\"endedDate\": \"2099-07-09T06:17:27.905Z\","
                                    + "\"certificationTypes\": [],"
                                    + "\"questions\": ["
                                    + "  {"
                                    + "    \"title\": \"카카오톡 서비스 사용자이신가요?\","
                                    + "    \"description\": \"카카오톡 사용자여부 확인\","
                                    + "    \"questionType\": \"SINGLE_CHOICE\","
                                    + "    \"questionNo\": 1,"
                                    + "    \"isRequired\": true,"
                                    + "    \"questionOptions\": ["
                                    + "      {"
                                    + "        \"option\": \"예\","
                                    + "        \"description\": \"사용자\""
                                    + "      }"
                                    + "    ]"
                                    + "  }"
                                    + "]"
                                    + "}";
                        })).asJson()
                        .check(status().is(200))
                        .check(jsonPath("$.surveyId").saveAs("surveyId")));

        submitSurveyScn = scenario("Submit Survey Scenario")
                .feed(feeder)
                .exec(http("User Registration")
                        .post("/auth/register")
                        .headers(headers)
                        .body(StringBody(session -> {
                            int count = session.getInt("count");
                            String name = "testUser" + count;
                            String email = "testUser" + count + "@gmail.com";
                            String password = "Password40@";
                            String phoneNumber = "01012345678";
                            return String.format("{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"phoneNumber\":\"%s\"}", name, email, password, phoneNumber);
                        })).asJson()
                        .check(status().is(200)))
                .pause(1)
                .exec(http("User Login")
                        .post("/auth/login")
                        .headers(headers)
                        .body(StringBody(session -> {
                            String email = "testUser" + session.getInt("count") + "@gmail.com";
                            String password = "Password40@";
                            return String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
                        })).asJson()
                        .check(status().is(200))
                        .check(headerRegex("Set-Cookie", "JSESSIONID=(.*?);").saveAs("jsessionid")))
                .pause(1)
                .exec(http("Submit Survey")
                        .post("/surveys/submit")
                        .headers(Map.of("Content-Type", "application/json", "Cookie", "JSESSIONID=${jsessionid}"))
                        .body(RawFileBody("data/submit_request.json")).asJson()
                        .check(status().is(200))
                );
        String testType = System.getProperty("type");
        setupSimulation(testType);
    }

    @Override
    public void loadTestingSetup() {
        setUp(
                createSurveyScn.injectOpen(atOnceUsers(1)),
                submitSurveyScn.injectOpen(
                        nothingFor(5),
                        atOnceUsers(1000),
                        rampUsers(5000).during(Duration.ofSeconds(300))
                )
        ).protocols(httpProtocol);
    }

    @Override
    public void stressTestingSetup() {
        setUp(
                createSurveyScn.injectOpen(atOnceUsers(1)),
                submitSurveyScn.injectOpen(
                        nothingFor(5),
                        rampUsers(10000).during(Duration.ofSeconds(600))
                )
        ).protocols(httpProtocol);
    }

    @Override
    public void soakTestingSetup() {
        setUp(
                createSurveyScn.injectOpen(atOnceUsers(1)),
                submitSurveyScn.injectOpen(
                        nothingFor(5),
                        constantUsersPerSec(50).during(Duration.ofMinutes(30))
                )
        ).protocols(httpProtocol);
    }

}
