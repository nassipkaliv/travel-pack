package kz.aitu.sdp.notify;

import java.util.List;
import kz.aitu.sdp.notify.notification.BatchSummary;
import kz.aitu.sdp.notify.notification.EncryptedNotification;
import kz.aitu.sdp.notify.notification.MarketingNotification;
import kz.aitu.sdp.notify.notification.Notification;
import kz.aitu.sdp.notify.notification.ScheduledBatchNotification;
import kz.aitu.sdp.notify.notification.UrgentNotification;
import kz.aitu.sdp.notify.sender.EmailMessageSender;
import kz.aitu.sdp.notify.sender.PushNotificationSender;
import kz.aitu.sdp.notify.sender.SmsMessageSender;
import kz.aitu.sdp.notify.sender.TelegramMessageSender;


public final class GatewayDemo {

    private final TelegramMessageSender telegram = new TelegramMessageSender();
    private final EmailMessageSender email = new EmailMessageSender();
    private final SmsMessageSender sms = new SmsMessageSender();

    public static void main(String[] args) {
        GatewayDemo demo = new GatewayDemo();
        demo.urgentOverTelegram();
        demo.encryptedOverEmail();
        demo.batchOverSms();
        demo.switchProviderAtRuntime();
        demo.fallbackWhenTelegramIsDown();
        demo.marketingOverPush();
    }

    private void urgentOverTelegram() {
        scenario("1. UrgentNotification + Telegram");
        new UrgentNotification(telegram).send("@aliya", "Flight KC 921 to Antalya is delayed by 2 hours");
    }

    private void encryptedOverEmail() {
        scenario("2. EncryptedNotification + Email");
        new EncryptedNotification(email).send("aliya@example.com", "Your booking code for the Antalya trip is 4821");
        System.out.println("    decrypted back: " + EncryptedNotification.decrypt(lastBody(email)));
    }

    private void batchOverSms() {
        scenario("3. ScheduledBatchNotification + SMS");
        ScheduledBatchNotification batch = new ScheduledBatchNotification(sms,
                List.of("+77011111111", "+77012222222", "+77013333333"));
        BatchSummary summary = batch.sendBatch("Transfer to the hotel leaves at 09:00 tomorrow");
        System.out.println(summary.toString().indent(4).stripTrailing());
    }

    /** The same object keeps working after its implementation is replaced. */
    private void switchProviderAtRuntime() {
        scenario("4. Runtime switching: Telegram -> Email");
        Notification notification = new UrgentNotification(telegram);
        notification.send("@aliya", "Villa check-in confirmed for 14 October");
        notification.setSender(email);
        notification.send("aliya@example.com", "Villa check-in confirmed for 14 October");
        System.out.println("    the UrgentNotification class was not modified between the two calls");
    }

    private void fallbackWhenTelegramIsDown() {
        scenario("5. Telegram unavailable -> fallback to SMS");
        telegram.setAvailable(false);
        UrgentNotification urgent = new UrgentNotification(telegram, sms);
        urgent.send("+77011111111", "Gate changed: flight KC 921 now departs from gate B12");
        System.out.println("    delivered through: " + urgent.lastUsedChannel().orElse("nobody"));
        telegram.setAvailable(true);
    }

    /** Both sides of the bridge extended after the fact, still no combination class. */
    private void marketingOverPush() {
        scenario("6. MarketingNotification + PushNotificationSender");
        PushNotificationSender push = new PushNotificationSender();
        MarketingNotification campaign = new MarketingNotification(push, "Autumn sale");
        campaign.optOut("device-77");
        campaign.send("device-42", "Tours to Antalya are 20% off this week");
        campaign.send("device-77", "Tours to Antalya are 20% off this week");
        campaign.setSender(sms);
        campaign.send("+77011111111", "Tours to Antalya are 20% off this week");
    }

    private static String lastBody(EmailMessageSender email) {
        String payload = email.lastDelivered();
        String[] parts = payload.split("\n\n");
        return parts[1];
    }

    private static void scenario(String title) {
        System.out.println();
        System.out.println(title);
    }
}
