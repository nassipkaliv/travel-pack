import { CodeBlock } from "../components/Code";

const TREE = `assignment1-builder/
├── pom.xml, mvnw
├── src/main/java/kz/aitu/sdp/travel/
│   ├── TravelPackage.java            product + Builder
│   ├── TravelPackageValidator.java   8 rules
│   ├── TravelPresets.java            allInclusiveBeach
│   ├── InvalidTravelPackageException.java
│   ├── BudgetLevel, RoomType, MealPlan
│   ├── Hotel, Flight, Train, Location
│   └── App.java                      demo
├── src/test/java/.../TravelPackageTest.java
├── docs/REPORT.md, docs/uml/
└── web/                              this site`;

export function Footer() {
  return (
    <footer id="run" className="border-t border-stone-200 bg-stone-100/60 dark:border-stone-800 dark:bg-stone-900/40">
      <div className="mx-auto grid max-w-6xl gap-10 px-4 py-20 sm:px-6 lg:grid-cols-2">
        <div>
          <p className="font-mono text-xs font-medium tracking-[0.2em] text-teal-700 uppercase dark:text-teal-400">
            08 · Run it
          </p>
          <h2 className="mt-3 text-3xl font-semibold tracking-tight">Java 17+, no Maven install needed.</h2>
          <div className="mt-8 space-y-6">
            <div>
              <p className="mb-2 text-sm font-medium">Java</p>
              <pre className="overflow-x-auto rounded-xl bg-stone-950 p-4 font-mono text-[13px] leading-6 text-stone-200">
                {`./mvnw test
./mvnw package && java -cp target/classes kz.aitu.sdp.travel.App`}
              </pre>
            </div>
            <div>
              <p className="mb-2 text-sm font-medium">This site</p>
              <pre className="overflow-x-auto rounded-xl bg-stone-950 p-4 font-mono text-[13px] leading-6 text-stone-200">
                {`cd web
npm install
npm run dev`}
              </pre>
            </div>
          </div>
        </div>
        <div className="min-w-0">
          <CodeBlock title="project structure" code={TREE} plain />
        </div>
      </div>
      <div className="border-t border-stone-200 dark:border-stone-800">
        <p className="mx-auto max-w-6xl px-4 py-6 text-sm text-stone-500 sm:px-6 dark:text-stone-400">
          Assignment 1 · Builder Pattern: Design Under Changing Requirements · Software Design Patterns
        </p>
      </div>
    </footer>
  );
}
