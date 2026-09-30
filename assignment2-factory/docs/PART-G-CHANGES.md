# Part G — adding the fourth family (LUXURY)

Commit: `Add luxury family as the fourth product family`.

## Files added (4)

| File | Role |
|---|---|
| `family/luxury/PrivateJet.java` | Transport of the new family |
| `family/luxury/PrivateVilla.java` | Accommodation of the new family |
| `family/luxury/HelicopterTour.java` | Excursion of the new family |
| `family/luxury/LuxuryTripFactory.java` | Concrete factory of the new family |

## Files changed (2, three lines in total)

| File | Change |
|---|---|
| `product/TripTier.java` | `LUXURY` added to the enum |
| `factory/TripFactoryProvider.java` | one import and one registry line: `FACTORIES.put(TripTier.LUXURY, LuxuryTripFactory::new);` |

## Files NOT changed

- `app/TripPlanner.java` — planning, pricing, itinerary and packing logic;
- `app/App.java` — the client;
- `booking/*` — the whole Factory Method side;
- `product/Transport.java`, `Accommodation.java`, `Excursion.java` — product interfaces;
- the other three families.

The existing tests kept passing without edits, and the parameterized ones
(`@EnumSource(TripTier.class)`) started covering the new family automatically.

## Why so little had to change

The business logic depends on `TripFactory`, `Transport`, `Accommodation` and `Excursion`,
never on a concrete class. The only code that knows concrete factories is the registry in
`TripFactoryProvider`, so a new family plugs into one line there.

Even that line could disappear by loading factories with `ServiceLoader`; the registry was
kept because it is easier to read and to explain.
