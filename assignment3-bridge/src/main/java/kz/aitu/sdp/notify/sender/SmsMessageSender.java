package kz.aitu.sdp.notify.sender;

/** One SMS carries at most {@value #MAX_LENGTH} characters; longer messages are refused. */
public class SmsMessageSender extends SimulatedSender {

    public static final int MAX_LENGTH = 160;

    @Override
    protected String render(String recipient, String title, String body) {
        return "%s: %s".formatted(title, body);
    }

    @Override
    protected boolean accepts(String payload) {
        return payload.length() <= MAX_LENGTH;
    }

    @Override
    public String getChannelName() {
        return "SMS";
    }
}
