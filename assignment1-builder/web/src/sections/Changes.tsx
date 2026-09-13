import { CodeBlock } from "../components/Code";
import { Section, Toggle } from "../components/ui";

const CHANGES = [
  { need: "New budget level or room type", change: "Add one enum constant", files: "BudgetLevel / RoomType" },
  { need: "New business rule", change: "One method + one line in validate()", files: "TravelPackageValidator" },
  { need: "New option, e.g. insurance", change: "One field + one builder step", files: "TravelPackage" },
  { need: "New preset", change: "One static method", files: "TravelPresets" },
];

const DIFF = ` void validate() {
     ...
     checkFlightForAirportTransfer();
+    checkStarsForAllInclusive();
     if (!errors.isEmpty()) {
         throw new InvalidTravelPackageException(errors);
     }
 }
+
+private void checkStarsForAllInclusive() {
+    if (trip.meals() == MealPlan.ALL_INCLUSIVE && trip.hotel() != null
+            && trip.hotel().stars() < 4) {
+        errors.add("All inclusive requires 4-5 stars");
+    }
+}`;

interface Props {
  withNewRule: boolean;
  onToggle: (on: boolean) => void;
  onTry: () => void;
}

export function Changes({ withNewRule, onToggle, onTry }: Props) {
  return (
    <Section
      id="changes"
      eyebrow="07 · Changing requirements"
      title="New requirement? A small, local change."
      intro="Rules are isolated in the validator and limits in the enums, so each typical change touches one place. Client code that uses the builder doesn't change."
    >
      <div className="overflow-x-auto rounded-2xl border border-stone-200 dark:border-stone-800">
        <table className="w-full min-w-[560px] text-left text-sm">
          <thead className="bg-stone-100 text-xs text-stone-500 uppercase dark:bg-stone-900 dark:text-stone-400">
            <tr>
              <th className="px-5 py-3 font-medium">New requirement</th>
              <th className="px-5 py-3 font-medium">What changes</th>
              <th className="px-5 py-3 font-medium">Where</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-stone-200 bg-white dark:divide-stone-800 dark:bg-stone-950">
            {CHANGES.map((c) => (
              <tr key={c.need}>
                <td className="px-5 py-3.5 font-medium">{c.need}</td>
                <td className="px-5 py-3.5 text-stone-600 dark:text-stone-400">{c.change}</td>
                <td className="px-5 py-3.5 font-mono text-xs text-teal-700 dark:text-teal-400">{c.files}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="mt-10 grid gap-6 lg:grid-cols-[1fr_1.3fr]">
        <div className="self-start rounded-2xl border border-stone-200 bg-white p-6 dark:border-stone-800 dark:bg-stone-900">
          <p className="font-mono text-xs text-stone-500 uppercase tracking-wider">Live demo</p>
          <p className="mt-2 text-xl font-semibold tracking-tight">"All inclusive only in 4–5 star hotels."</p>
          <p className="mt-3 text-sm leading-relaxed text-stone-600 dark:text-stone-400">
            Turn the new rule on. It is added to the validator in the playground, exactly like the diff on the right. Packages
            built from <code className="font-mono">allInclusiveBeach()</code> stay valid, because its PREMIUM budget already
            requires 4–5★.
          </p>
          <div className="mt-6 space-y-4">
            <Toggle checked={withNewRule} onChange={onToggle} label="Add checkStarsForAllInclusive()" />
            <button
              onClick={onTry}
              className="text-sm font-semibold text-teal-700 underline-offset-4 hover:underline dark:text-teal-400"
            >
              Try it: ALL_INCLUSIVE in a 3★ hotel →
            </button>
          </div>
        </div>
        <CodeBlock title="TravelPackageValidator.java" code={DIFF} diff />
      </div>
    </Section>
  );
}
