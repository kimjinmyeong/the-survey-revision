package simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class SurveySubmitAnswerSimulation extends Simulation {

        HttpProtocolBuilder httpProtocol = http
                .baseUrl("http://localhost:8080/v1") // Base URL of your Spring Boot application
                .acceptHeader("application/json")
                .contentTypeHeader("application/json")
                .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");

        Map<String, String> headers = Map.of("Content-Type", "application/json");

        // AtomicInteger to ensure unique counts
        private static final AtomicInteger count = new AtomicInteger(1);

        // Iterator to provide unique count values
        private static final Iterator<Map<String, Object>> feeder = Stream.generate(() -> Collections.singletonMap("count", (Object) count.getAndIncrement())).iterator();

        // Scenario to create a survey
        ScenarioBuilder createSurveysScn = scenario("Survey Create Scenario")
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

    // Scenario to create a survey
    ScenarioBuilder submitSurveysScn = scenario("Submit Survey Scenario")
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

    {
        setUp(
                createSurveysScn.injectOpen(atOnceUsers(1)),
                submitSurveysScn.injectOpen(
                        nothingFor(3), // wait for 3 seconds to ensure surveys are created
                        atOnceUsers(1000), // simulate 1000 users concurrently fetching surveys
                        rampUsers(1000).during(Duration.ofSeconds(300)) // ramp up to 1000 users over 5 minutes
                )
        ).protocols(httpProtocol);
    }




}
