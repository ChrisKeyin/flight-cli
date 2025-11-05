# Flight CLI

Java command-line client for the Flight API.

## Requirements
- Java 17+
- Maven 3.9+
- Running Flight API at `http://localhost:8080` (configurable)

## Quick start
```bash
# Compile + run
mvn -DskipTests compile exec:java "-Dexec.mainClass=ca.keyin.cli.Main"

# Or point to a remote API
mvn -DskipTests compile exec:java "-Dexec.mainClass=ca.keyin.cli.Main" -DbaseUrl=http://localhost:8080
```
Features (Sprint Questions)

List airports in a city

List aircraft a passenger has flown on

List airports an aircraft uses (takeoff/landing)

List airports a passenger has used

Testing
```
mvn test
```
CI

GitHub Actions runs tests on every PR to main. See Actions tab.


