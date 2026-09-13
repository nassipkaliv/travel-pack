import { CodeBlock } from "../components/Code";

const SNIPPET = `TravelPackage beach = TravelPresets
        .allInclusiveBeach("BEACH-1", almaty, antalya)
        .hotel("Rixos", 5)
        .flight("KC 921")
        .travelers(2, 0)
        .visaSupport()
        .build();`;

const STATS = [
  { value: "8", label: "business rules" },
  { value: "5 / 5", label: "constraint types" },
  { value: "25", label: "passing tests" },
  { value: "1", label: "preset" },
];

export function Hero() {
  return (
    <header className="relative overflow-hidden border-b border-stone-200 dark:border-stone-800">
      <div
        aria-hidden
        className="pointer-events-none absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,var(--color-teal-100),transparent_60%)] dark:bg-[radial-gradient(ellipse_at_top_right,var(--color-teal-950),transparent_60%)]"
      />
      <div className="relative mx-auto grid max-w-6xl items-center gap-12 px-4 pt-16 pb-20 sm:px-6 lg:grid-cols-[1.1fr_1fr] lg:pt-24 lg:pb-28">
        <div>
          <p className="inline-flex flex-wrap items-center gap-2 rounded-full border border-stone-200 bg-white/70 px-3 py-1 text-xs font-medium text-stone-600 backdrop-blur dark:border-stone-800 dark:bg-stone-900/70 dark:text-stone-400">
            <span className="size-1.5 rounded-full bg-teal-500" />
            Assignment 1 · Software Design Patterns · Java 17
          </p>
          <h1 className="mt-6 text-4xl font-semibold tracking-tight text-balance sm:text-6xl">
            Travel Package <span className="text-teal-700 dark:text-teal-400">Builder</span>
          </h1>
          <p className="mt-6 max-w-xl text-lg leading-relaxed text-pretty text-stone-600 dark:text-stone-400">
            A travel package has 14 fields that depend on each other. The Builder pattern turns them into a readable fluent API
            and guarantees that every package it returns is valid.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <a
              href="#playground"
              className="rounded-full bg-stone-900 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-teal-700 dark:bg-stone-100 dark:text-stone-900 dark:hover:bg-teal-300"
            >
              Open the playground
            </a>
            <a
              href="#design"
              className="rounded-full border border-stone-300 px-5 py-2.5 text-sm font-semibold transition hover:border-stone-900 dark:border-stone-700 dark:hover:border-stone-300"
            >
              See the design
            </a>
          </div>
          <dl className="mt-12 grid grid-cols-2 gap-6 sm:grid-cols-4">
            {STATS.map((s) => (
              <div key={s.label}>
                <dt className="text-xs text-stone-500 dark:text-stone-400">{s.label}</dt>
                <dd className="mt-1 text-2xl font-semibold tracking-tight tabular-nums">{s.value}</dd>
              </div>
            ))}
          </dl>
        </div>

        <div className="min-w-0">
          <CodeBlock title="Main.java" code={SNIPPET} className="shadow-xl shadow-stone-900/5" />
          <div className="mt-3 flex items-start gap-2.5 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 dark:border-emerald-400/20 dark:bg-emerald-400/5">
            <span className="mt-1 size-2 shrink-0 rounded-full bg-emerald-500" />
            <p className="min-w-0 font-mono text-xs leading-relaxed break-words text-emerald-900 dark:text-emerald-200">
              BEACH-1: Almaty -&gt; Antalya, 7 nights, PREMIUM, Hotel[name=Rixos, stars=5], STANDARD room, ALL_INCLUSIVE, guests
              2+0, Flight[number=KC 921], transfer, visa
            </p>
          </div>
        </div>
      </div>
    </header>
  );
}
