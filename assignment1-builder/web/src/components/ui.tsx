import type { ReactNode } from "react";
import type { ConstraintKind } from "../domain/travel";

export function Section({
  id,
  eyebrow,
  title,
  intro,
  children,
}: {
  id: string;
  eyebrow: string;
  title: string;
  intro?: ReactNode;
  children: ReactNode;
}) {
  return (
    <section id={id} className="mx-auto max-w-6xl px-4 py-20 sm:px-6 sm:py-24">
      <div className="max-w-2xl">
        <p className="font-mono text-xs font-medium uppercase tracking-[0.2em] text-teal-700 dark:text-teal-400">
          {eyebrow}
        </p>
        <h2 className="mt-3 text-3xl font-semibold tracking-tight text-balance sm:text-4xl">{title}</h2>
        {intro && <p className="mt-4 text-lg leading-relaxed text-stone-600 dark:text-stone-400">{intro}</p>}
      </div>
      <div className="mt-12">{children}</div>
    </section>
  );
}

export function Card({ children, className = "" }: { children: ReactNode; className?: string }) {
  return (
    <div
      className={`rounded-2xl border border-stone-200 bg-white p-6 shadow-sm dark:border-stone-800 dark:bg-stone-900 ${className}`}
    >
      {children}
    </div>
  );
}

const KIND_STYLE: Record<ConstraintKind, string> = {
  Range: "bg-amber-100 text-amber-800 dark:bg-amber-400/10 dark:text-amber-300",
  Mandatory: "bg-sky-100 text-sky-800 dark:bg-sky-400/10 dark:text-sky-300",
  "Depends on parameter": "bg-violet-100 text-violet-800 dark:bg-violet-400/10 dark:text-violet-300",
  "Mutually exclusive": "bg-rose-100 text-rose-800 dark:bg-rose-400/10 dark:text-rose-300",
  "Mandatory under condition": "bg-teal-100 text-teal-800 dark:bg-teal-400/10 dark:text-teal-300",
  "Requires component": "bg-indigo-100 text-indigo-800 dark:bg-indigo-400/10 dark:text-indigo-300",
};

export function KindBadge({ kind }: { kind: ConstraintKind }) {
  return (
    <span className={`inline-flex shrink-0 items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${KIND_STYLE[kind]}`}>
      {kind}
    </span>
  );
}

export function Segmented<T extends string>({
  value,
  options,
  onChange,
  label,
}: {
  value: T;
  options: { value: T; label: ReactNode; hint?: string }[];
  onChange: (value: T) => void;
  label: string;
}) {
  return (
    <div role="radiogroup" aria-label={label} className="flex flex-wrap gap-1 rounded-xl bg-stone-100 p-1 dark:bg-stone-800/70">
      {options.map((o) => {
        const active = o.value === value;
        return (
          <button
            key={o.value}
            role="radio"
            aria-checked={active}
            onClick={() => onChange(o.value)}
            className={`flex min-w-0 flex-1 flex-col items-center rounded-lg px-2.5 py-1.5 text-xs font-medium transition ${
              active
                ? "bg-white text-stone-900 shadow-sm dark:bg-stone-950 dark:text-stone-50"
                : "text-stone-500 hover:text-stone-900 dark:text-stone-400 dark:hover:text-stone-100"
            }`}
          >
            <span className="truncate">{o.label}</span>
            {o.hint && <span className="text-[10px] font-normal text-stone-400">{o.hint}</span>}
          </button>
        );
      })}
    </div>
  );
}

export function Toggle({
  checked,
  onChange,
  label,
  disabled = false,
}: {
  checked: boolean;
  onChange: (checked: boolean) => void;
  label: ReactNode;
  disabled?: boolean;
}) {
  return (
    <label className={`flex items-center gap-3 ${disabled ? "cursor-not-allowed opacity-60" : "cursor-pointer"}`}>
      <button
        type="button"
        role="switch"
        aria-checked={checked}
        disabled={disabled}
        onClick={() => onChange(!checked)}
        className={`relative h-6 w-11 shrink-0 rounded-full transition ${
          checked ? "bg-teal-600 dark:bg-teal-500" : "bg-stone-300 dark:bg-stone-700"
        }`}
      >
        <span
          className={`absolute top-0.5 left-0.5 size-5 rounded-full bg-white shadow transition-transform ${
            checked ? "translate-x-5" : ""
          }`}
        />
      </button>
      <span className="text-sm">{label}</span>
    </label>
  );
}

export function Field({ label, hint, children }: { label: string; hint?: ReactNode; children: ReactNode }) {
  return (
    <div>
      <div className="mb-2 flex items-baseline justify-between gap-2">
        <span className="text-sm font-medium">{label}</span>
        {hint && <span className="font-mono text-xs text-stone-400">{hint}</span>}
      </div>
      {children}
    </div>
  );
}

export const inputClass =
  "w-full rounded-lg border border-stone-200 bg-white px-3 py-2 text-sm outline-none transition focus:border-teal-500 focus:ring-2 focus:ring-teal-500/20 dark:border-stone-700 dark:bg-stone-950";

export function Stepper({
  value,
  onChange,
  min,
  max,
  label,
}: {
  value: number;
  onChange: (value: number) => void;
  min: number;
  max: number;
  label: string;
}) {
  const btn =
    "grid size-8 place-items-center rounded-lg text-lg text-stone-600 transition hover:bg-stone-100 disabled:opacity-30 dark:text-stone-300 dark:hover:bg-stone-800";
  return (
    <div className="flex items-center justify-between rounded-lg border border-stone-200 p-1 dark:border-stone-700">
      <button className={btn} onClick={() => onChange(value - 1)} disabled={value <= min} aria-label={`Decrease ${label}`}>
        −
      </button>
      <span className="font-mono text-sm tabular-nums">{value}</span>
      <button className={btn} onClick={() => onChange(value + 1)} disabled={value >= max} aria-label={`Increase ${label}`}>
        +
      </button>
    </div>
  );
}

export function StatusIcon({ status }: { status: "pass" | "fail" | "skip" }) {
  if (status === "pass") {
    return (
      <span className="grid size-5 shrink-0 place-items-center rounded-full bg-emerald-500 text-white" aria-label="passes">
        <svg viewBox="0 0 16 16" className="size-3" fill="none" stroke="currentColor" strokeWidth={2.5}>
          <path d="M3.5 8.5l3 3 6-7" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </span>
    );
  }
  if (status === "fail") {
    return (
      <span className="grid size-5 shrink-0 place-items-center rounded-full bg-rose-500 text-white" aria-label="fails">
        <svg viewBox="0 0 16 16" className="size-3" fill="none" stroke="currentColor" strokeWidth={2.5}>
          <path d="M4.5 4.5l7 7m0-7l-7 7" strokeLinecap="round" />
        </svg>
      </span>
    );
  }
  return (
    <span
      className="grid size-5 shrink-0 place-items-center rounded-full border-2 border-dashed border-stone-300 dark:border-stone-600"
      aria-label="not applicable"
    />
  );
}
