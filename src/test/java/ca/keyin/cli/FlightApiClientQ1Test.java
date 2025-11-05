package ca.keyin.cli;

import ca.keyin.cli.dto.CityDto;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightApiClientQ1Test {

    private MockWebServer server;
    private FlightApiClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        String baseUrl = server.url("/").toString();
        client = new FlightApiClient(baseUrl);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void getCities_parsesListCorrectly() throws Exception {
        String body = """
        [
          {"id":1,"name":"Boston","state":"MA","population":675000},
          {"id":2,"name":"Toronto","state":"ON","population":2800000}
        ]
        """;
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(body)
                .addHeader("Content-Type", "application/json"));

        List<CityDto> cities = client.getAs("/cities", new TypeReference<List<CityDto>>() {});

        assertNotNull(cities);
        assertEquals(2, cities.size());
        CityDto c1 = cities.get(0);
        assertEquals(1L, c1.id());
        assertEquals("Boston", c1.name());
        assertEquals("MA", c1.state());
        assertEquals(675000, c1.population());
    }

    @Test
    void getCities_handles404AsException() {
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("{\"error\":\"Not Found\"}")
                .addHeader("Content-Type", "application/json"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                client.getAs("/cities", new com.fasterxml.jackson.core.type.TypeReference<List<CityDto>>() {}));

        assertTrue(ex.getMessage().contains("404"), "Should mention 404 in error message");
    }
}
