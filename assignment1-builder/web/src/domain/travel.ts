// TypeScript mirror of the Java domain. Messages and formats match the Java code exactly,
// so what the playground shows is what `build()` does in Java.

export type BudgetLevel = "ECONOMY" | "STANDARD" | "PREMIUM";
export type RoomType = "STANDARD" | "SUITE" | "FAMILY";
export type MealPlan = "ROOM_ONLY" | "BREAKFAST" | "HALF_BOARD" | "ALL_INCLUSIVE";

export const BUDGETS: Record<BudgetLevel, { minStars: number; maxStars: number }> = {
  ECONOMY: { minStars: 1, maxStars: 3 },
  STANDARD: { minStars: 3, maxStars: 4 },
  PREMIUM: { minStars: 4, maxStars: 5 },
};

export const ROOMS: Record<RoomType, { capacity: number }> = {
  STANDARD: { capacity: 2 },
  SUITE: { capacity: 3 },
  FAMILY: { capacity: 4 },
};

export const MEAL_PLANS: MealPlan[] = ["ROOM_ONLY", "BREAKFAST", "HALF_BOARD", "ALL_INCLUSIVE"];

export interface Location {
  city: string;
  country: string;
}

export const LOCATIONS: Location[] = [
  { city: "Almaty", country: "Kazakhstan" },
  { city: "Astana", country: "Kazakhstan" },
  { city: "Shymkent", country: "Kazakhstan" },
  { city: "Antalya", country: "Turkey" },
  { city: "Dubai", country: "UAE" },
  { city: "Tbilisi", country: "Georgia" },
];

/** Everything the user can set. Mirrors the fields of TravelPackage.Builder. */
export interface Draft {
  id: string;
  from: Location;
  to: Location;
  nights: number;
  budget: BudgetLevel;
  hotel: { name: string; stars: number } | null;
  room: RoomType;
  meals: MealPlan;
  adults: number;
  children: number;
  flight: string | null;
  train: string | null;
  airportTransfer: boolean;
  visaSupport: boolean;
}

export type Mode = "builder" | "preset";

/** Values right after TravelPackage.builder(id, from, to). */
export function builderDefaults(id: string, from: Location, to: Location): Draft {
  return {
    id, from, to,
    nights: 0,
    budget: "STANDARD",
    hotel: null,
    room: "STANDARD",
    meals: "BREAKFAST",
    adults: 1,
    children: 0,
    flight: null,
    train: null,
    airportTransfer: false,
    visaSupport: false,
  };
}

/** Values right after TravelPresets.allInclusiveBeach(id, from, to). */
export function presetDefaults(id: string, from: Location, to: Location): Draft {
  return {
    ...builderDefaults(id, from, to),
    nights: 7,
    budget: "PREMIUM",
    meals: "ALL_INCLUSIVE",
    airportTransfer: true,
  };
}

export function baseFor(mode: Mode, draft: Draft): Draft {
  return mode === "preset"
    ? presetDefaults(draft.id, draft.from, draft.to)
    : builderDefaults(draft.id, draft.from, draft.to);
}

// ---------------------------------------------------------------------------
// Level 1: checks inside builder steps / records (IllegalArgumentException, thrown immediately)
// ---------------------------------------------------------------------------

export interface StepFailure {
  step: string;
  exception: "IllegalArgumentException";
  message: string;
}

const blank = (s: string) => s.trim().length === 0;

export function stepFailure(d: Draft): StepFailure | null {
  if (blank(d.id)) return { step: "builder()", exception: "IllegalArgumentException", message: "id must not be blank" };
  if (d.hotel && (blank(d.hotel.name) || d.hotel.stars < 1 || d.hotel.stars > 5)) {
    return { step: ".hotel()", exception: "IllegalArgumentException", message: "Hotel needs a name and 1..5 stars" };
  }
  if (d.adults < 1 || d.children < 0) {
    return { step: ".travelers()", exception: "IllegalArgumentException", message: "Need at least 1 adult and 0+ children" };
  }
  if (d.flight !== null && blank(d.flight)) {
    return { step: ".flight()", exception: "IllegalArgumentException", message: "Flight number must not be blank" };
  }
  if (d.train !== null && blank(d.train)) {
    return { step: ".train()", exception: "IllegalArgumentException", message: "Train number must not be blank" };
  }
  return null;
}

// ---------------------------------------------------------------------------
// Level 2: TravelPackageValidator (cross-field rules, checked in build())
// ---------------------------------------------------------------------------

export type ConstraintKind =
  | "Range"
  | "Mandatory"
  | "Depends on parameter"
  | "Mutually exclusive"
  | "Mandatory under condition"
  | "Requires component";

export interface Rule {
  method: string;
  title: string;
  kind: ConstraintKind;
  /** Returns an error message, null when the rule passes, or "skip" when it does not apply. */
  check: (d: Draft) => string | null | "skip";
  extra?: boolean;
}

const isInternational = (d: Draft) => d.from.country.toLowerCase() !== d.to.country.toLowerCase();
const guests = (d: Draft) => d.adults + d.children;

export const RULES: Rule[] = [
  {
    method: "checkNightsInRange",
    title: "Stay is 1..30 nights",
    kind: "Range",
    check: (d) => (d.nights < 1 || d.nights > 30 ? `Nights must be 1..30, got ${d.nights}` : null),
  },
  {
    method: "checkHotelIsChosen",
    title: "A hotel is chosen",
    kind: "Mandatory",
    check: (d) => (d.hotel === null ? "Hotel is required" : null),
  },
  {
    method: "checkStarsMatchBudget",
    title: "Hotel stars match the budget",
    kind: "Depends on parameter",
    check: (d) => {
      if (!d.hotel) return "skip";
      const { minStars, maxStars } = BUDGETS[d.budget];
      const stars = d.hotel.stars;
      return stars < minStars || stars > maxStars
        ? `${d.budget} budget allows ${minStars}..${maxStars} stars, got ${stars}`
        : null;
    },
  },
  {
    method: "checkExactlyOneTransport",
    title: "Exactly one transport: flight or train",
    kind: "Mutually exclusive",
    check: (d) => ((d.flight !== null) === (d.train !== null) ? "Choose exactly one transport: flight or train" : null),
  },
  {
    method: "checkVisaForInternationalTrip",
    title: "International trip has visa support",
    kind: "Mandatory under condition",
    check: (d) => {
      if (!isInternational(d)) return "skip";
      return d.visaSupport ? null : "International trip requires visa support";
    },
  },
  {
    method: "checkFamilyRoomForChildren",
    title: "Children stay in a FAMILY room",
    kind: "Requires component",
    check: (d) => {
      if (d.children === 0) return "skip";
      return d.room !== "FAMILY" ? "Children require a FAMILY room" : null;
    },
  },
  {
    method: "checkGuestsFitRoom",
    title: "Guests fit the room capacity",
    kind: "Depends on parameter",
    check: (d) => {
      const capacity = ROOMS[d.room].capacity;
      return guests(d) > capacity ? `${d.room} room fits ${capacity} guests, got ${guests(d)}` : null;
    },
  },
  {
    method: "checkFlightForAirportTransfer",
    title: "Airport transfer comes with a flight",
    kind: "Requires component",
    check: (d) => {
      if (!d.airportTransfer) return "skip";
      return d.flight === null ? "Airport transfer requires a flight" : null;
    },
  },
];

/** A "changing requirement" that is not in the Java code yet — shown in section 7. */
export const NEW_RULE: Rule = {
  method: "checkStarsForAllInclusive",
  title: "ALL_INCLUSIVE only in 4–5 star hotels",
  kind: "Depends on parameter",
  extra: true,
  check: (d) => {
    if (d.meals !== "ALL_INCLUSIVE" || !d.hotel) return "skip";
    return d.hotel.stars < 4 ? "All inclusive requires 4-5 stars" : null;
  },
};

export type RuleStatus = "pass" | "fail" | "skip";

export interface RuleResult {
  rule: Rule;
  status: RuleStatus;
  message?: string;
}

export function evaluate(d: Draft, withNewRule: boolean): RuleResult[] {
  const rules = withNewRule ? [...RULES, NEW_RULE] : RULES;
  return rules.map((rule) => {
    const out = rule.check(d);
    if (out === "skip") return { rule, status: "skip" };
    if (out === null) return { rule, status: "pass" };
    return { rule, status: "fail", message: out };
  });
}

export type BuildResult =
  | { kind: "ok"; text: string }
  | { kind: "step"; failure: StepFailure }
  | { kind: "invalid"; errors: string[] };

export function build(d: Draft, withNewRule = false): BuildResult {
  const failure = stepFailure(d);
  if (failure) return { kind: "step", failure };
  const errors = evaluate(d, withNewRule).flatMap((r) => (r.message ? [r.message] : []));
  if (errors.length > 0) return { kind: "invalid", errors };
  return { kind: "ok", text: toJavaString(d) };
}

/** Same output as TravelPackage.toString() in Java. */
export function toJavaString(d: Draft): string {
  const hotel = d.hotel ? `Hotel[name=${d.hotel.name}, stars=${d.hotel.stars}]` : "null";
  const transport = d.flight !== null ? `Flight[number=${d.flight}]` : d.train !== null ? `Train[number=${d.train}]` : "null";
  return (
    `${d.id}: ${d.from.city} -> ${d.to.city}, ${d.nights} nights, ${d.budget}, ${hotel}, ${d.room} room, ` +
    `${d.meals}, guests ${d.adults}+${d.children}, ${transport}` +
    `${d.airportTransfer ? ", transfer" : ""}${d.visaSupport ? ", visa" : ""}`
  );
}

// ---------------------------------------------------------------------------
// Java code generation
// ---------------------------------------------------------------------------

const varName = (l: Location) => l.city.charAt(0).toLowerCase() + l.city.slice(1).replace(/\s+/g, "");
const str = (s: string) => JSON.stringify(s);

/** Builds the Java snippet: only steps that change something relative to the starting point. */
export function toJavaCode(d: Draft, mode: Mode): string {
  const base = baseFor(mode, d);
  const steps: string[] = [];

  if (d.nights !== base.nights) steps.push(`.nights(${d.nights})`);
  if (d.budget !== base.budget) steps.push(`.budget(BudgetLevel.${d.budget})`);
  if (d.hotel) steps.push(`.hotel(${str(d.hotel.name)}, ${d.hotel.stars})`);
  if (d.room !== base.room) steps.push(`.room(RoomType.${d.room})`);
  if (d.meals !== base.meals) steps.push(`.meals(MealPlan.${d.meals})`);
  if (d.adults !== base.adults || d.children !== base.children) steps.push(`.travelers(${d.adults}, ${d.children})`);
  if (d.flight !== null) steps.push(`.flight(${str(d.flight)})`);
  if (d.train !== null) steps.push(`.train(${str(d.train)})`);
  if (d.airportTransfer && !base.airportTransfer) steps.push(`.airportTransfer()`);
  if (d.visaSupport) steps.push(`.visaSupport()`);
  steps.push(`.build();`);

  const from = varName(d.from);
  const to = varName(d.to);
  const declarations = [
    `Location ${from} = new Location(${str(d.from.city)}, ${str(d.from.country)});`,
    from !== to ? `Location ${to} = new Location(${str(d.to.city)}, ${str(d.to.country)});` : null,
  ].filter(Boolean);

  const start =
    mode === "preset"
      ? `TravelPresets.allInclusiveBeach(${str(d.id)}, ${from}, ${to})`
      : `TravelPackage.builder(${str(d.id)}, ${from}, ${to})`;

  return [...declarations, "", `TravelPackage trip = ${start}`, ...steps.map((s) => `        ${s}`)].join("\n");
}

// ---------------------------------------------------------------------------
// Ready-made examples for the playground
// ---------------------------------------------------------------------------

const city = (name: string) => LOCATIONS.find((l) => l.city === name)!;

export const EXAMPLES: { label: string; mode: Mode; draft: Draft }[] = [
  {
    label: "Family trip",
    mode: "builder",
    draft: {
      ...builderDefaults("FAMILY-1", city("Almaty"), city("Astana")),
      nights: 4,
      hotel: { name: "Hilton", stars: 4 },
      room: "FAMILY",
      adults: 2,
      children: 2,
      train: "002",
    },
  },
  {
    label: "Beach preset",
    mode: "preset",
    draft: {
      ...presetDefaults("BEACH-1", city("Almaty"), city("Antalya")),
      hotel: { name: "Rixos", stars: 5 },
      adults: 2,
      flight: "KC 921",
      visaSupport: true,
    },
  },
  {
    label: "Broken package",
    mode: "builder",
    draft: {
      ...builderDefaults("BAD-1", city("Almaty"), city("Antalya")),
      nights: 45,
      budget: "ECONOMY",
      hotel: { name: "Palace", stars: 5 },
      adults: 2,
      children: 2,
      train: "002",
      airportTransfer: true,
    },
  },
];
