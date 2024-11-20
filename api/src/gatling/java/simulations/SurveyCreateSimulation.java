package simulations;

import io.gatling.javaapi.core.ScenarioBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class SurveyCreateSimulation extends BaseSimulation {

    private final ScenarioBuilder createSurveyScn;

    public SurveyCreateSimulation() {
        createSurveyScn = scenario("Create Survey Scenario")
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
                .exec(http("Create Survey")
                        .post("/surveys")
                        .headers(headers)
                        .body(RawFileBody("data/survey_request.json")).asJson()
                        .check(status().is(200))
                        .check(jsonPath("$.surveyId").saveAs("surveyId")));
        String testType = System.getProperty("type");
        setupSimulation(testType);
    }

    @Override
    public void loadTestingSetup() {
        setUp(
                createSurveyScn.injectOpen(
                        atOnceUsers(1),
                        nothingFor(5),
                        atOnceUsers(1000),
                        rampUsers(5000).during(Duration.ofSeconds(300))
                )
        ).protocols(httpProtocol);
    }

    @Override
    public void stressTestingSetup() {
        setUp(
                createSurveyScn.injectOpen(
                        atOnceUsers(1),
                        nothingFor(5),
                        rampUsers(10000).during(Duration.ofSeconds(600))
                )
        ).protocols(httpProtocol);
    }

    @Override
    public void soakTestingSetup() {
        setUp(
                createSurveyScn.injectOpen(
                        atOnceUsers(1),
                        nothingFor(5),
                        constantUsersPerSec(50).during(Duration.ofMinutes(30))
                )
        ).protocols(httpProtocol);
    }
}
