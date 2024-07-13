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
            .baseUrl("http://localhost:8080/v1") // Base URL of your Spring Boot application
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");

    Map<String, String> headers = Map.of("Content-Type", "application/json");

    // AtomicInteger to ensure unique counts
    private static final AtomicInteger count = new AtomicInteger(1);

    // Iterator to provide unique count values
    private static final Iterator<Map<String, Object>> feeder = Stream.generate(() -> Collections.singletonMap("count", (Object) count.getAndIncrement())).iterator();

    // Scenario to create 15 surveys
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

    // Scenario to get surveys on the second page
    ScenarioBuilder getAllSurveysScn = scenario("Get All Surveys")
            .exec(http("Get Surveys Page 1")
                    .get("/surveys")
                    .queryParam("page", "1")
                    .check(jsonPath("$.surveys").exists())
                    .headers(headers)
                    .check(status().is(200))
            );

    {
        setUp(
                createSurveysScn.injectOpen(atOnceUsers(1)),
                getAllSurveysScn.injectOpen(
                        nothingFor(5), // wait for 5 seconds to ensure surveys are created
                        atOnceUsers(1000), // simulate 1000 users concurrently fetching surveys
                        rampUsers(5000).during(Duration.ofSeconds(300))) // ramp up to 5000 users over 5 minutes
        ).protocols(httpProtocol);
    }
}
