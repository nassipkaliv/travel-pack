package kz.aitu.sdp.notify.notification;

import java.util.ArrayList;
import java.util.List;
import kz.aitu.sdp.notify.sender.MessageSender;

/**
 * Notification type added after the providers were finished (section 6 of the assignment).
 *
 * <p>A campaign message: it carries a campaign name, appends an unsubscribe line and skips
 * recipients who opted out. It was written against {@link MessageSender} only, so it works
 * with every existing provider and no provider had to change.
 */
public class MarketingNotification extends Notification {

    private static final String UNSUBSCRIBE = "Reply STOP to unsubscribe";

    private final String campaign;
    private final List<String> optedOut = new ArrayList<>();

    public MarketingNotification(MessageSender sender, String campaign) {
        super(sender);
        this.campaign = campaign;
    }

    public void optOut(String recipient) {
        optedOut.add(recipient);
    }

    @Override
    public void send(String recipient, String message) {
        if (optedOut.contains(recipient)) {
            System.out.printf("    %s opted out of %s%n", recipient, campaign);
            return;
        }
        sender.sendMessage(recipient, campaign, message + "\n" + UNSUBSCRIBE);
    }
}
