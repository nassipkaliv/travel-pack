import { useMemo } from "react";
import { CodeBlock } from "../components/Code";
import { Card, Field, KindBadge, Section, Segmented, StatusIcon, Stepper, Toggle, inputClass } from "../components/ui";
import {
  BUDGETS,
  EXAMPLES,
  LOCATIONS,
  MEAL_PLANS,
  ROOMS,
  build,
  evaluate,
  toJavaCode,
  type BudgetLevel,
  type Draft,
  type Mode,
  type RoomType,
} from "../domain/travel";

export interface PlaygroundState {
  mode: Mode;
  draft: Draft;
}

interface Props {
  state: PlaygroundState;
  onChange: (state: PlaygroundState) => void;
  withNewRule: boolean;
}

export function Playground({ state, onChange, withNewRule }: Props) {
  const { mode, draft } = state;
  const set = (patch: Partial<Draft>) => onChange({ mode, draft: { ...draft, ...patch } });

  const code = useMemo(() => toJavaCode(draft, mode), [draft, mode]);
  const result = useMemo(() => build(draft, withNewRule), [draft, withNewRule]);
  const rules = useMemo(() => evaluate(draft, withNewRule), [draft, withNewRule]);

  const switchMode = (next: Mode) => {
    if (next === mode) return;
    const presetValues = { nights: 7, budget: "PREMIUM" as const, meals: "ALL_INCLUSIVE" as const, airportTransfer: true };
    onChange({ mode: next, draft: next === "preset" ? { ...draft, ...presetValues } : draft });
  };

  const locationSelect = (value: Draft["from"], onPick: (l: Draft["from"]) => void, label: string) => (
    <select
      aria-label={label}
      className={inputClass}
      value={value.city}
      onChange={(e) => onPick(LOCATIONS.find((l) => l.city === e.target.value)!)}
    >
      {LOCATIONS.map((l) => (
        <option key={l.city} value={l.city}>
          {l.city}
        </option>
      ))}
    </select>
  );

  const failed = rules.filter((r) => r.status === "fail").length;
  const buildReached = result.kind !== "step";

  return (
    <Section
      id="playground"
      eyebrow="02 · Playground"
      title="Build a package. Watch the rules react."
      intro={
        <>
          Every control below is a builder step. The Java code, the rule checks and the result of{" "}
          <code className="font-mono text-base text-teal-700 dark:text-teal-400">build()</code> update live, using the same
          rules and messages as the Java validator.
        </>
      }
    >
      <div className="mb-6 flex flex-wrap items-center gap-2">
        <span className="mr-1 text-sm text-stone-500 dark:text-stone-400">Try an example:</span>
        {EXAMPLES.map((ex) => (
          <button
            key={ex.label}
            onClick={() => onChange({ mode: ex.mode, draft: ex.draft })}
            className="rounded-full border border-stone-200 bg-white px-3.5 py-1.5 text-sm font-medium transition hover:border-teal-500 hover:text-teal-700 dark:border-stone-700 dark:bg-stone-900 dark:hover:border-teal-400 dark:hover:text-teal-300"
          >
            {ex.label}
          </button>
        ))}
      </div>

      <div className="grid gap-6 lg:grid-cols-[minmax(0,400px)_minmax(0,1fr)]">
        {/* ---------------- Controls ---------------- */}
        <Card className="space-y-6 self-start">
          <Field label="Start from">
            <Segmented<Mode>
              label="Start from"
              value={mode}
              onChange={switchMode}
              options={[
                { value: "builder", label: "TravelPackage.builder()", hint: "empty builder" },
                { value: "preset", label: "allInclusiveBeach()", hint: "preset" },
              ]}
            />
          </Field>

          <div className="grid grid-cols-2 gap-3">
            <Field label="Package id">
              <input className={inputClass} value={draft.id} onChange={(e) => set({ id: e.target.value })} />
            </Field>
            <Field label="Nights" hint="1..30">
              <input
                type="number"
                className={inputClass}
                value={draft.nights}
                min={0}
                max={60}
                onChange={(e) => set({ nights: Number(e.target.value) || 0 })}
              />
            </Field>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Field label="From" hint={draft.from.country}>{locationSelect(draft.from, (from) => set({ from }), "From")}</Field>
            <Field label="To" hint={draft.to.country}>{locationSelect(draft.to, (to) => set({ to }), "To")}</Field>
          </div>

          <Field label="Budget">
            <Segmented<BudgetLevel>
              label="Budget"
              value={draft.budget}
              onChange={(budget) => set({ budget })}
              options={(Object.keys(BUDGETS) as BudgetLevel[]).map((b) => ({
                value: b,
                label: b,
                hint: `${BUDGETS[b].minStars}–${BUDGETS[b].maxStars}★`,
              }))}
            />
          </Field>

          <div className="space-y-3">
            <Toggle
              checked={draft.hotel !== null}
              onChange={(on) => set({ hotel: on ? { name: "Hilton", stars: 4 } : null })}
              label={<span className="font-medium">Hotel</span>}
            />
            {draft.hotel && (
              <div className="grid grid-cols-[1fr_auto] gap-3">
                <input
                  aria-label="Hotel name"
                  className={inputClass}
                  value={draft.hotel.name}
                  onChange={(e) => set({ hotel: { ...draft.hotel!, name: e.target.value } })}
                />
                <div className="flex items-center" role="radiogroup" aria-label="Hotel stars">
                  {[1, 2, 3, 4, 5].map((s) => (
                    <button
                      key={s}
                      role="radio"
                      aria-checked={draft.hotel!.stars === s}
                      aria-label={`${s} stars`}
                      onClick={() => set({ hotel: { ...draft.hotel!, stars: s } })}
                      className={`px-0.5 text-xl leading-none transition ${
                        s <= draft.hotel!.stars ? "text-amber-400" : "text-stone-300 dark:text-stone-700"
                      }`}
                    >
                      ★
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>

          <Field label="Room">
            <Segmented<RoomType>
              label="Room"
              value={draft.room}
              onChange={(room) => set({ room })}
              options={(Object.keys(ROOMS) as RoomType[]).map((r) => ({
                value: r,
                label: r,
                hint: `fits ${ROOMS[r].capacity}`,
              }))}
            />
          </Field>

          <Field label="Meals">
            <select
              aria-label="Meals"
              className={inputClass}
              value={draft.meals}
              onChange={(e) => set({ meals: e.target.value as Draft["meals"] })}
            >
              {MEAL_PLANS.map((m) => (
                <option key={m}>{m}</option>
              ))}
            </select>
          </Field>

          <div className="grid grid-cols-2 gap-3">
            <Field label="Adults">
              <Stepper label="adults" value={draft.adults} min={0} max={6} onChange={(adults) => set({ adults })} />
            </Field>
            <Field label="Children">
              <Stepper label="children" value={draft.children} min={0} max={6} onChange={(children) => set({ children })} />
            </Field>
          </div>

          <div className="space-y-3">
            <p className="text-sm font-medium">Transport</p>
            <TransportRow
              label="Flight"
              value={draft.flight}
              placeholder="KC 921"
              onChange={(flight) => set({ flight })}
            />
            <TransportRow label="Train" value={draft.train} placeholder="002" onChange={(train) => set({ train })} />
          </div>

          <div className="space-y-3 border-t border-stone-200 pt-5 dark:border-stone-800">
            <Toggle
              checked={draft.airportTransfer}
              disabled={mode === "preset"}
              onChange={(airportTransfer) => set({ airportTransfer })}
              label="Airport transfer"
            />
            {mode === "preset" && (
              <p className="text-xs leading-relaxed text-stone-500 dark:text-stone-400">
                The preset already calls <code className="font-mono">airportTransfer()</code>, and the builder has no step to
                undo it.
              </p>
            )}
            <Toggle checked={draft.visaSupport} onChange={(visaSupport) => set({ visaSupport })} label="Visa support" />
          </div>
        </Card>

        {/* ---------------- Output ---------------- */}
        <div className="min-w-0 space-y-6">
          <CodeBlock title="Main.java" code={code} />

          <ResultCard result={result} />

          <Card className="p-0">
            <div className="flex flex-wrap items-center justify-between gap-x-4 gap-y-1 border-b border-stone-200 px-5 py-3.5 dark:border-stone-800">
              <p className="font-mono text-sm">TravelPackageValidator.validate()</p>
              {buildReached ? (
                <p className="shrink-0 text-xs text-stone-500 dark:text-stone-400">
                  {failed === 0 ? "all rules pass" : `${failed} of ${rules.length} fail`}
                </p>
              ) : (
                <p className="shrink-0 text-xs text-amber-600 dark:text-amber-400">not reached</p>
              )}
            </div>
            <ul className={`divide-y divide-stone-100 dark:divide-stone-800 ${buildReached ? "" : "opacity-40"}`}>
              {rules.map(({ rule, status, message }) => (
                <li key={rule.method} className="flex gap-3 px-5 py-3">
                  <span className="mt-0.5">
                    <StatusIcon status={status} />
                  </span>
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
                      <span className={`text-sm font-medium ${status === "skip" ? "text-stone-400" : ""}`}>{rule.title}</span>
                      {rule.extra && (
                        <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-emerald-800 dark:bg-emerald-400/10 dark:text-emerald-300">
                          new
                        </span>
                      )}
                    </div>
                    <p className="mt-0.5 truncate font-mono text-xs text-stone-400">{rule.method}()</p>
                    {message && <p className="mt-1 text-sm text-rose-600 dark:text-rose-400">{message}</p>}
                  </div>
                  <div className="hidden sm:block">
                    <KindBadge kind={rule.kind} />
                  </div>
                </li>
              ))}
            </ul>
          </Card>
        </div>
      </div>
    </Section>
  );
}

function TransportRow({
  label,
  value,
  placeholder,
  onChange,
}: {
  label: string;
  value: string | null;
  placeholder: string;
  onChange: (value: string | null) => void;
}) {
  return (
    <div className="grid grid-cols-[auto_1fr] items-center gap-3">
      <div className="w-24">
        <Toggle checked={value !== null} onChange={(on) => onChange(on ? placeholder : null)} label={label} />
      </div>
      <input
        aria-label={`${label} number`}
        className={`${inputClass} ${value === null ? "invisible" : ""}`}
        value={value ?? ""}
        placeholder={placeholder}
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
  );
}

function ResultCard({ result }: { result: ReturnType<typeof build> }) {
  if (result.kind === "ok") {
    return (
      <div className="rounded-2xl border border-emerald-200 bg-emerald-50 p-5 dark:border-emerald-400/20 dark:bg-emerald-400/5">
        <div className="flex items-center gap-2.5">
          <StatusIcon status="pass" />
          <p className="font-medium text-emerald-900 dark:text-emerald-200">
            build() returned an immutable <code className="font-mono">TravelPackage</code>
          </p>
        </div>
        <p className="mt-3 rounded-lg bg-white/70 px-3 py-2 font-mono text-[13px] leading-relaxed break-words text-stone-700 dark:bg-stone-950/60 dark:text-stone-300">
          {result.text}
        </p>
      </div>
    );
  }

  if (result.kind === "step") {
    return (
      <div className="rounded-2xl border border-amber-200 bg-amber-50 p-5 dark:border-amber-400/20 dark:bg-amber-400/5">
        <p className="font-medium text-amber-900 dark:text-amber-200">
          <code className="font-mono">{result.failure.exception}</code> thrown at{" "}
          <code className="font-mono">{result.failure.step}</code>
        </p>
        <p className="mt-1 text-sm text-amber-800 dark:text-amber-300">{result.failure.message}</p>
        <p className="mt-3 text-xs text-amber-700/80 dark:text-amber-300/70">
          Level 1 check: a value that is wrong on its own fails immediately, so <code className="font-mono">build()</code> is
          never reached.
        </p>
      </div>
    );
  }

  return (
    <div className="rounded-2xl border border-rose-200 bg-rose-50 p-5 dark:border-rose-400/20 dark:bg-rose-400/5">
      <div className="flex items-center gap-2.5">
        <StatusIcon status="fail" />
        <p className="font-medium text-rose-900 dark:text-rose-200">
          <code className="font-mono">InvalidTravelPackageException</code> · {result.errors.length}{" "}
          {result.errors.length === 1 ? "error" : "errors"}
        </p>
      </div>
      <ul className="mt-3 space-y-1.5 font-mono text-[13px] text-rose-800 dark:text-rose-300">
        {result.errors.map((e) => (
          <li key={e} className="flex gap-2">
            <span className="select-none text-rose-400">–</span>
            {e}
          </li>
        ))}
      </ul>
    </div>
  );
}
