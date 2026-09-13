import { CodeBlock } from "../components/Code";
import { Section } from "../components/ui";

const CONSTRUCTOR = `// What is 2? What is 1? Which boolean is visa?
new TravelPackage("BEACH-1", almaty, antalya, 7,
        BudgetLevel.PREMIUM, hotel, RoomType.FAMILY,
        MealPlan.ALL_INCLUSIVE, 2, 1, flight, null,
        true, true);`;

const SETTERS = `TravelPackage trip = new TravelPackage();
trip.setHotel(new Hotel("Rixos", 5));
// here the package is already broken:
// no nights, no transport, 5★ with STANDARD budget
trip.setBudget(BudgetLevel.PREMIUM);`;

const BUILDER = `TravelPackage trip = TravelPackage.builder("FAMILY-1", almaty, astana)
        .nights(4)
        .hotel("Hilton", 4)
        .room(RoomType.FAMILY)
        .travelers(2, 2)
        .train("002")
        .build();   // valid, or an exception with every error`;

const WINS = [
  { title: "Readable", text: "Every value has a name. No guessing what the fifth argument is." },
  { title: "Immutable", text: "Final fields, no setters. A package cannot change after build()." },
  { title: "Always valid", text: "Rules run in one place. An invalid package is never returned." },
];

export function Problem() {
  return (
    <Section
      id="problem"
      eyebrow="01 · Problem"
      title="14 fields, 3 required, and rules between them."
      intro={
        <>
          Hotel stars must match the budget. Children need a family room. A trip abroad needs visa support. A constructor
          can't express that, and setters let the object exist half-broken.
        </>
      }
    >
      <div className="grid gap-6 lg:grid-cols-2">
        <div className="space-y-6">
          <div>
            <p className="mb-2 flex items-center gap-2 text-sm font-medium text-rose-700 dark:text-rose-400">
              <span className="size-1.5 rounded-full bg-rose-500" /> Telescoping constructor
            </p>
            <CodeBlock code={CONSTRUCTOR} />
          </div>
          <div>
            <p className="mb-2 flex items-center gap-2 text-sm font-medium text-rose-700 dark:text-rose-400">
              <span className="size-1.5 rounded-full bg-rose-500" /> JavaBean setters
            </p>
            <CodeBlock code={SETTERS} />
          </div>
        </div>
        <div className="flex flex-col">
          <p className="mb-2 flex items-center gap-2 text-sm font-medium text-emerald-700 dark:text-emerald-400">
            <span className="size-1.5 rounded-full bg-emerald-500" /> Builder
          </p>
          <CodeBlock code={BUILDER} />
          <div className="mt-6 grid flex-1 gap-3 sm:grid-cols-3 lg:grid-cols-1 xl:grid-cols-3">
            {WINS.map((w) => (
              <div key={w.title} className="rounded-2xl border border-stone-200 bg-white p-4 dark:border-stone-800 dark:bg-stone-900">
                <p className="font-semibold">{w.title}</p>
                <p className="mt-1 text-sm leading-relaxed text-stone-600 dark:text-stone-400">{w.text}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      <p className="mt-8 max-w-3xl rounded-2xl border-l-4 border-teal-500 bg-teal-50 px-5 py-4 text-sm leading-relaxed text-teal-950 dark:bg-teal-400/5 dark:text-teal-100">
        <strong>Why the check must wait for build():</strong> "stars depend on budget" can't be checked inside{" "}
        <code className="font-mono">hotel()</code>, because the budget may be set later — or was already set by a preset.
      </p>
    </Section>
  );
}
