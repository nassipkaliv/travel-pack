# Trip Factory — Factory Method & Abstract Factory

Assignment 2, Software Design Patterns. Java 17, Maven, JUnit 5.

**Domain:** trip planning (continues the Travel Package domain of Assignment 1).

| Product type ↓ / Family → | BUDGET | STANDARD | PREMIUM |
|---|---|---|---|
| **Transport** | `BusTransfer` | `EconomyFlight` | `BusinessFlight` |
| **Accommodation** | `Hostel` | `Hotel3Star` | `Resort5Star` |
| **Excursion** | `SelfGuidedWalk` | `GroupTour` | `PrivateGuide` |

9 concrete products, 3 families, 3 product types. Products of one family differ in behaviour:
travel time and baggage, private room and meals, guide and group size.

## Part A — without factories (this commit)

`src/main/java/kz/aitu/sdp/trip/naive/` creates products with `if/else` over the tier.
Four problems it causes are described in [docs/PART-A-PROBLEMS.md](docs/PART-A-PROBLEMS.md).

```bash
./mvnw package -DskipTests
java -cp target/classes kz.aitu.sdp.trip.naive.NaiveApp
```

Next steps: Factory Method and Abstract Factory.
