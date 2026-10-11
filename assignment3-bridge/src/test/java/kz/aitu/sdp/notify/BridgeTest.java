package kz.aitu.sdp.notify;

import java.util.ArrayList;
import java.util.List;
import kz.aitu.sdp.notify.notification.BatchSummary;
import kz.aitu.sdp.notify.notification.EncryptedNotification;
import kz.aitu.sdp.notify.notification.MarketingNotification;
import kz.aitu.sdp.notify.notification.Notification;
import kz.aitu.sdp.notify.notification.ScheduledBatchNotification;
import kz.aitu.sdp.notify.notification.UrgentNotification;
import kz.aitu.sdp.notify.sender.EmailMessageSender;
import kz.aitu.sdp.notify.sender.KafkaEventSender;
import kz.aitu.sdp.notify.sender.MessageSender;
import kz.aitu.sdp.notify.sender.PushNotificationSender;
import kz.aitu.sdp.notify.sender.SmsMessageSender;
import kz.aitu.sdp.notify.sender.TelegramMessageSender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The abstraction side and the implementation side, and the bridge between them. */
class BridgeTest {

    private final TelegramMessageSender telegram = new TelegramMessageSender();
    private final EmailMessageSender email = new EmailMessageSender();
    private final SmsMessageSender sms = new SmsMessageSender();

    @Test
    void urgentNotificationIsDeliveredAndTagged() {
        new UrgentNotification(telegram).send("@aliya", "Disk is full");

        assertAll(
                () -> assertEquals(1, telegram.deliveredCount()),
                () -> assertTrue(telegram.lastDelivered().contains("[URGENT] Disk is full")));
    }

    @Test
    void unavailableProviderDeliversNothing() {
        telegram.setAvailable(false);

        new UrgentNotification(telegram).send("@aliya", "Disk is full");

        assertAll(
                () -> assertFalse(telegram.isAvailable()),
                () -> assertEquals(0, telegram.deliveredCount()));
    }

    @Test
    void fallbackProviderTakesOverWhenThePrimaryIsDown() {
        telegram.setAvailable(false);
        UrgentNotification urgent = new UrgentNotification(telegram, sms);

        urgent.send("+77011111111", "Power outage");

        assertAll(
                () -> assertEquals(0, telegram.deliveredCount()),
                () -> assertEquals(1, sms.deliveredCount()),
                () -> assertEquals("SMS", urgent.lastUsedChannel().orElseThrow()));
    }

    @Test
    void fallbackAlsoTakesOverWhenTheProviderRefusesTheMessage() {
        UrgentNotification urgent = new UrgentNotification(sms, email);

        urgent.send("aliya@example.com", "x".repeat(200));

        assertAll(
                () -> assertEquals(0, sms.deliveredCount(), "too long for one SMS"),
                () -> assertEquals(1, email.deliveredCount()),
                () -> assertEquals("Email", urgent.lastUsedChannel().orElseThrow()));
    }

    /** Failure scenario: nothing is left to try. */
    @Test
    void urgentNotificationReportsFailureWhenEveryProviderIsDown() {
        telegram.setAvailable(false);
        sms.setAvailable(false);
        UrgentNotification urgent = new UrgentNotification(telegram, sms);

        urgent.send("@aliya", "Power outage");

        assertAll(
                () -> assertTrue(urgent.lastUsedChannel().isEmpty()),
                () -> assertEquals(0, telegram.deliveredCount()),
                () -> assertEquals(0, sms.deliveredCount()));
    }

    @Test
    void smsRefusesMessagesLongerThanItsLimit() {
        boolean shortMessage = sms.sendMessage("+77011111111", "Hi", "x".repeat(100));
        boolean longMessage = sms.sendMessage("+77011111111", "Hi", "x".repeat(SmsMessageSender.MAX_LENGTH));

        assertAll(
                () -> assertTrue(shortMessage),
                () -> assertFalse(longMessage),
                () -> assertEquals(1, sms.deliveredCount()));
    }

    @Test
    void encryptedNotificationHidesTheMessageAndCanBeDecrypted() {
        new EncryptedNotification(email).send("aliya@example.com", "One-time code 4821");
        String payload = email.lastDelivered();

        assertAll(
                () -> assertFalse(payload.contains("One-time code 4821"), "plain text must not travel"),
                () -> assertTrue(payload.contains(EncryptedNotification.encrypt("One-time code 4821"))),
                () -> assertEquals("One-time code 4821",
                        EncryptedNotification.decrypt(EncryptedNotification.encrypt("One-time code 4821"))));
    }

    @Test
    void batchNotificationSendsToEveryRecipientAndSummarisesTheRun() {
        ScheduledBatchNotification batch = new ScheduledBatchNotification(email,
                List.of("a@example.com", "b@example.com", "c@example.com"));

        BatchSummary summary = batch.sendBatch("Classes start at 09:00");

        assertAll(
                () -> assertEquals(3, summary.total()),
                () -> assertEquals(3, summary.successful()),
                () -> assertEquals(0, summary.failed()),
                () -> assertEquals(3, email.deliveredCount()));
    }

    @Test
    void batchSummaryCountsFailuresSeparately() {
        ScheduledBatchNotification batch = new ScheduledBatchNotification(sms,
                List.of("+77011111111", "+77012222222"));
        sms.setAvailable(false);

        BatchSummary summary = batch.sendBatch("Classes start at 09:00");

        assertAll(
                () -> assertEquals(2, summary.total()),
                () -> assertEquals(0, summary.successful()),
                () -> assertEquals(2, summary.failed()),
                () -> assertTrue(summary.toString().contains("Failed: 2")));
    }

    @Test
    void theSameNotificationObjectChangesItsProviderAtRuntime() {
        Notification notification = new UrgentNotification(telegram);

        notification.send("@aliya", "Deploy finished");
        notification.setSender(email);
        notification.send("aliya@example.com", "Deploy finished");

        assertAll(
                () -> assertEquals(1, telegram.deliveredCount()),
                () -> assertEquals(1, email.deliveredCount()));
    }

    @Test
    void everyProviderFormatsTheSameMessageInItsOwnWay() {
        KafkaEventSender kafka = new KafkaEventSender();
        List<MessageSender> providers = List.of(telegram, email, sms, kafka);
        List<String> payloads = new ArrayList<>();

        for (MessageSender provider : providers) {
            new UrgentNotification(provider).send("user", "Backup failed");
        }
        payloads.add(telegram.lastDelivered());
        payloads.add(email.lastDelivered());
        payloads.add(sms.lastDelivered());
        payloads.add(kafka.lastDelivered());

        assertAll(
                () -> assertTrue(payloads.get(0).startsWith("*Urgent notification*"), "Telegram formats the title"),
                () -> assertTrue(payloads.get(1).contains("Notification Gateway"), "Email signs the message"),
                () -> assertTrue(payloads.get(2).startsWith("Urgent notification:"), "SMS is plain text"),
                () -> assertTrue(payloads.get(3).startsWith("topic=notifications"), "Kafka publishes an event"),
                () -> assertEquals(4, payloads.stream().distinct().count()));
    }

    @Test
    void oneNotificationClassServesEveryProvider() {
        for (MessageSender provider : List.of(telegram, email, sms, new KafkaEventSender())) {
            new UrgentNotification(provider).send("user", "Backup failed");
        }

        assertAll(
                () -> assertEquals(1, telegram.deliveredCount()),
                () -> assertEquals(1, email.deliveredCount()),
                () -> assertEquals(1, sms.deliveredCount()));
    }

    /** Section 6: a provider added later works with notifications written before it. */
    @Test
    void newPushProviderWorksWithTheExistingNotifications() {
        PushNotificationSender push = new PushNotificationSender();

        new UrgentNotification(push).send("device-42", "Disk is full");
        new EncryptedNotification(push).send("device-42", "One-time code 4821");

        assertAll(
                () -> assertEquals(2, push.deliveredCount()),
                () -> assertTrue(push.delivered().get(0).contains("[URGENT] Disk is full")),
                () -> assertTrue(push.delivered().get(0).startsWith("device=device-42")));
    }

    @Test
    void pushProviderTrimsLongMessagesToTheBannerSize() {
        PushNotificationSender push = new PushNotificationSender();

        new UrgentNotification(push).send("device-42", "x".repeat(300));

        assertTrue(push.lastDelivered().endsWith("..."));
    }

    @Test
    void newProviderAlsoJoinsAFallbackChain() {
        PushNotificationSender push = new PushNotificationSender();
        telegram.setAvailable(false);
        UrgentNotification urgent = new UrgentNotification(telegram, push);

        urgent.send("device-42", "Power outage");

        assertEquals("Push", urgent.lastUsedChannel().orElseThrow());
    }

    /** Section 6: a notification type added later works with providers written before it. */
    @Test
    void newMarketingNotificationWorksWithTheExistingProviders() {
        MarketingNotification campaign = new MarketingNotification(email, "Autumn sale");

        campaign.send("aliya@example.com", "Tours are 20% off");
        campaign.setSender(telegram);
        campaign.send("@aliya", "Tours are 20% off");

        assertAll(
                () -> assertEquals(1, email.deliveredCount()),
                () -> assertEquals(1, telegram.deliveredCount()),
                () -> assertTrue(email.lastDelivered().contains("Reply STOP to unsubscribe")),
                () -> assertTrue(telegram.lastDelivered().startsWith("*Autumn sale*")));
    }

    @Test
    void marketingNotificationSkipsRecipientsWhoOptedOut() {
        MarketingNotification campaign = new MarketingNotification(telegram, "Autumn sale");
        campaign.optOut("@bolat");

        campaign.send("@aliya", "Tours are 20% off");
        campaign.send("@bolat", "Tours are 20% off");

        assertEquals(1, telegram.deliveredCount());
    }

    @Test
    void newNotificationTypeAndNewProviderWorkTogether() {
        PushNotificationSender push = new PushNotificationSender();
        MarketingNotification campaign = new MarketingNotification(push, "Autumn sale");

        campaign.send("device-42", "Tours are 20% off");

        assertAll(
                () -> assertEquals(1, push.deliveredCount()),
                () -> assertTrue(push.lastDelivered().contains("push[Autumn sale]")));
    }
}
