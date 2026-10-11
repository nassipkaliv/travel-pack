# Multi-Channel Notification Gateway — Bridge Pattern

Assignment 3, Software Design Patterns. Java 17, Maven, JUnit 5.

The gateway sends different **types of notification** through different **delivery providers**.
Without Bridge this needs N × M classes (`UrgentTelegramNotification`, `EncryptedEmailNotification`, …).
Bridge splits the two axes so they can evolve independently:

- **Abstraction** — notification types;
- **Implementor** — message delivery providers (`MessageSender`).

## Run

```bash
./mvnw test
```

Work in progress: the module skeleton is in place, the Bridge hierarchies follow.
