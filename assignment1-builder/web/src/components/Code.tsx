import { useState, type ReactNode } from "react";

const TOKEN =
  /(\/\/.*$|\/\*[\s\S]*?\*\/)|("(?:[^"\\]|\\.)*")|(@\w+)|\b(public|private|protected|final|static|class|return|new|if|else|void|throw|throws|this|boolean|int|enum|record|import|package|null|true|false|var)\b|\b([A-Z][A-Za-z0-9_]*)\b|\b(\d+)\b|([a-z]\w*)(?=\()/gm;

const CLASS_BY_GROUP = ["", "tok-com", "tok-str", "tok-ann", "tok-kw", "tok-type", "tok-num", "tok-call"];

export function highlight(code: string): ReactNode[] {
  const out: ReactNode[] = [];
  let last = 0;
  for (const match of code.matchAll(TOKEN)) {
    const index = match.index ?? 0;
    if (index > last) out.push(code.slice(last, index));
    const group = match.findIndex((g, i) => i > 0 && g !== undefined);
    out.push(
      <span key={index} className={CLASS_BY_GROUP[group]}>
        {match[0]}
      </span>,
    );
    last = index + match[0].length;
  }
  if (last < code.length) out.push(code.slice(last));
  return out;
}

interface CodeBlockProps {
  code: string;
  title?: string;
  className?: string;
  /** Lines starting with "+" or "-" are rendered as a diff. */
  diff?: boolean;
  /** No syntax highlighting (for trees, terminal output). */
  plain?: boolean;
}

export function CodeBlock({ code, title, className = "", diff = false, plain = false }: CodeBlockProps) {
  const [copied, setCopied] = useState(false);

  const copy = async () => {
    const plain = diff ? code.split("\n").map((l) => l.replace(/^[+-] ?/, "")).join("\n") : code;
    try {
      await navigator.clipboard.writeText(plain);
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    } catch {
      /* clipboard can be blocked; copying is a convenience only */
    }
  };

  return (
    <div
      className={`overflow-hidden rounded-2xl border border-stone-200 bg-white shadow-sm dark:border-stone-800 dark:bg-stone-900 ${className}`}
    >
      {title && (
        <div className="flex items-center justify-between border-b border-stone-200 px-4 py-2.5 dark:border-stone-800">
          <div className="flex items-center gap-2">
            <span className="flex gap-1.5" aria-hidden>
              <span className="size-2.5 rounded-full bg-stone-300 dark:bg-stone-700" />
              <span className="size-2.5 rounded-full bg-stone-300 dark:bg-stone-700" />
              <span className="size-2.5 rounded-full bg-stone-300 dark:bg-stone-700" />
            </span>
            <span className="font-mono text-xs text-stone-500 dark:text-stone-400">{title}</span>
          </div>
          <button
            onClick={copy}
            className="rounded-md px-2 py-1 text-xs font-medium text-stone-500 transition hover:bg-stone-100 hover:text-stone-900 dark:text-stone-400 dark:hover:bg-stone-800 dark:hover:text-stone-100"
          >
            {copied ? "Copied" : "Copy"}
          </button>
        </div>
      )}
      <pre className="overflow-x-auto p-4 font-mono text-[13px] leading-6">
        {diff ? (
          code.split("\n").map((line, i) => {
            const added = line.startsWith("+");
            const removed = line.startsWith("-");
            const body = added || removed ? line.slice(1) : line;
            return (
              <div
                key={i}
                className={`-mx-4 px-4 ${
                  added
                    ? "bg-emerald-50 dark:bg-emerald-400/10"
                    : removed
                      ? "bg-rose-50 dark:bg-rose-400/10"
                      : ""
                }`}
              >
                <span className="mr-3 inline-block w-3 select-none text-stone-400">
                  {added ? "+" : removed ? "−" : " "}
                </span>
                {highlight(body)}
              </div>
            );
          })
        ) : (
          <code>{plain ? code : highlight(code)}</code>
        )}
      </pre>
    </div>
  );
}
