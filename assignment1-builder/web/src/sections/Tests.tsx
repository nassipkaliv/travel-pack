import { useState } from "react";
import { Section, StatusIcon } from "../components/ui";
import { SPECS, type Spec } from "../domain/specs";

type State = "idle" | "running" | "done";

export function Tests() {
  const [state, setState] = useState<State>("idle");
  const [results, setResults] = useState<Record<string, boolean>>({});

  const run = async () => {
    setState("running");
    setResults({});
    for (const spec of SPECS) {
      await new Promise((resolve) => setTimeout(resolve, 45));
      const passed = spec.run();
      setResults((prev) => ({ ...prev, [spec.name]: passed }));
    }
    setState("done");
  };

  const passed = Object.values(results).filter(Boolean).length;
  const groups: Spec["group"][] = ["Builder", "Rules", "Preset"];

  return (
    <Section
      id="tests"
      eyebrow="06 · Tests"
      title="25 tests. Every rule, both sides of every boundary."
      intro="JUnit 5 with parameterized tests for the boundaries: 0/1 and 30/31 nights, each budget's star limits, each room's capacity."
    >
      <div className="grid gap-6 lg:grid-cols-[minmax(0,340px)_minmax(0,1fr)]">
        <div className="space-y-4 self-start">
          <div className="overflow-hidden rounded-2xl border border-stone-800 bg-stone-950 text-stone-200 shadow-sm">
            <div className="border-b border-stone-800 px-4 py-2.5 font-mono text-xs text-stone-500">terminal</div>
            <pre className="overflow-x-auto p-4 font-mono text-[13px] leading-6">
              <span className="text-stone-500">$</span> ./mvnw test{"\n"}
              <span className="text-stone-400">Running TravelPackageTest</span>
              {"\n"}
              <span className="text-emerald-400">Tests run: 25, Failures: 0, Errors: 0</span>
              {"\n"}
              <span className="font-semibold text-emerald-400">BUILD SUCCESS</span>
            </pre>
          </div>

          <div className="rounded-2xl border border-stone-200 bg-white p-5 dark:border-stone-800 dark:bg-stone-900">
            <p className="text-sm leading-relaxed text-stone-600 dark:text-stone-400">
              Run the same 25 cases right here, against the TypeScript copy of the validator that powers the playground.
            </p>
            <button
              onClick={run}
              disabled={state === "running"}
              className="mt-4 w-full rounded-full bg-teal-700 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-teal-800 disabled:opacity-60 dark:bg-teal-500 dark:text-teal-950 dark:hover:bg-teal-400"
            >
              {state === "running" ? "Running…" : state === "done" ? "Run again" : "Run tests in the browser"}
            </button>
            {state !== "idle" && (
              <div className="mt-4">
                <div className="h-1.5 overflow-hidden rounded-full bg-stone-100 dark:bg-stone-800">
                  <div
                    className="h-full bg-emerald-500 transition-all"
                    style={{ width: `${(Object.keys(results).length / SPECS.length) * 100}%` }}
                  />
                </div>
                <p className="mt-2 font-mono text-xs text-stone-500 tabular-nums" aria-live="polite">
                  {passed} / {SPECS.length} passed
                </p>
              </div>
            )}
          </div>
        </div>

        <div className="grid min-w-0 gap-4 sm:grid-cols-2">
          {groups.map((group) => (
            <div
              key={group}
              className={`rounded-2xl border border-stone-200 bg-white dark:border-stone-800 dark:bg-stone-900 ${
                group === "Rules" ? "sm:col-span-2" : ""
              }`}
            >
              <p className="border-b border-stone-200 px-5 py-3 text-sm font-semibold dark:border-stone-800">
                {group} <span className="font-normal text-stone-400">· {SPECS.filter((s) => s.group === group).length}</span>
              </p>
              <ul className={`gap-x-2 p-2 ${group === "Rules" ? "md:columns-2" : ""}`}>
                {SPECS.filter((s) => s.group === group).map((spec) => {
                  const result = results[spec.name];
                  return (
                    <li key={spec.name} className="flex break-inside-avoid items-center gap-2.5 rounded-lg px-3 py-1.5">
                      {result === undefined ? (
                        <span className="grid size-5 shrink-0 place-items-center">
                          <span className="size-1.5 rounded-full bg-stone-300 dark:bg-stone-600" />
                        </span>
                      ) : (
                        <StatusIcon status={result ? "pass" : "fail"} />
                      )}
                      <span className="min-w-0 font-mono text-xs break-all" title={spec.name}>
                        {spec.name}
                      </span>
                    </li>
                  );
                })}
              </ul>
            </div>
          ))}
        </div>
      </div>
    </Section>
  );
}
