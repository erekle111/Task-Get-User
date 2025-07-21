package configs;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;


import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WireMockConf {
    private static WireMockServer wireMockServer;
    public static final int PORT = 8080;
    public static final String BASE_URL = "http://localhost:" + PORT;

    public static void startWireMock() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(
                    WireMockConfiguration.options()
                            .port(PORT)
                            .globalTemplating(true)
            );
        }

        if (!wireMockServer.isRunning()) {
            wireMockServer.start();
            WireMock.configureFor("localhost", PORT);
        }
    }

    public static void stopWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    public static void setupStubs() {
        // Get all users
        stubFor(get(urlEqualTo("/users"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    [
                        {"id": 1, "name": "Alice", "age": 30, "gender": "female"},
                        {"id": 2, "name": "Bob", "age": 25, "gender": "male"}
                    ]
                    """)));

        // Filter by age
        stubFor(get(urlEqualTo("/users?age=30"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    [
                        {"id": 1, "name": "Alice", "age": 30, "gender": "female"}
                    ]
                    """)));

        stubFor(get(urlEqualTo("/users?age=25"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    [
                        {"id": 2, "name": "Bob", "age": 25, "gender": "male"}
                    ]
                    """)));

        // Filter by gender
        stubFor(get(urlEqualTo("/users?gender=male"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    [
                        {"id": 2, "name": "Bob", "age": 25, "gender": "male"}
                    ]
                    """)));

        stubFor(get(urlEqualTo("/users?gender=female"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    [
                        {"id": 1, "name": "Alice", "age": 30, "gender": "female"}
                    ]
                    """)));


        // Invalid age
        stubFor(get(urlEqualTo("/users?age=-1"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {"error": "Invalid age parameter. Age must be positive."}
                    """)));

        // Invalid gender
        stubFor(get(urlEqualTo("/users?gender=unknown"))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {"error": "Invalid gender parameter. Allowed values: male, female"}
                    """)));

        // Internal server error
        stubFor(get(urlPathEqualTo("/users"))
                .withQueryParam("error", equalTo("500"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {"error": "Internal server error"}
                    """)));

    }
}