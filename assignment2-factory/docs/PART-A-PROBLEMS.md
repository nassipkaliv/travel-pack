# Part A — the version without factories

Code: `src/main/java/kz/aitu/sdp/trip/naive/`

`TripBookingService` creates concrete products directly:

```java
if (tier.equals("BUDGET")) {
    transport = new BusTransfer();
} else if (tier.equals("STANDARD")) {
    transport = new EconomyFlight();
} else if (tier.equals("PREMIUM")) {
    transport = new BusinessFlight();
}
```

## Problems this causes

### 1. The client depends on concrete classes

`TripBookingService` names all 9 product classes. Any change to a product class —
renaming it, splitting it, replacing it with another implementation — reaches the client.
The client should only need the three interfaces `Transport`, `Accommodation`, `Excursion`.

### 2. Creation logic is duplicated

The same `if/else` over the tier is written three times, once per product type. A fourth
product type would add a fourth copy. Any other class that needs a trip copies all of it.

### 3. A new family means editing existing code

Adding a `LUXURY` tier (private jet, villa, helicopter tour) requires a new branch in every
`if/else`, in every place that creates products. This breaks the Open/Closed Principle:
the client should be open for extension but closed for modification.

### 4. Incompatible products can be combined by accident

`bookMixedTrip()` compiles and runs, and returns a trip with a business class flight and a
bed in a shared hostel room. Nothing in the design keeps one trip inside one tier.

## What the patterns fix

| Problem | Fixed by |
|---|---|
| 1, 2 | Factory Method — creation moves behind one method the client calls |
| 3 | A new family is a new factory class; existing code is not edited |
| 4 | Abstract Factory — one factory creates one consistent family of products |
