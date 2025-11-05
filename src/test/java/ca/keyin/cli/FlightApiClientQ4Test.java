package ca.keyin.cli;

import ca.keyin.cli.dto.AirportDto;
import ca.keyin.cli.dto.PassengerDto;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightApiClientQ4Test {

    private MockWebServer server;
    private FlightApiClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        client = new FlightApiClient(server.url("/").toString());
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void listPassengers_thenAirportsForPassenger_happyPath() throws Exception {
        String passengersJson = """
        [
          {"id":1,"firstName":"Alice","lastName":"Ng","phoneNumber":"555-1111","cityId":1},
          {"id":2,"firstName":"Bob","lastName":"Lee","phoneNumber":"555-2222","cityId":2}
        ]
        """;
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(passengersJson)
                .addHeader("Content-Type", "application/json"));

        List<PassengerDto> passengers =
                client.getAs("/passengers", new TypeReference<List<PassengerDto>>() {});

        assertNotNull(passengers);
        assertEquals(2, passengers.size());
        assertEquals(1L, passengers.get(0).id());
        assertEquals("Alice", passengers.get(0).firstName());

        String airportsJson = """
        [
          {"id":1,"name":"Logan International Airport","code":"BOS"},
          {"id":2,"name":"Toronto Pearson International Airport","code":"YYZ"}
        ]
        """;
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(airportsJson)
                .addHeader("Content-Type", "application/json"));

        List<AirportDto> airports =
                client.getAs("/passengers/1/airports", new TypeReference<List<AirportDto>>() {});

        assertNotNull(airports);
        assertEquals(2, airports.size());
        assertEquals("BOS", airports.get(0).code());
        assertEquals("YYZ", airports.get(1).code());
    }

    @Test
    void airportsForPassenger_emptyList_ok() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));

        List<AirportDto> airports =
                client.getAs("/passengers/99/airports", new TypeReference<List<AirportDto>>() {});
        assertNotNull(airports);
        assertTrue(airports.isEmpty());
    }

    @Test
    void airportsForPassenger_404_throws() {
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("{\"error\":\"Not Found\"}")
                .addHeader("Content-Type", "application/json"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                client.getAs("/passengers/12345/airports", new TypeReference<List<AirportDto>>() {}));
        assertTrue(ex.getMessage().contains("404"));
    }
}
