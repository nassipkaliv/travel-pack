# Report — Factory Method & Abstract Factory

Domain: trip planning. Product type × product family, 12 concrete products.

| Product type ↓ / Family → | BUDGET | STANDARD | PREMIUM | LUXURY |
|---|---|---|---|---|
| Transport | BusTransfer | EconomyFlight | BusinessFlight | PrivateJet |
| Accommodation | Hostel | Hotel3Star | Resort5Star | PrivateVilla |
| Excursion | SelfGuidedWalk | GroupTour | PrivateGuide | HelicopterTour |

Behaviour really differs: 14 h by bus against 90 min by jet, 10 kg against 100 kg of
baggage, no meals against all inclusive, a fixed 09:00 group tour against a flexible
private one. These values drive the itinerary, the packing list and the price, so the
family is visible in the output, not only in class names.

## Part A — without factories

`naive/TripBookingService` creates products with three copies of the same `if/else`.
Problems, in detail in [PART-A-PROBLEMS.md](PART-A-PROBLEMS.md):

1. the client depends on all 9 concrete classes;
2. creation logic is duplicated once per product type;
3. a new family means editing every `if/else`;
4. `bookMixedTrip()` shows that a business class flight and a hostel bed can be combined
   by accident.

## Part B — Factory Method

```
BookingDocument (Product)        BookingChannel (Creator)
├── EmailVoucher                 ├── OnlineBookingChannel
├── PaperVoucher                 ├── AgencyBookingChannel
└── CorporateInvoice             └── CorporateBookingChannel
```

The Creator is not a wrapper around `new`. `BookingChannel.book()` contains the logic
shared by every channel:

1. refuse the booking if the group is larger than the excursion can host;
2. apply the pricing rule of the channel (discount or service fee);
3. issue the next reference number from the channel's own counter;
4. create the document through the factory method;
5. assemble the `Booking` and, in `confirmationMessage()`, use the document that was created.

### Why a factory method and not a static factory

- `book()` is written once and works for channels that did not exist when it was written;
  a static factory would need a `switch` over channel names inside that logic.
- Each channel is an object with state: the counter behind `ONL-0001`, `AGN-0001`.
  A static method has no state to keep.
- Subclasses also override pricing and payment terms, so the whole channel behaviour is
  one polymorphic unit, not a collection of static helpers.
- Tests can pass their own subclass of `BookingChannel`; a static factory cannot be
  substituted.

## Part C — Abstract Factory

```java
public interface TripFactory {
    TripTier tier();
    Transport createTransport();
    Accommodation createAccommodation();
    Excursion createExcursion();
}
```

Four concrete factories: `BudgetTripFactory`, `StandardTripFactory`, `PremiumTripFactory`,
`LuxuryTripFactory`. Each creates the three products of its own family and nothing else.

## Part D — compatibility by design

The rule is "all three components of a trip belong to one family". It is enforced by the
structure, not by a validation branch:

| Mechanism | Effect |
|---|---|
| Concrete products are package-private next to their factory | code outside the family package cannot name or instantiate `Hostel`, `Resort5Star`, … |
| A factory is the only source of components | every component a factory returns carries its own tier |
| `TripPlanner` holds one factory | all three components of a plan come from the same instance |
| `TripPlan` compares tiers | safety net for hand-assembled plans (tested), not the main mechanism |

So `A1 + B2 + C3` is not "checked and rejected" — outside the family package there is no
expression that produces it.

## Part E — runtime family selection

`ExternalConfiguration` reads a setting from the command line, then the environment, then
`trip.properties`:

```bash
java ... App --family=PREMIUM --channel=agency
TRIP_FAMILY=LUXURY java ... App
```

`TripFactoryProvider` maps the value to a factory; `BookingChannels` does the same for the
channel. `App` resolves both once at startup. After that line, nothing in the business
logic knows which family is running — `TripPlanner` only sees `TripFactory`.

Unknown values are reported with the list of known ones instead of silently falling back.

## Part F — business operations

`TripPlanner` implements four operations in which the products collaborate:

| Operation | Collaboration |
|---|---|
| `planTrip` | takes the three components from one factory and binds them to the request |
| `quote` | transport and excursion are priced per person, the stay per night |
| `arrivalSchedule` | travel time decides arrival, the check-in hour of the stay decides when the room opens, the excursion starts 90 min after check-in — next day if the arrival is late or the tour has a fixed hour; the meal plan decides the dinner line |
| `packingAdvice` | baggage limit from the transport, items from the stay and the excursion, warnings from all three (`pack light`, `no meals`, `shared room`, `arrive 15 minutes early`) |

The same request produces a same-day helicopter tour for LUXURY and a next-day city walk
for BUDGET, because the products, not the client, decide.

`BookingChannel.book()` is the fifth operation and spans both patterns: it prices a plan
built by the Abstract Factory and issues a document created by the Factory Method.

## Part G — fourth family

See [PART-G-CHANGES.md](PART-G-CHANGES.md). Four new files, three changed lines
(`TripTier.LUXURY`, one import, one registry entry). No change in `TripPlanner`, `App`,
`booking/` or the product interfaces, and the existing tests passed unchanged.

## Part H — UML

[uml/class-diagram.puml](uml/class-diagram.puml); a Mermaid version renders in the
[README](../README.md#uml). The Abstract Factory part and the Factory Method part are
drawn as two marked packages.

## Part I — tests

58 JUnit 5 tests:

| Class | Covers |
|---|---|
| `TripFactoryTest` | creation of every family, the exact concrete classes, behaviour differences, products not public outside their package |
| `CompatibilityTest` | one tier per plan, per-planner isolation, mixed plan rejected (negative) |
| `RuntimeSelectionTest` | argument > environment > file > default, channel selection, unknown family and unknown channel (negative) |
| `TripPlannerTest` | itinerary in three families, dinner by meal plan, price breakdown, packing advice, budget refusal and invalid request (negative) |
| `BookingChannelTest` | document per channel, discounts and fees, reference numbers, one shared `book()` for all channels, group too large and missing company (negative) |
| `LuxuryFamilyTest` | the fourth family in planning, scheduling, packing, booking and runtime selection |
| `ClientAbstractionTest` | the planner accepts only the `TripFactory` interface; one loop serves every registered family |

## Part J — git history

Nine commits, one per design step: domain without factories → product abstractions →
Abstract Factory → runtime selection → business operations → Factory Method → external
configuration → tests → fourth family → documentation.
