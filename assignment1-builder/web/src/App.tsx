import { useEffect, useState } from "react";
import { EXAMPLES } from "./domain/travel";
import { Changes } from "./sections/Changes";
import { CleanCode } from "./sections/CleanCode";
import { Design } from "./sections/Design";
import { Footer } from "./sections/Footer";
import { Hero } from "./sections/Hero";
import { Playground, type PlaygroundState } from "./sections/Playground";
import { Problem } from "./sections/Problem";
import { Rules } from "./sections/Rules";
import { Tests } from "./sections/Tests";

const NAV = [
  ["problem", "Problem"],
  ["playground", "Playground"],
  ["design", "Design"],
  ["rules", "Rules"],
  ["clean-code", "Clean Code"],
  ["tests", "Tests"],
  ["changes", "Changes"],
  ["run", "Run"],
] as const;

function useActiveSection() {
  const [active, setActive] = useState<string>("");
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        const visible = entries.filter((e) => e.isIntersecting);
        if (visible.length > 0) setActive(visible[0].target.id);
      },
      { rootMargin: "-40% 0px -55% 0px" },
    );
    NAV.forEach(([id]) => {
      const el = document.getElementById(id);
      if (el) observer.observe(el);
    });
    return () => observer.disconnect();
  }, []);
  return active;
}

export default function App() {
  const [playground, setPlayground] = useState<PlaygroundState>({ mode: EXAMPLES[0].mode, draft: EXAMPLES[0].draft });
  const [withNewRule, setWithNewRule] = useState(false);
  const active = useActiveSection();

  const tryNewRule = () => {
    const beach = EXAMPLES[1];
    setWithNewRule(true);
    setPlayground({
      mode: "builder",
      draft: { ...beach.draft, budget: "STANDARD", hotel: { name: "City Inn", stars: 3 } },
    });
    document.getElementById("playground")?.scrollIntoView();
  };

  return (
    <>
      <nav className="sticky top-0 z-20 border-b border-stone-200/80 bg-stone-50/80 backdrop-blur-lg dark:border-stone-800/80 dark:bg-stone-950/80">
        <div className="mx-auto flex h-14 max-w-6xl items-center gap-6 px-4 sm:px-6">
          <a href="#" className="flex shrink-0 items-center gap-2 font-semibold tracking-tight">
            <span className="grid size-7 place-items-center rounded-lg bg-teal-700 font-mono text-xs text-white dark:bg-teal-500 dark:text-teal-950">
              B
            </span>
            <span className="hidden sm:inline">Travel Package Builder</span>
          </a>
          <div className="-mr-4 flex min-w-0 flex-1 overflow-x-auto pr-4 [scrollbar-width:none] sm:mr-0 sm:pr-0">
            <div className="ml-auto flex gap-1">
            {NAV.map(([id, label]) => (
              <a
                key={id}
                href={`#${id}`}
                className={`shrink-0 rounded-full px-3 py-1.5 text-sm transition ${
                  active === id
                    ? "bg-stone-900 text-white dark:bg-stone-100 dark:text-stone-900"
                    : "text-stone-600 hover:text-stone-900 dark:text-stone-400 dark:hover:text-stone-100"
                }`}
              >
                {label}
              </a>
            ))}
            </div>
          </div>
        </div>
      </nav>

      <Hero />
      <main>
        <Problem />
        <div className="border-y border-stone-200 bg-white/60 dark:border-stone-800 dark:bg-stone-900/30">
          <Playground state={playground} onChange={setPlayground} withNewRule={withNewRule} />
        </div>
        <Design />
        <div className="border-y border-stone-200 bg-white/60 dark:border-stone-800 dark:bg-stone-900/30">
          <Rules />
        </div>
        <CleanCode />
        <div className="border-y border-stone-200 bg-white/60 dark:border-stone-800 dark:bg-stone-900/30">
          <Tests />
        </div>
        <Changes withNewRule={withNewRule} onToggle={setWithNewRule} onTry={tryNewRule} />
      </main>
      <Footer />
    </>
  );
}
