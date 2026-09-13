import { CodeBlock } from "../components/Code";
import { Section } from "../components/ui";
import { UmlDiagram } from "../components/UmlDiagram";

const FLOW = [
  { step: "1", title: "Steps", text: "Client calls .nights(), .hotel()… in any order. The builder only stores values." },
  { step: "2", title: "build()", text: "Creates the object through the private constructor new TravelPackage(this)." },
  { step: "3", title: "validate()", text: "The validator runs every rule and collects all errors." },
  { step: "4", title: "Result", text: "Returns an immutable package, or throws one exception listing every error." },
];

const ROLES = [
  ["TravelPackage", "The product. Final fields, no setters, private constructor."],
  ["TravelPackage.Builder", "Fluent steps and build(). The only way to create a package."],
  ["TravelPackageValidator", "Business rules. One rule per method."],
  ["TravelPresets", "allInclusiveBeach() returns a pre-filled Builder."],
  ["BudgetLevel, RoomType", "Enums that own the limits: stars range, room capacity."],
  ["Hotel, Flight, Train, Location", "Records that reject bad values on creation."],
];

const BUILD = `public TravelPackage build() {
    TravelPackage result = new TravelPackage(this);
    // throws InvalidTravelPackageException if any rule fails
    new TravelPackageValidator(result).validate();
    return result;
}`;

export function Design() {
  return (
    <Section
      id="design"
      eyebrow="03 · Design"
      title="One way in, one place for rules."
      intro="The constructor is private, so build() is the only way to get a TravelPackage — and build() always runs the validator."
    >
      <ol className="grid gap-px overflow-hidden rounded-2xl border border-stone-200 bg-stone-200 sm:grid-cols-2 lg:grid-cols-4 dark:border-stone-800 dark:bg-stone-800">
        {FLOW.map((f) => (
          <li key={f.step} className="bg-white p-5 dark:bg-stone-900">
            <span className="font-mono text-xs text-teal-700 dark:text-teal-400">{f.step.padStart(2, "0")}</span>
            <p className="mt-1 font-mono text-sm font-semibold">{f.title}</p>
            <p className="mt-2 text-sm leading-relaxed text-stone-600 dark:text-stone-400">{f.text}</p>
          </li>
        ))}
      </ol>

      <div className="mt-10">
        <UmlDiagram />
      </div>

      <div className="mt-10 grid gap-6 lg:grid-cols-2">
        <dl className="divide-y divide-stone-200 dark:divide-stone-800">
          {ROLES.map(([name, role]) => (
            <div key={name} className="grid gap-1 py-3 sm:grid-cols-[200px_1fr] sm:gap-4">
              <dt className="font-mono text-sm font-medium">{name}</dt>
              <dd className="text-sm text-stone-600 dark:text-stone-400">{role}</dd>
            </div>
          ))}
        </dl>
        <div className="space-y-4">
          <CodeBlock title="TravelPackage.Builder" code={BUILD} />
          <div className="grid gap-3 sm:grid-cols-2">
            <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 dark:border-amber-400/20 dark:bg-amber-400/5">
              <p className="text-sm font-semibold text-amber-900 dark:text-amber-200">Level 1 · in the step</p>
              <p className="mt-1 text-sm text-amber-800 dark:text-amber-300/90">
                Wrong on its own: blank id, 6 stars, 0 adults. Thrown immediately.
              </p>
            </div>
            <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 dark:border-rose-400/20 dark:bg-rose-400/5">
              <p className="text-sm font-semibold text-rose-900 dark:text-rose-200">Level 2 · in build()</p>
              <p className="mt-1 text-sm text-rose-800 dark:text-rose-300/90">
                Wrong in combination: 5★ with ECONOMY. All errors reported together.
              </p>
            </div>
          </div>
        </div>
      </div>
    </Section>
  );
}
