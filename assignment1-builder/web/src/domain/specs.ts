// The 25 cases of TravelPackageTest.java, run against the TypeScript mirror of the validator.

import { LOCATIONS, build, builderDefaults, presetDefaults, stepFailure, type Draft } from "./travel";

const city = (name: string) => LOCATIONS.find((l) => l.city === name)!;
const ALMATY = city("Almaty");
const ASTANA = city("Astana");
const ANTALYA = city("Antalya");

/** Same as valid() in the Java test: each case breaks exactly one thing. */
const valid = (patch: Partial<Draft> = {}): Draft => ({
  ...builderDefaults("T", ALMATY, ASTANA),
  nights: 3,
  hotel: { name: "Hilton", stars: 4 },
  train: "002",
  ...patch,
});

const ok = (d: Draft) => build(d).kind === "ok";
const hasError = (d: Draft, text: string) => {
  const r = build(d);
  return r.kind === "invalid" && r.errors.some((e) => e.includes(text));
};
const throwsInStep = (d: Draft) => stepFailure(d) !== null;

export interface Spec {
  group: "Builder" | "Rules" | "Preset";
  name: string;
  run: () => boolean;
}

const abroad = (patch: Partial<Draft> = {}): Draft => ({
  ...builderDefaults("T", ALMATY, ANTALYA),
  nights: 3,
  hotel: { name: "H", stars: 4 },
  flight: "KC 921",
  ...patch,
});

export const SPECS: Spec[] = [
  {
    group: "Builder",
    name: "buildsWithDefaults",
    run: () => {
      const d = valid();
      return ok(d) && d.budget === "STANDARD" && d.room === "STANDARD" && d.meals === "BREAKFAST" && d.adults === 1;
    },
  },
  {
    group: "Builder",
    name: "buildsWithAllValues",
    run: () =>
      ok({
        ...builderDefaults("T", ALMATY, ANTALYA),
        nights: 10, budget: "PREMIUM", hotel: { name: "Rixos", stars: 5 }, room: "FAMILY",
        meals: "ALL_INCLUSIVE", adults: 2, children: 1, flight: "KC 921", airportTransfer: true, visaSupport: true,
      }),
  },
  { group: "Builder", name: "stepOrderDoesNotMatter", run: () => ok(valid({ hotel: { name: "Rixos", stars: 5 }, budget: "PREMIUM" })) },
  {
    group: "Builder",
    name: "rejectsBadArgumentsImmediately",
    run: () =>
      throwsInStep(valid({ id: " " })) &&
      throwsInStep(valid({ adults: 0, children: 1 })) &&
      throwsInStep(valid({ hotel: { name: "Hilton", stars: 6 } })),
  },
  ...([[0, false], [1, true], [30, true], [31, false]] as const).map(([nights, expected]) => ({
    group: "Rules" as const,
    name: `nightsRange [${nights}, ${expected}]`,
    run: () => (expected ? ok(valid({ nights })) : hasError(valid({ nights }), "Nights")),
  })),
  {
    group: "Rules",
    name: "hotelIsRequired",
    run: () => hasError(valid({ hotel: null }), "Hotel is required"),
  },
  ...(
    [["ECONOMY", 3, true], ["ECONOMY", 4, false], ["STANDARD", 2, false], ["PREMIUM", 4, true], ["PREMIUM", 3, false]] as const
  ).map(([budget, stars, expected]) => ({
    group: "Rules" as const,
    name: `hotelStarsDependOnBudget [${budget}, ${stars}, ${expected}]`,
    run: () => {
      const d = valid({ budget, hotel: { name: "H", stars } });
      return expected ? ok(d) : hasError(d, "budget allows");
    },
  })),
  {
    group: "Rules",
    name: "flightAndTrainAreMutuallyExclusive",
    run: () =>
      hasError(valid({ flight: "KC 1" }), "exactly one transport") &&
      hasError(valid({ train: null }), "exactly one transport"),
  },
  {
    group: "Rules",
    name: "internationalTripNeedsVisaSupport",
    run: () => hasError(abroad(), "visa") && ok(abroad({ visaSupport: true })),
  },
  {
    group: "Rules",
    name: "childrenNeedFamilyRoom",
    run: () =>
      hasError(valid({ room: "SUITE", adults: 1, children: 1 }), "FAMILY room") &&
      ok(valid({ room: "FAMILY", adults: 2, children: 2 })),
  },
  ...([["STANDARD", 2, true], ["STANDARD", 3, false], ["FAMILY", 4, true], ["FAMILY", 5, false]] as const).map(
    ([room, adults, expected]) => ({
      group: "Rules" as const,
      name: `guestsMustFitRoom [${room}, ${adults}, ${expected}]`,
      run: () => {
        const d = valid({ room, adults });
        return expected ? ok(d) : hasError(d, "fits");
      },
    }),
  ),
  { group: "Rules", name: "airportTransferNeedsFlight", run: () => hasError(valid({ airportTransfer: true }), "Airport transfer") },
  {
    group: "Rules",
    name: "reportsAllErrorsAtOnce",
    run: () => {
      const r = build({
        ...builderDefaults("BAD", ALMATY, ANTALYA),
        nights: 45, budget: "ECONOMY", hotel: { name: "Palace", stars: 5 },
        adults: 2, children: 2, train: "002", airportTransfer: true,
      });
      return r.kind === "invalid" && r.errors.length === 6;
    },
  },
  {
    group: "Preset",
    name: "beachPresetWorksAfterAddingHotelAndFlight",
    run: () => {
      const d = { ...presetDefaults("B", ALMATY, ANTALYA), hotel: { name: "Rixos", stars: 5 }, flight: "KC 921", visaSupport: true };
      return ok(d) && d.nights === 7 && d.budget === "PREMIUM" && d.meals === "ALL_INCLUSIVE" && d.airportTransfer;
    },
  },
  {
    group: "Preset",
    name: "presetCanBeChangedButIsStillValidated",
    run: () => {
      const preset = { ...presetDefaults("B", ALMATY, ANTALYA), flight: "KC 921", visaSupport: true };
      return (
        ok({ ...preset, nights: 14, hotel: { name: "Rixos", stars: 5 } }) &&
        hasError({ ...preset, nights: 14, hotel: { name: "Cheap Inn", stars: 2 } }, "budget allows")
      );
    },
  },
];
