# Travel Package Builder

Assignment 1, Software Design Patterns — Builder pattern. Java 17, Maven, JUnit 5.

**Variant:** Travel Package · **Preset:** `allInclusiveBeach`

## Run

```bash
./mvnw test                                                        # 25 tests
./mvnw package && java -cp target/classes kz.aitu.sdp.travel.App   # demo
```

## Website

Interactive presentation of the project (React + TypeScript + Tailwind): a live builder playground, UML, rules, tests.

```bash
cd web
npm install
npm run dev
```

## Example

```java
TravelPackage trip = TravelPackage.builder("FAMILY-1", almaty, astana)
        .nights(4)
        .hotel("Hilton", 4)
        .room(RoomType.FAMILY)
        .travelers(2, 2)
        .train("002")
        .build();

TravelPackage beach = TravelPresets.allInclusiveBeach("BEACH-1", almaty, antalya)
        .hotel("Rixos", 5)
        .flight("KC 921")
        .visaSupport()
        .build();
```

Invalid package → `build()` throws with all errors:

```
- Nights must be 1..30, got 45
- ECONOMY budget allows 1..3 stars, got 5
- International trip requires visa support
- Children require a FAMILY room
- STANDARD room fits 2 guests, got 4
- Airport transfer requires a flight
```

## Rules

| Rule | Type |
|---|---|
| 1..30 nights | range |
| Hotel stars depend on budget (ECONOMY 1–3, STANDARD 3–4, PREMIUM 4–5) | depends on another parameter |
| Flight **or** train, exactly one | mutually exclusive |
| International trip → visa support | mandatory under condition |
| Children → FAMILY room; guests ≤ room capacity | component requires component / range |
| Airport transfer → flight | component requires component |

## UML

```mermaid
classDiagram
    class TravelPackage {
        -id, from, to, nights, budget, hotel
        -room, meals, adults, children
        -flight, train, airportTransfer, visaSupport
        +builder(id, from, to)$ Builder
        +isInternational() boolean
    }
    class Builder {
        +nights(int) Builder
        +budget(BudgetLevel) Builder
        +hotel(name, stars) Builder
        +room(RoomType) Builder
        +travelers(adults, children) Builder
        +flight(number) Builder
        +train(number) Builder
        +airportTransfer() Builder
        +visaSupport() Builder
        +build() TravelPackage
    }
    class TravelPackageValidator {
        +validate()
    }
    class TravelPresets {
        +allInclusiveBeach(id, from, to)$ Builder
    }
    TravelPackage +-- Builder
    Builder ..> TravelPackage : creates
    Builder ..> TravelPackageValidator : validates with
    TravelPackageValidator ..> InvalidTravelPackageException : throws
    TravelPresets ..> Builder : returns
    TravelPackage --> BudgetLevel
    TravelPackage --> RoomType
    TravelPackage --> MealPlan
    TravelPackage *-- Hotel
    TravelPackage *-- Flight
    TravelPackage *-- Train
```

Report: [docs/REPORT.md](docs/REPORT.md)
