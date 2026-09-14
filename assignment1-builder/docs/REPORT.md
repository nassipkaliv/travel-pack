# Report — Builder Pattern: Travel Package

## 1. Problem

A travel package has 14 fields. Only 3 are always required (id, from, to).
The others are optional, and many depend on each other:

- hotel stars must match the budget;
- children need a family room;
- the trip needs a flight **or** a train;
- a trip abroad needs visa support.

A constructor with 14 arguments is unreadable. Setters let the object exist in a broken state.

## 2. Why Builder

- **Readable:** every value has a name — `.nights(4).hotel("Hilton", 4)`.
- **Immutable:** the product has only `final` fields and no setters.
- **Always valid:** all rules are checked in one place, `build()`.

Rules like "stars depend on budget" can't be checked inside `hotel()`,
because the budget may be set later. So they are checked in `build()`.

## 3. Design

See UML in [README](../README.md#uml).

| Class | Role |
|---|---|
| `TravelPackage` | the product, immutable, private constructor |
| `TravelPackage.Builder` | fluent steps + `build()` |
| `TravelPackageValidator` | business rules, one method per rule |
| `TravelPresets` | preset `allInclusiveBeach` |
| `BudgetLevel`, `RoomType` | enums that store limits (stars, capacity) |
| `Hotel`, `Flight`, `Train`, `Location` | small records |
| `TravelConsole`, `ConsoleInput` | interactive console: the user types values, the Builder creates the package |

How `build()` works:

```java
public TravelPackage build() {
    TravelPackage result = new TravelPackage(this);
    new TravelPackageValidator(result).validate();   // throws if invalid
    return result;
}
```

Two kinds of checks:

- **simple** (blank id, 6 stars, 0 adults) → thrown right away in the step;
- **cross-field** (stars vs budget) → checked in `build()`, all errors are reported together.

The preset returns a `Builder`, not a finished object, so the user can still change it.

## 4. Rules

| Rule | Constraint type |
|---|---|
| 1..30 nights | range |
| Stars depend on budget | depends on another parameter |
| Exactly one of flight / train | mutually exclusive |
| International → visa support | mandatory under condition |
| Children → FAMILY room | component requires component |
| Guests ≤ room capacity | range depends on parameter |
| Airport transfer → flight | component requires component |

## 5. Clean Code (Chapter 3)

- Small functions: each rule is a short method (`checkVisaForInternationalTrip`, `checkGuestsFitRoom`, ...).
- One function — one job: one rule per method; `validate()` only calls the checks.
- No flag arguments: `visaSupport()` instead of `visaSupport(true)`.
- Clear names, few arguments (0–2 per method).
- Exceptions instead of error codes.

## 6. Tests

28 JUnit 5 tests, all pass:

- builder: defaults, all values, step order, bad arguments;
- every rule: valid and invalid case, boundaries (0/1/30/31 nights, star limits, room capacity);
- all errors are reported at once;
- preset works, can be changed, and is still validated;
- console: typed values create a package, invalid input shows all errors (input is simulated with a `Scanner` over a string).

## 7. Changing requirements

| New requirement | What to change |
|---|---|
| New budget level or room type | add one enum constant |
| New rule | add one method in the validator + one line in `validate()` |
| New option (e.g. insurance) | one field + one builder method |
| New preset | one static method |

Example — "all inclusive only in 4–5 star hotels":

```java
private void checkStarsForAllInclusive() {
    if (trip.meals() == MealPlan.ALL_INCLUSIVE && trip.hotel() != null && trip.hotel().stars() < 4) {
        errors.add("All inclusive requires 4-5 stars");
    }
}
```

Existing code using the builder does not change.

## 8. Conclusion

Builder fits this task: many optional fields with rules between them.
The result is a readable API, an immutable object that is always valid,
and a design where new requirements are small, local changes.
