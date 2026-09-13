import { KindBadge, Section, StatusIcon } from "../components/ui";
import { RULES } from "../domain/travel";

const RULE_DETAILS: Record<string, string> = {
  checkNightsInRange: "nights ∈ 1..30",
  checkHotelIsChosen: "hotel ≠ null",
  checkStarsMatchBudget: "ECONOMY 1–3★ · STANDARD 3–4★ · PREMIUM 4–5★",
  checkExactlyOneTransport: "flight XOR train",
  checkVisaForInternationalTrip: "from.country ≠ to.country ⇒ visaSupport",
  checkFamilyRoomForChildren: "children > 0 ⇒ room = FAMILY",
  checkGuestsFitRoom: "adults + children ≤ STANDARD 2 · SUITE 3 · FAMILY 4",
  checkFlightForAirportTransfer: "airportTransfer ⇒ flight ≠ null",
};

const REQUIRED = [
  { text: "One parameter depends on another parameter", covered: ["checkStarsMatchBudget"] },
  { text: "Two options are mutually exclusive", covered: ["checkExactlyOneTransport"] },
  { text: "A parameter becomes mandatory under a specific condition", covered: ["checkVisaForInternationalTrip"] },
  { text: "Parameters have dependent valid ranges", covered: ["checkNightsInRange", "checkGuestsFitRoom"] },
  { text: "One component requires another component", covered: ["checkFamilyRoomForChildren", "checkFlightForAirportTransfer"] },
];

export function Rules() {
  return (
    <Section
      id="rules"
      eyebrow="04 · Rules"
      title="Eight rules, one method each."
      intro="Every rule lives in its own small method of TravelPackageValidator. Limits like star ranges and room capacity live in the enums, not in the rules."
    >
      <div className="grid gap-10 lg:grid-cols-[1.4fr_1fr]">
        <ol className="grid gap-3 sm:grid-cols-2">
          {RULES.map((rule, i) => (
            <li
              key={rule.method}
              className="flex flex-col rounded-2xl border border-stone-200 bg-white p-5 dark:border-stone-800 dark:bg-stone-900"
            >
              <div className="flex items-start justify-between gap-3">
                <span className="font-mono text-xs text-stone-400">R{i + 1}</span>
                <KindBadge kind={rule.kind} />
              </div>
              <p className="mt-3 font-semibold">{rule.title}</p>
              <p className="mt-1 text-sm text-stone-600 dark:text-stone-400">{RULE_DETAILS[rule.method]}</p>
              <p className="mt-auto truncate pt-4 font-mono text-xs text-teal-700 dark:text-teal-400">{rule.method}()</p>
            </li>
          ))}
        </ol>

        <div className="self-start rounded-2xl border border-stone-200 bg-stone-100/70 p-6 dark:border-stone-800 dark:bg-stone-900/50">
          <p className="text-sm font-semibold">Constraint types required by the assignment</p>
          <ul className="mt-5 space-y-5">
            {REQUIRED.map((r) => (
              <li key={r.text} className="flex gap-3">
                <span className="mt-0.5">
                  <StatusIcon status="pass" />
                </span>
                <div className="min-w-0">
                  <p className="text-sm">{r.text}</p>
                  <p className="mt-1 font-mono text-xs break-words text-stone-500 dark:text-stone-400">
                    {r.covered.join(", ")}
                  </p>
                </div>
              </li>
            ))}
          </ul>
          <div className="mt-6 border-t border-stone-200 pt-5 dark:border-stone-800">
            <p className="text-sm font-semibold">Required preset</p>
            <p className="mt-1 font-mono text-xs text-stone-500 dark:text-stone-400">
              TravelPresets.allInclusiveBeach(id, from, to)
            </p>
            <p className="mt-2 text-sm text-stone-600 dark:text-stone-400">
              7 nights · PREMIUM · ALL_INCLUSIVE · airport transfer. Returns a Builder, so hotel and flight are added after.
            </p>
          </div>
        </div>
      </div>
    </Section>
  );
}
