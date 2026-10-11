package kz.aitu.sdp.notify.notification;

import kz.aitu.sdp.notify.sender.MessageSender;

/**
 * Refined abstraction: marks the message as urgent and refuses to send it through a
 * provider that is down.
 */
public class UrgentNotification extends Notification {

    public static final String PREFIX = "[URGENT]";
    private static final String TITLE = "Urgent notification";

    public UrgentNotification(MessageSender sender) {
        super(sender);
    }

    @Override
    public void send(String recipient, String message) {
        String body = PREFIX + " " + message;
        if (!sender.isAvailable()) {
            System.out.printf("    %s is unavailable%n", sender.getChannelName());
            return;
        }
        sender.sendMessage(recipient, TITLE, body);
    }
}
