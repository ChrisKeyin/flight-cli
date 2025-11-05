package ca.keyin.cli;

import ca.keyin.cli.dto.AirportDto;
import ca.keyin.cli.dto.CityDto;
import ca.keyin.cli.dto.PassengerDto;
import ca.keyin.cli.dto.AircraftDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        String baseUrl = System.getProperty("baseUrl", "http://localhost:8080");
        FlightApiClient client = new FlightApiClient(baseUrl);

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Flight CLI ===");
            System.out.println("1) Q1: List airports in a city");
            System.out.println("2) Q2: List aircraft a passenger has flown on");
            System.out.println("3) Q3: List airports an aircraft uses (takeoff/landing)");
            System.out.println("0) Exit");

            String choice = readMenuChoice(sc, "Choose: ");

            try {
                switch (choice) {
                    case "1" -> handleQ1(client, sc);
                    case "2" -> handleQ2(client, sc);
                    case "3" -> handleQ3(client, sc);
                    case "0" -> {
                        System.out.println("Bye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace(System.out);
            }
        }
    }

    private static String readMenuChoice(Scanner sc, String prompt) {
        System.out.print(prompt);
        System.out.flush();
        while (true) {
            String line = sc.nextLine();
            if (line != null) {
                line = line.trim();
                if (!line.isEmpty()) return line;
            }
            System.out.print(prompt);
            System.out.flush();
        }
    }

    private static void handleQ1(FlightApiClient client, Scanner sc) throws Exception {
        List<CityDto> cities = client.getAs("/cities", new TypeReference<List<CityDto>>() {});
        if (cities.isEmpty()) {
            System.out.println("No cities found.");
            return;
        }

        System.out.println("\nAvailable Cities:");
        for (CityDto c : cities) {
            System.out.printf("  [%d] %s, %s (pop %d)%n", c.id(), c.name(), c.state(), c.population());
        }

        System.out.print("\nEnter city id: ");
        long cityId = Long.parseLong(sc.nextLine().trim());

        List<AirportDto> airports = client.getAs(
                "/cities/" + cityId + "/airports",
                new TypeReference<List<AirportDto>>() {}
        );

        if (airports.isEmpty()) {
            System.out.println("No airports found for that city.");
        } else {
            System.out.println("\nAirports in selected city:");
            for (AirportDto a : airports) {
                System.out.printf("  - %s (%s) [id=%d]%n", a.name(), a.code(), a.id());
            }
        }
    }

    private static void handleQ2(FlightApiClient client, Scanner sc) throws Exception {
        List<PassengerDto> passengers = client.getAs("/passengers", new TypeReference<List<PassengerDto>>() {});
        if (passengers.isEmpty()) {
            System.out.println("No passengers found.");
            return;
        }

        System.out.println("\nPassengers:");
        for (PassengerDto p : passengers) {
            System.out.printf("  [%d] %s %s  (tel: %s)%n", p.id(), p.firstName(), p.lastName(), p.phoneNumber());
        }

        System.out.print("\nEnter passenger id: ");
        long passengerId = Long.parseLong(sc.nextLine().trim());

        List<AircraftDto> aircraft = client.getAs(
                "/passengers/" + passengerId + "/aircraft",
                new TypeReference<List<AircraftDto>>() {}
        );

        if (aircraft.isEmpty()) {
            System.out.println("This passenger has no recorded flights.");
        } else {
            System.out.println("\nAircraft flown by this passenger:");
            for (AircraftDto a : aircraft) {
                System.out.printf("  - %s | %s | seats: %d (id=%d)%n",
                        a.type(), a.airlineName(), a.numberOfPassengers(), a.id());
            }
        }
    }

    private static void handleQ3(FlightApiClient client, Scanner sc) throws Exception {
        boolean showedList = false;

        try {
            List<AircraftDto> aircraftList = client.getAs(
                    "/aircraft",
                    new TypeReference<List<AircraftDto>>() {}
            );
            if (!aircraftList.isEmpty()) {
                System.out.println("\nAvailable Aircraft:");
                for (AircraftDto a : aircraftList) {
                    System.out.printf("  [%d] %s | %s | seats: %d%n",
                            a.id(), a.type(), a.airlineName(), a.numberOfPassengers());
                }
                showedList = true;
            }
        } catch (RuntimeException ignored) {
        }

        if (!showedList) {
            System.out.println("\nEnter an aircraft id (e.g., 1, 2, or 3).");
        }

        System.out.print("Enter aircraft id: ");
        long aircraftId = Long.parseLong(sc.nextLine().trim());

        List<AirportDto> airports = client.getAs(
                "/aircraft/" + aircraftId + "/airports",
                new TypeReference<List<AirportDto>>() {}
        );

        if (airports.isEmpty()) {
            System.out.println("No airports recorded for this aircraft.");
        } else {
            System.out.println("\nAirports used by this aircraft:");
            for (AirportDto ap : airports) {
                System.out.printf("  - %s (%s) [id=%d]%n", ap.name(), ap.code(), ap.id());
            }
        }
    }

}
