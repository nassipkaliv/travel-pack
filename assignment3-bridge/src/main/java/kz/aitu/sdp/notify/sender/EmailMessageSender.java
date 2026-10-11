package kz.aitu.sdp.notify.sender;

/** Supports a subject line and appends a signature to every message. */
public class EmailMessageSender extends SimulatedSender {

    private static final String SIGNATURE = "--\nNotification Gateway, AITU";

    @Override
    protected String render(String recipient, String title, String body) {
        return "Subject: %s\nTo: %s\n\n%s\n\n%s".formatted(title, recipient, body, SIGNATURE);
    }

    @Override
    public String getChannelName() {
        return "Email";
    }
}
