package simulations;

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.gatling.javaapi.http.HttpDsl.http;

/**
 * Abstract base class for Gatling performance simulations.
 * This class provides common setup for HTTP protocol, headers, and a feeder iterator.
 * Subclasses should implement the specific test setups for load, stress, and soak testing.
 *
 * To run the simulation with a specified test type, use the following command:
 *
 * <pre>
 * ./gradlew gatlingRun-SimulationClassName -Dtype=LOAD
 * ./gradlew gatlingRun-SimulationClassName -Dtype=STRESS
 * ./gradlew gatlingRun-SimulationClassName -Dtype=SOAK
 * </pre>
 *
 * Replace `SimulationClassName` with the name of the concrete simulation class.
 */
public abstract class BaseSimulation extends Simulation {

    protected final AtomicInteger count = new AtomicInteger(1);
    protected final Iterator<Map<String, Object>> feeder = Stream.generate(() -> Collections.singletonMap("count", (Object) count.getAndIncrement())).iterator();

    protected HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080/v1")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");

    protected Map<String, String> headers = Map.of("Content-Type", "application/json");

    public abstract void loadTestingSetup();
    public abstract void stressTestingSetup();
    public abstract void soakTestingSetup();

    public void setupSimulation(String testType) {
        if (testType == null) {
            testType = "LOAD";
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
