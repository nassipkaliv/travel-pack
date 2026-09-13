// Hand-drawn UML class diagram of the Java code. Scales with its container.

const HEADER = 34;
const HEADER_STEREO = 46;
const LINE = 17;
const PAD = 14;

interface BoxSpec {
  x: number;
  y: number;
  w: number;
  title: string;
  stereotype?: string;
  sections: string[][];
  accent?: boolean;
}

const sectionHeight = (lines: string[]) => lines.length * LINE + PAD;
const boxHeight = (b: BoxSpec) =>
  (b.stereotype ? HEADER_STEREO : HEADER) + b.sections.reduce((sum, s) => sum + sectionHeight(s), 0);

const BOXES = {
  presets: {
    x: 30, y: 112, w: 290,
    title: "TravelPresets", stereotype: "utility",
    sections: [["+ allInclusiveBeach(id, from, to) : Builder"]],
  },
  builder: {
    x: 400, y: 30, w: 300, accent: true,
    title: "TravelPackage.Builder",
    sections: [[
      "+ nights(int) : Builder",
      "+ budget(BudgetLevel) : Builder",
      "+ hotel(name, stars) : Builder",
      "+ room(RoomType) : Builder",
      "+ meals(MealPlan) : Builder",
      "+ travelers(adults, children) : Builder",
      "+ flight(number) : Builder",
      "+ train(number) : Builder",
      "+ airportTransfer() : Builder",
      "+ visaSupport() : Builder",
      "+ build() : TravelPackage",
    ]],
  },
  product: {
    x: 790, y: 30, w: 300,
    title: "TravelPackage", stereotype: "immutable",
    sections: [
      ["- id, from, to, nights", "- budget, hotel, room, meals", "- adults, children", "- flight, train", "- airportTransfer, visaSupport"],
      ["+ builder(id, from, to) : Builder", "+ isInternational() : boolean"],
    ],
  },
  validator: {
    x: 400, y: 345, w: 300,
    title: "TravelPackageValidator",
    sections: [
      ["~ validate() : void"],
      [
        "- checkNightsInRange()",
        "- checkHotelIsChosen()",
        "- checkStarsMatchBudget()",
        "- checkExactlyOneTransport()",
        "- checkVisaForInternationalTrip()",
        "- checkFamilyRoomForChildren()",
        "- checkGuestsFitRoom()",
        "- checkFlightForAirportTransfer()",
      ],
    ],
  },
  exception: {
    x: 30, y: 428, w: 290,
    title: "InvalidTravelPackageException",
    sections: [["+ errors() : List<String>"]],
  },
  budget: { x: 790, y: 290, w: 145, title: "BudgetLevel", stereotype: "enum", sections: [["ECONOMY(1, 3)", "STANDARD(3, 4)", "PREMIUM(4, 5)"]] },
  room: { x: 945, y: 290, w: 145, title: "RoomType", stereotype: "enum", sections: [["STANDARD(2)", "SUITE(3)", "FAMILY(4)"]] },
  meals: { x: 790, y: 420, w: 145, title: "MealPlan", stereotype: "enum", sections: [["ROOM_ONLY", "BREAKFAST", "HALF_BOARD", "ALL_INCLUSIVE"]] },
  hotel: { x: 945, y: 420, w: 145, title: "Hotel", stereotype: "record", sections: [["name : String", "stars : int"]] },
  location: { x: 790, y: 565, w: 145, title: "Location", stereotype: "record", sections: [["city : String", "country : String"]] },
  flight: { x: 945, y: 530, w: 145, title: "Flight", stereotype: "record", sections: [["number : String"]] },
  train: { x: 945, y: 625, w: 145, title: "Train", stereotype: "record", sections: [["number : String"]] },
} satisfies Record<string, BoxSpec>;

function Box({ spec }: { spec: BoxSpec }) {
  const h = boxHeight(spec);
  const headerH = spec.stereotype ? HEADER_STEREO : HEADER;
  let cursor = spec.y + headerH;

  return (
    <g>
      <rect
        x={spec.x} y={spec.y} width={spec.w} height={h} rx={10}
        className={
          spec.accent
            ? "fill-teal-50 stroke-teal-500 dark:fill-teal-950 dark:stroke-teal-400"
            : "fill-white stroke-stone-300 dark:fill-stone-900 dark:stroke-stone-700"
        }
        strokeWidth={spec.accent ? 1.75 : 1.25}
      />
      {spec.stereotype && (
        <text x={spec.x + spec.w / 2} y={spec.y + 17} textAnchor="middle" className="fill-stone-400 font-mono text-[10px]">
          «{spec.stereotype}»
        </text>
      )}
      <text
        x={spec.x + spec.w / 2}
        y={spec.y + (spec.stereotype ? 35 : 22)}
        textAnchor="middle"
        className="fill-stone-900 text-[13.5px] font-semibold dark:fill-stone-100"
      >
        {spec.title}
      </text>
      {spec.sections.map((lines, si) => {
        const top = cursor;
        cursor += sectionHeight(lines);
        return (
          <g key={si}>
            <line
              x1={spec.x} x2={spec.x + spec.w} y1={top} y2={top}
              className={spec.accent ? "stroke-teal-300 dark:stroke-teal-700" : "stroke-stone-200 dark:stroke-stone-800"}
            />
            {lines.map((line, li) => (
              <text
                key={li}
                x={spec.x + 12}
                y={top + 7 + LINE * (li + 1) - 4}
                className="fill-stone-600 font-mono text-[11.5px] dark:fill-stone-300"
              >
                {line}
              </text>
            ))}
          </g>
        );
      })}
    </g>
  );
}

function Label({ x, y, children, anchor = "middle" }: { x: number; y: number; children: string; anchor?: "start" | "middle" | "end" }) {
  return (
    <text x={x} y={y} textAnchor={anchor} className="fill-stone-500 text-[11px] font-medium dark:fill-stone-400">
      {children}
    </text>
  );
}

export function UmlDiagram() {
  const b = BOXES;
  const bottom = (s: BoxSpec) => s.y + boxHeight(s);
  const builderBottom = bottom(b.builder);
  const productBottom = bottom(b.product);
  const validatorMidY = b.validator.y + boxHeight(b.validator) / 2;
  const presetsMidY = b.presets.y + boxHeight(b.presets) / 2;
  const groupTop = 262;

  return (
    <div className="overflow-x-auto rounded-2xl border border-stone-200 bg-stone-100/60 p-3 dark:border-stone-800 dark:bg-stone-900/40">
      <svg
        viewBox="0 0 1120 740"
        role="img"
        aria-label="UML class diagram of the travel package builder"
        className="h-auto w-full min-w-[760px] text-stone-500 dark:text-stone-400"
      >
        <defs>
          <marker id="open-arrow" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="9" markerHeight="9" orient="auto-start-reverse">
            <path d="M1 1L9 5L1 9" fill="none" stroke="currentColor" strokeWidth="1.5" />
          </marker>
          <marker id="diamond" viewBox="0 0 16 10" refX="1" refY="5" markerWidth="16" markerHeight="10" orient="auto-start-reverse">
            <path d="M1 5L8 1L15 5L8 9Z" fill="currentColor" />
          </marker>
        </defs>

        {/* value types group */}
        <rect
          x={775} y={groupTop} width={330} height={458} rx={14}
          fill="none" strokeDasharray="5 5"
          className="stroke-stone-300 dark:stroke-stone-700"
        />
        <Label x={790} y={groupTop + 18} anchor="start">value types</Label>

        {/* Presets --returns--> Builder */}
        <line x1={320} y1={presetsMidY} x2={398} y2={presetsMidY} stroke="currentColor" strokeDasharray="5 4" markerEnd="url(#open-arrow)" />
        <Label x={359} y={presetsMidY - 8}>returns</Label>

        {/* Builder nested in TravelPackage (circle-plus at the outer class) */}
        <line x1={700} y1={70} x2={780} y2={70} stroke="currentColor" />
        <circle cx={783} cy={70} r={7} className="fill-white dark:fill-stone-900" stroke="currentColor" />
        <path d="M779 70h8M783 66v8" stroke="currentColor" />
        <Label x={740} y={62}>nested</Label>

        {/* Builder --creates--> TravelPackage */}
        <line x1={700} y1={150} x2={788} y2={150} stroke="currentColor" strokeDasharray="5 4" markerEnd="url(#open-arrow)" />
        <Label x={744} y={142}>creates</Label>

        {/* Builder --validates with--> Validator */}
        <line x1={550} y1={builderBottom} x2={550} y2={b.validator.y - 2} stroke="currentColor" strokeDasharray="5 4" markerEnd="url(#open-arrow)" />
        <Label x={560} y={(builderBottom + b.validator.y) / 2 + 4} anchor="start">validates with</Label>

        {/* Validator --throws--> Exception */}
        <line x1={400} y1={validatorMidY} x2={322} y2={validatorMidY} stroke="currentColor" strokeDasharray="5 4" markerEnd="url(#open-arrow)" />
        <Label x={361} y={validatorMidY - 8}>throws</Label>

        {/* TravelPackage ◆— value types */}
        <line x1={940} y1={productBottom} x2={940} y2={groupTop} stroke="currentColor" markerStart="url(#diamond)" />
        <Label x={950} y={(productBottom + groupTop) / 2 + 4} anchor="start">has</Label>

        {Object.entries(b).map(([key, spec]) => (
          <Box key={key} spec={spec} />
        ))}
      </svg>
    </div>
  );
}
