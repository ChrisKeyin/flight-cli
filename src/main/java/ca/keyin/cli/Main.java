package ca.keyin.cli;

import ca.keyin.cli.dto.AirportDto;
import ca.keyin.cli.dto.CityDto;
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
            System.out.println("0) Exit");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            try {
                if ("1".equals(choice)) {
                    handleQ1(client, sc);
                } else if ("0".equals(choice)) {
                    System.out.println("Bye!");
                    return;
                } else {
                    System.out.println("Invalid choice. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace(System.out);
            }
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
}
