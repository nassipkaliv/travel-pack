package kz.aitu.sdp.notify.notification;

import java.util.ArrayList;
import java.util.List;
import kz.aitu.sdp.notify.sender.MessageSender;

/**
 * Refined abstraction: keeps a list of recipients and sends one message to all of them,
 * reporting how many deliveries succeeded.
 */
public class ScheduledBatchNotification extends Notification {

    private static final String TITLE = "Scheduled notification";

    private final List<String> recipients = new ArrayList<>();

    public ScheduledBatchNotification(MessageSender sender, List<String> recipients) {
        super(sender);
        this.recipients.addAll(recipients);
    }

    public void addRecipient(String recipient) {
        recipients.add(recipient);
    }

    public List<String> recipients() {
        return List.copyOf(recipients);
    }

    /** Sends to one recipient, as the base abstraction requires. */
    @Override
    public void send(String recipient, String message) {
        sender.sendMessage(recipient, TITLE, message);
    }

    /** Sends to the whole collection and counts the results. */
    public BatchSummary sendBatch(String message) {
        int successful = 0;
        for (String recipient : recipients) {
            if (sender.sendMessage(recipient, TITLE, message)) {
                successful++;
            }
        }
        return new BatchSummary(recipients.size(), successful, recipients.size() - successful);
    }
}
