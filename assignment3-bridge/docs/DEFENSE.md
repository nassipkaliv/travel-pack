# Defense guide — Assignment 3, Bridge Pattern

Read top to bottom. Lines in quotes are what you say, the rest is what you open or run.

---

## 0. Setup before you start

```bash
cd ~/projects/aitu_sdp/assignment3-bridge
export JAVA_HOME=/opt/homebrew/opt/openjdk
./mvnw package -DskipTests
```

Keep two things ready: a terminal in this folder and the editor with the project open.

---

## 1. What the project is (30 seconds)

> "The project is a **Multi-Channel Notification Gateway**. It sends different **types of
> notification** through different **delivery providers**.
>
> There are two independent dimensions. *What* is sent: urgent, encrypted, scheduled batch
> and marketing notifications. *How* it travels: Telegram, Email, SMS, Kafka and Push.
>
> Without Bridge that is four times five — twenty classes like `UrgentTelegramNotification`.
> With Bridge it is four plus five — **nine classes**, because a notification **has a**
> provider instead of **being** one."

| Abstraction — what is sent | Implementor — how it travels |
|---|---|
| `UrgentNotification` | `TelegramMessageSender` — Markdown title |
| `EncryptedNotification` | `EmailMessageSender` — subject and signature |
| `ScheduledBatchNotification` | `SmsMessageSender` — max 160 characters |
| `MarketingNotification` *(added later)* | `KafkaEventSender` — publishes an event |
| | `PushNotificationSender` *(added later)* — 120-character banner |

---

## 2. Where exactly the Bridge is

**Open** `src/main/java/kz/aitu/sdp/notify/notification/Notification.java`

```java
public abstract class Notification {
    protected MessageSender sender;      // <-- this field IS the bridge
    public void setSender(MessageSender sender) { this.sender = sender; }
    public abstract void send(String recipient, String message);
}
```

> "The bridge is this field. The abstraction keeps a reference to the implementor interface
> and talks to it through three methods only: `sendMessage`, `isAvailable` and
> `getChannelName`. Because it is a field and not a superclass, `setSender` can replace the
> provider while the program is running."

---

## 3. How one message travels through the code

**Open** `notification/UrgentNotification.java` and `sender/TelegramMessageSender.java`

> "Here is the whole flow. The abstraction decides **what** is sent: `UrgentNotification`
> adds the `[URGENT]` tag and checks whether the provider is available. Then it calls
> `sender.sendMessage(recipient, title, body)` — and it has no idea which class that is.
>
> The provider decides **how** the message looks. Telegram renders a bold Markdown title,
> Email adds a subject and a signature, SMS produces plain text and refuses anything longer
> than 160 characters, Kafka builds an event payload.
>
> So the `[URGENT]` tag comes from the notification, because it belongs to the meaning of
> the message, and the signature comes from the provider, because it belongs to the channel."

---

## 4. Live demo — the six required scenarios

```bash
java -cp target/classes kz.aitu.sdp.notify.GatewayDemo
```

What to say while the output scrolls:

**Scenarios 1–3:**
> "Urgent over Telegram, encrypted over Email — the demo decrypts the payload back to prove
> the plain text never travelled — and a batch over SMS with the required summary:
> total, successful, failed."

**Scenario 4 — runtime switching:**
> "This is the same `UrgentNotification` object. I call `send`, then `setSender(email)`, then
> `send` again. The notification class was not modified between the two calls; only the
> implementation behind the bridge changed."

**Scenario 5 — fallback:**
> "Telegram is marked unavailable, so the urgent notification moves down its chain and SMS
> delivers the message. The chain is walked through the interface: a provider is skipped when
> `isAvailable()` returns false **or** when `sendMessage()` returns false. There is no
> `instanceof` anywhere."

**Scenario 6 — both extensions together:**
> "A marketing notification, which I added after the providers were done, sent through the
> Push provider, which I added after the notifications were done. Then the same campaign is
> switched to SMS. No combination class exists for any of this."

---

## 5. Independent extension — the main point of Bridge

**Open** `sender/PushNotificationSender.java` and `notification/MarketingNotification.java`

```bash
git log --oneline
```

> "Two commits prove the main advantage. `Add PushNotificationSender without touching the
> notifications` contains one new provider class and nothing from the notification package.
> `Add MarketingNotification without touching the providers` contains one new notification
> class and nothing from the sender package.
>
> That is the whole idea of Bridge: each side grows on its own. Adding a sixth provider costs
> one class, not four."

---

## 6. UML diagram

**Open** `docs/uml/class-diagram.puml`, or the Mermaid version in `README.md`.

> "The diagram has two packages. The green one is the **Abstraction** side: the abstract class
> `Notification` on top and four **Refined Abstractions** under it — urgent, encrypted,
> scheduled batch and marketing.
>
> The orange one is the **Implementation** side: the `MessageSender` interface is the
> **Implementor**, and the five senders are **Concrete Implementors**. `SimulatedSender` sits
> between them and holds what they share — availability, the delivery log and printing — so
> each provider only writes its own `render` method.
>
> The arrow between the packages is the **Bridge relationship**: an aggregation from
> `Notification` to `MessageSender`, labelled has-a. The second aggregation from
> `UrgentNotification` is the fallback chain, which is a list of the same interface.
>
> Note that there are no arrows between concrete notifications and concrete senders. That is
> the point: twelve pairs on the diagram, zero combination classes."

If the teacher picks one relationship and asks about it:

| Relationship | What to say |
|---|---|
| `Notification` ◆→ `MessageSender` | "Aggregation, the bridge. The notification holds a provider it does not own and can swap at runtime." |
| `Notification` ◁— `UrgentNotification` | "Inheritance inside the abstraction hierarchy: a refined abstraction adds the tag and the fallback chain." |
| `MessageSender` ◁┈ `SimulatedSender` | "Realisation: the shared base implements the interface and leaves `render` to each provider." |
| `GatewayDemo` ┈→ `Notification` | "Dependency: the client creates notifications and providers and wires them together." |

---

## 7. Tests

```bash
./mvnw test
```

> "Eighteen JUnit 5 tests, the assignment asks for ten. They cover every item of the list:
> successful delivery, an unavailable provider, fallback, the SMS length limit, encryption,
> the batch summary, runtime switching, the new Push provider, the new marketing notification
> and a failure scenario where every provider is down and nothing is delivered.
>
> One test is worth pointing at: `fallbackAlsoTakesOverWhenTheProviderRefusesTheMessage`.
> SMS rejects a 200-character message, and the fallback moves the message to Email — so the
> chain reacts not only to an outage but also to a refusal."

---

## 8. The six README questions

| Question | Answer |
|---|---|
| **1. Two independent dimensions?** | What is sent (notification types) and how it travels (providers). |
| **2. Where exactly is the Bridge?** | The `protected MessageSender sender` field in `Notification`, plus `setSender()`. |
| **3. Why is inheritance problematic?** | It multiplies the axes: N × M classes, and the urgent logic would be copied into every provider-specific class. |
| **4. New provider?** | One class implementing `MessageSender`. No notification touched; it also works as a fallback immediately. |
| **5. New notification type?** | One class extending `Notification`. No provider touched; it runs over all five channels. |
| **6. Why Bridge and not Adapter?** | Adapter is applied afterwards to convert an incompatible interface. Nothing is converted here: `MessageSender` was designed up front as the implementation side. Adapter fixes incompatibility, Bridge prevents a combinatorial explosion. |

---

## 9. "Modify one requirement" — section 10 of the assignment

Be ready to edit and re-run. Each of these is a small change:

| Request | What you do |
|---|---|
| Change the SMS limit 160 → 120 | `MAX_LENGTH` in `SmsMessageSender`, one constant |
| Add `WhatsAppMessageSender` | One class extending `SimulatedSender`: write `render` and `getChannelName` |
| Add `SilentNotification` | One class extending `Notification`: `send` logs instead of delivering |
| Make Email unavailable and switch | `email.setAvailable(false)` and either `setSender(...)` or a fallback |
| Add another fallback provider | `urgent.addFallback(kafka)` |
| Change the sender at runtime | `notification.setSender(push)` |

After the edit: `./mvnw test` and run the demo again.

---

## 10. Quick answers

| Question | Answer |
|---|---|
| Is this not Strategy? | "Strategy swaps one algorithm behind a client. Bridge separates two hierarchies that both keep growing — and I showed that by adding a class on each side." |
| Why an abstract class for `Notification` and an interface for `MessageSender`? | "The abstraction shares state — the `sender` field — so an abstract class fits. Providers share no state through the contract, so an interface is enough. The assignment also specifies both shapes." |
| Why does `sendMessage` return `boolean`? | "So the abstraction can react to a refusal without knowing the reason. That is what makes the SMS-to-Email fallback possible." |
| Why is there a `SimulatedSender` base class? | "Only to avoid repeating availability, the log and printing in five providers. The bridge still points at the `MessageSender` interface." |
| Where is the client? | "`GatewayDemo`. It is the only place that names concrete classes: it creates the providers, wires them into notifications and runs the scenarios." |
| How many classes would there be without Bridge? | "Twenty combinations for four types and five providers, and a new provider would add four more. With Bridge it is nine classes and a new provider is one." |

---

## 11. Checklist before the defense

- [ ] `./mvnw test` → `Tests run: 18, Failures: 0`
- [ ] `java -cp target/classes kz.aitu.sdp.notify.GatewayDemo` prints all six scenarios
- [ ] `git log --oneline` shows eleven step-by-step commits
- [ ] Everything is pushed to GitHub
- [ ] Open in the editor: `Notification.java`, `UrgentNotification.java`, `SmsMessageSender.java`, `PushNotificationSender.java`, the UML file
- [ ] You can say in one sentence where the bridge is and why it is not Adapter
