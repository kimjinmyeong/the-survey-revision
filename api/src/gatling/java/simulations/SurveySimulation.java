package simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class SurveySimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080/v1") // Base URL of your Spring Boot application
            .contentTypeHeader("application/json")
            .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");

    Map<String, String> headers = Map.of("Content-Type", "application/json");

    ScenarioBuilder scn = scenario("Survey Operations")
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
                    .body(RawFileBody("data/survey_request.json")).asJson()
                    .check(status().is(200))
                    .check(jsonPath("$.surveyId").saveAs("surveyId")));
//            .exec(http("Get Specific Survey")
//                    .get("1L")
//                    .headers(headers)
//                    .check(status().is(200))
//                    .check(jsonPath("$.surveyId").is("${surveyId}"))
//                    .check(jsonPath("$.title").is("This is test survey title"))
//                    .check(jsonPath("$.startedDate").transform(date -> {
//                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
//                        return LocalDateTime.parse(date, formatter).format(formatter);
//                    }).is(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))))
//                    .check(jsonPath("$.endedDate").transform(date -> {
//                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
//                        return LocalDateTime.parse(date, formatter).format(formatter);
//                    }).is(LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))))
//                    .check(jsonPath("$.questions").exists())
//                    .check(jsonPath("$.rewardPoints").is("1"))


    {
        setUp(scn.injectOpen(rampUsers(1).during(Duration.ofSeconds(10)))).protocols(httpProtocol);
    }
}
