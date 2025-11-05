package ca.keyin.cli;

import ca.keyin.cli.dto.AircraftDto;
import ca.keyin.cli.dto.AirportDto;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightApiClientQ3Test {

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
    void listAircraft_thenAirportsForAircraft_happyPath() throws Exception {
        String aircraftListJson = """
        [
          {"id":1,"type":"Airbus A320","airlineName":"Delta","numberOfPassengers":180},
          {"id":2,"type":"Boeing 737-800","airlineName":"WestJet","numberOfPassengers":174}
        ]
        """;
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(aircraftListJson)
                .addHeader("Content-Type", "application/json"));

        List<AircraftDto> aircraft = client.getAs("/aircraft", new TypeReference<List<AircraftDto>>() {});

        assertNotNull(aircraft);
        assertEquals(2, aircraft.size());
        assertEquals(1L, aircraft.get(0).id());
        assertEquals("Airbus A320", aircraft.get(0).type());

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

        List<AirportDto> airports = client.getAs("/aircraft/1/airports", new TypeReference<List<AirportDto>>() {});

        assertNotNull(airports);
        assertEquals(2, airports.size());
        assertEquals(1L, airports.get(0).id());
        assertEquals("BOS", airports.get(0).code());
    }

    @Test
    void airportsForAircraft_emptyList_ok() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));

        List<AirportDto> airports = client.getAs("/aircraft/99/airports", new TypeReference<List<AirportDto>>() {});
        assertNotNull(airports);
        assertTrue(airports.isEmpty());
    }

    @Test
    void airportsForAircraft_404_throws() {
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("{\"error\":\"Not Found\"}")
                .addHeader("Content-Type", "application/json"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                client.getAs("/aircraft/123456/airports", new TypeReference<List<AirportDto>>() {}));

        assertTrue(ex.getMessage().contains("404"));
    }
}
