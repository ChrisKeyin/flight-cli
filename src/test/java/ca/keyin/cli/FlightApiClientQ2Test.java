package ca.keyin.cli;

import ca.keyin.cli.dto.AircraftDto;
import ca.keyin.cli.dto.PassengerDto;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightApiClientQ2Test {

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
    void listPassengers_thenAircraftForPassenger_happyPath() throws Exception {
        String passengersJson = """
        [
          {"id":1,"firstName":"Alice","lastName":"Ng","phoneNumber":"555-1111"},
          {"id":2,"firstName":"Bob","lastName":"Lee","phoneNumber":"555-2222"}
        ]
        """;
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(passengersJson)
                .addHeader("Content-Type", "application/json"));

        List<PassengerDto> passengers = client.getAs("/passengers", new TypeReference<List<PassengerDto>>() {});

        assertNotNull(passengers);
        assertEquals(2, passengers.size());
        assertEquals(1L, passengers.get(0).id());
        assertEquals("Alice", passengers.get(0).firstName());

        String aircraftJson = """
        [
          {"id":10,"type":"Airbus A320","airlineName":"Delta","numberOfPassengers":180},
          {"id":11,"type":"Boeing 737-800","airlineName":"WestJet","numberOfPassengers":174}
        ]
        """;
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(aircraftJson)
                .addHeader("Content-Type", "application/json"));

        List<AircraftDto> aircraft = client.getAs("/passengers/1/aircraft", new TypeReference<List<AircraftDto>>() {});

        assertNotNull(aircraft);
        assertEquals(2, aircraft.size());
        assertEquals(10L, aircraft.get(0).id());
        assertEquals("Airbus A320", aircraft.get(0).type());
        assertEquals("Delta", aircraft.get(0).airlineName());
        assertEquals(180, aircraft.get(0).numberOfPassengers());
    }

    @Test
    void listPassengers_emptyList_ok() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));

        List<PassengerDto> passengers = client.getAs("/passengers", new TypeReference<List<PassengerDto>>() {});
        assertNotNull(passengers);
        assertTrue(passengers.isEmpty());
    }

    @Test
    void aircraftForPassenger_404_throws() {
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("{\"error\":\"Not Found\"}")
                .addHeader("Content-Type", "application/json"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                client.getAs("/passengers/999/aircraft", new TypeReference<List<AircraftDto>>() {}));

        assertTrue(ex.getMessage().contains("404"));
    }
}
