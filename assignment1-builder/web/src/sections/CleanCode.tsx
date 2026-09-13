import { CodeBlock } from "../components/Code";
import { Section } from "../components/ui";

const BEFORE = `// does two things
private void checkRoom() {
    if (p.children() > 0 && p.room() != RoomType.FAMILY) {
        errors.add("Children require a FAMILY room");
    }
    if (p.guests() > p.room().capacity()) {
        errors.add("...");
    }
}`;

const AFTER = `private void checkFamilyRoomForChildren() {
    if (trip.children() > 0 && trip.room() != RoomType.FAMILY) {
        errors.add("Children require a FAMILY room");
    }
}

private void checkGuestsFitRoom() {
    RoomType room = trip.room();
    if (trip.guests() > room.capacity()) {
        errors.add("%s room fits %d guests, got %d".formatted(
                room, room.capacity(), trip.guests()));
    }
}`;

const VALIDATE = `void validate() {
    checkNightsInRange();
    checkHotelIsChosen();
    checkStarsMatchBudget();
    checkExactlyOneTransport();
    checkVisaForInternationalTrip();
    checkFamilyRoomForChildren();
    checkGuestsFitRoom();
    checkFlightForAirportTransfer();
    if (!errors.isEmpty()) {
        throw new InvalidTravelPackageException(errors);
    }
}`;

const PRINCIPLES = [
  { title: "Small", text: "Each rule is a 3–8 line method." },
  { title: "Do one thing", text: "One rule per method. validate() only calls them." },
  { title: "One level of abstraction", text: "validate() reads like a list of requirements." },
  { title: "Descriptive names", text: "checkVisaForInternationalTrip says exactly what it checks." },
  { title: "Few arguments", text: "Rule methods take 0 arguments, builder steps 0–2." },
  { title: "No flag arguments", text: "visaSupport() instead of visaSupport(true)." },
  { title: "Exceptions, not error codes", text: "InvalidTravelPackageException carries all errors." },
  { title: "No duplication", text: "Star ranges and capacities are defined once, in enums." },
];

export function CleanCode() {
  return (
    <Section
      id="clean-code"
      eyebrow="05 · Clean Code, Chapter 3"
      title="Functions that do one thing."
      intro="The validator is where Chapter 3 shows most clearly: small functions, one job each, names that read like the requirements."
    >
      <ul className="grid gap-px overflow-hidden rounded-2xl border border-stone-200 bg-stone-200 sm:grid-cols-2 lg:grid-cols-4 dark:border-stone-800 dark:bg-stone-800">
        {PRINCIPLES.map((p) => (
          <li key={p.title} className="bg-white p-5 dark:bg-stone-900">
            <p className="font-semibold">{p.title}</p>
            <p className="mt-1 text-sm leading-relaxed text-stone-600 dark:text-stone-400">{p.text}</p>
          </li>
        ))}
      </ul>

      <div className="mt-10 grid gap-6 lg:grid-cols-2">
        <div className="min-w-0">
          <p className="mb-2 text-sm font-medium text-rose-700 dark:text-rose-400">Before · two jobs, one-letter name</p>
          <CodeBlock code={BEFORE} />
        </div>
        <div className="min-w-0 lg:row-span-2">
          <p className="mb-2 text-sm font-medium text-emerald-700 dark:text-emerald-400">After · one rule per method</p>
          <CodeBlock code={AFTER} />
        </div>
        <div className="min-w-0">
          <p className="mb-2 text-sm font-medium text-stone-600 dark:text-stone-400">One level of abstraction</p>
          <CodeBlock code={VALIDATE} />
        </div>
      </div>
    </Section>
  );
}
