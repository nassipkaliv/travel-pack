# Defense guide — Assignment 5, Strategy Pattern

Lines in quotes are what you say, the rest is what you open or run.

## 0. Setup

```bash
cd ~/projects/aitu_sdp/assignment5-strategy
export JAVA_HOME=/opt/homebrew/opt/openjdk
./mvnw test                                           # 23 tests, BUILD SUCCESS
./mvnw package -DskipTests && java -cp target/classes kz.aitu.sdp.route.RouteDemo
```

## 1. What the project is (30 seconds)

> "It is a route optimization engine for a logistics company. There are eight candidate routes
> from the hub to the Expo district, and there is no single best one: urgent orders want time,
> economy orders want price, eco orders want low CO2. Each way of choosing is a separate
> strategy behind the `RouteStrategy` interface, `RoutePlanner` is the context that delegates to
> it, and `StrategySelector` decides which strategy a request gets."

## 2. Show the three roles

| Open | Say |
|---|---|
| `planner/RoutePlanner.java` | "The context. One field of the interface type, `setStrategy`, and `plan` just delegates. No `instanceof`, no switch." |
| `strategy/RouteStrategy.java` | "The strategy. I return `RouteDecision` instead of `Route`, because the result must explain itself." |
| `strategy/ScoredRouteStrategy.java` | "The shared steps: screen with hard constraints, score, take the lowest. A rejected route is never scored, so it cannot win on score." |
| `strategy/FastestRouteStrategy.java` | "A concrete strategy writes only the scoring — here time plus traffic penalty." |
| `planner/StrategySelector.java` | "Strategy chooses **how** to solve the problem. The selector decides **which** strategy is used. Different responsibilities, different classes." |
| `RoutingConfig.java` | "The only class that knows the concrete strategies. The rules are a list of condition → strategy, not a switch." |

## 3. Questions you will likely get

**"Why not if/else?"** — Every algorithm would live in one method; adding one means editing it,
and the planner would know every formula. Here each algorithm is a class you can test, configure
and swap at runtime.

**"Where is runtime switching?"** — `RouteDemo.switchStrategiesAtRuntime()`: one planner, three
`setStrategy` calls, four different routes (R2, R5, R6, R7).

**"Why is the cost formula in `Tariff` and not in Cheapest?"** — The budget constraint must use
the same price as the strategy. The price is a business rule; *ranking by* price is the strategy.

**"Why is the eco score in the Eco strategy?"** — It is an opinion of what a good route is, not a
fact about the route. Balanced weighs CO2 differently, Cheapest ignores it.

**"What if no route fits?"** — `NoSuitableRouteException` with every rejected route and its
reasons. Run scenario 4 to show it. Never `null`.

**"How many classes did the fifth strategy change?"** — `git log --stat --grep="fifth strategy"`: new
`PremiumRouteStrategy` and its test; `RoutingConfig` got one constant, one rule and one list
entry; the demo got a request and a scenario. Planner, interface, selector, base class and the
other strategies are untouched.

**"Strategy vs Bridge?"** — Same mechanics (field + setter), different intent. Bridge in
assignment 3 split two growing hierarchies — notification types × providers — to avoid N × M
classes. Here there is one context and a family of interchangeable algorithms chosen per request.

**"SOLID?"** — SRP: planner / strategies / selector / constraints / tariff each do one thing.
OCP: Premium added without edits. DIP: planner depends on `RouteStrategy` only.

## 4. Live modification recipes

**Add a new strategy** (e.g. shortest distance) — new file in `strategy/`:

```java
public class DistanceFirstStrategy extends ScoredRouteStrategy {
    @Override
    public String getStrategyName() {
        return "Shortest";
    }

    @Override
    protected RouteScorer scorer(List<Route> candidates, DeliveryRequest request) {
        return route -> new RouteScore(route.distanceKm(), List.of("Distance: " + route.distanceKm() + " km"));
    }
}
```

Then in `RoutingConfig`: add a constant and put it into `strategies()`. Run the demo — it
appears in the comparison table. Nothing else changes.

**Add a selection rule** — one line in `RoutingConfig.selector()`, before the rule it must win over:

```java
.when("fragile cargo", request -> request.cargo() == CargoType.FRAGILE, BALANCED)
```

**Add a hard constraint for every strategy** — one constant in `HardConstraints` and add it to
`standard()`:

```java
public static final RouteConstraint NO_HEAVY_TRAFFIC = (route, request) ->
        route.traffic() != TrafficLevel.HEAVY ? Optional.empty() : Optional.of("heavy traffic");
```

**Add a constraint for one strategy only** — pass it in the constructor, like Premium:
`super(HardConstraints.standard().with(MY_CONSTRAINT));`

**Change Balanced weights** — `RoutingConfig.BALANCED = new BalancedRouteStrategy(0.10, 0.60, 0.10, 0.20)`.
Show that `new BalancedRouteStrategy(0.5, 0.5, 0.2, 0)` throws `IllegalArgumentException`.

**Add a new route** — one `route(...)` line in `DemoData.routes()`. If it beats an existing
winner, `PlannerTest` will name the test that changed — that is expected.

**Change the price formula** — only `Tariff.price()`; the budget constraint and every strategy see
the new price at once.
