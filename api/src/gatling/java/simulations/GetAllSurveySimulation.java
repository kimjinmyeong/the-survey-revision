package simulations;

import io.gatling.javaapi.core.ScenarioBuilder;

import java.time.Duration;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class GetAllSurveySimulation extends BaseSimulation {

    private final ScenarioBuilder createSurveyScn;
    private final ScenarioBuilder getAllSurveyScn;

    public GetAllSurveySimulation() {
        createSurveyScn = scenario("Create Surveys")
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
                        .check(status().is(200)))
                .pause(1)
                .repeat(15, "n").on(
                        exec(http("Create Survey #{n}")
                                .post("/surveys")
                                .headers(Map.of("Content-Type", "application/json"))
                                .body(RawFileBody("data/survey_request.json")).asJson()
                                .check(status().is(200))
                        )
                );

        getAllSurveyScn = scenario("Get All Surveys")
                .exec(http("Get Surveys Page 1")
                        .get("/surveys")
                        .queryParam("page", "1")
                        .check(jsonPath("$.surveys").exists())
                        .headers(headers)
                        .check(status().is(200))
                );
        String testType = System.getProperty("type");
        setupSimulation(testType);
    }

    @Override
    public void loadTestingSetup() {
        setUp(
                createSurveyScn.injectOpen(atOnceUsers(1)),
                getAllSurveyScn.injectOpen(
                        nothingFor(5),
                        atOnceUsers(1),
                        rampUsers(5).during(Duration.ofSeconds(20))
                )
        ).protocols(httpProtocol);
    }

    @Override
    public void stressTestingSetup() {
        setUp(
                createSurveyScn.injectOpen(atOnceUsers(1)),
                getAllSurveyScn.injectOpen(
                        nothingFor(5),
                        rampUsers(10000).during(Duration.ofSeconds(600))
                )
        ).protocols(httpProtocol);
    }

    @Override
    public void soakTestingSetup() {
        setUp(
                createSurveyScn.injectOpen(atOnceUsers(1)),
                getAllSurveyScn.injectOpen(
                        nothingFor(5),
                        constantUsersPerSec(50).during(Duration.ofMinutes(30))
                )
        ).protocols(httpProtocol);
    }

}
