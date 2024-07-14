package simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class GetAllSurveySimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080/v1")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");

    Map<String, String> headers = Map.of("Content-Type", "application/json");

    private static final AtomicInteger count = new AtomicInteger(1);
    private static final Iterator<Map<String, Object>> feeder = Stream.generate(() -> Collections.singletonMap("count", (Object) count.getAndIncrement())).iterator();

    ScenarioBuilder createSurveysScn = scenario("Create Surveys")
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
            .repeat(15, "n").on(
                    exec(http("Create Survey #{n}")
                            .post("/surveys")
                            .headers(headers)
                            .body(RawFileBody("data/survey_request.json")).asJson()
                            .check(status().is(200))
                    )
            );

    ScenarioBuilder getAllSurveysScn = scenario("Get All Surveys")
            .exec(http("Get Surveys Page 1")
                    .get("/surveys")
                    .queryParam("page", "1")
                    .check(jsonPath("$.surveys").exists())
                    .headers(headers)
                    .check(status().is(200))
            );

    // Load Testing Setup
    public void loadTestingSetup() {
        setUp(
                createSurveysScn.injectOpen(atOnceUsers(1)),
                getAllSurveysScn.injectOpen(
                        nothingFor(5),
                        atOnceUsers(1000),
                        rampUsers(5000).during(Duration.ofSeconds(300))
                )
        ).protocols(httpProtocol);
    }

    // Stress Testing Setup
    public void stressTestingSetup() {
        setUp(
                createSurveysScn.injectOpen(atOnceUsers(1)),
                getAllSurveysScn.injectOpen(
                        nothingFor(5),
                        rampUsers(10000).during(Duration.ofSeconds(600))
                )
        ).protocols(httpProtocol);
    }

    // Soak Testing Setup
    public void soakTestingSetup() {
        setUp(
                createSurveysScn.injectOpen(atOnceUsers(1)),
                getAllSurveysScn.injectOpen(
                        nothingFor(5),
                        constantUsersPerSec(50).during(Duration.ofMinutes(30))
                )
        ).protocols(httpProtocol);
    }

    // Main Simulation Setup
    {
        String testType = System.getProperty("type");
        if (testType == null) {
            testType = "LOAD"; // Default to LOAD if no system property is set
        }

        switch (testType.toUpperCase()) {
            case "LOAD":
                loadTestingSetup();
                break;
            case "STRESS":
                stressTestingSetup();
                break;
            case "SOAK":
                soakTestingSetup();
                break;
            default:
                throw new IllegalArgumentException("Unknown type: " + testType);
        }
    }
}
