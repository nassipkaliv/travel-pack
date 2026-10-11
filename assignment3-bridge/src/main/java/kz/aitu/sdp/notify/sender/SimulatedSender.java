package kz.aitu.sdp.notify.sender;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared behaviour of the simulated providers: availability, a delivery log and printing.
 *
 * <p>No external API is called. Every provider only renders the message in its own format
 * and remembers what it delivered, so the demo and the tests can inspect it.
 */
public abstract class SimulatedSender implements MessageSender {

    private final List<String> delivered = new ArrayList<>();
    private boolean available = true;

    @Override
    public final boolean sendMessage(String recipient, String title, String body) {
        if (!available) {
            return false;
        }
        String payload = render(recipient, title, body);
        if (!accepts(payload)) {
            return false;
        }
        delivered.add(payload);
        System.out.printf("    %-8s -> %s : %s%n", getChannelName(), recipient, oneLine(payload));
        return true;
    }

    /** How this provider formats a message. */
    protected abstract String render(String recipient, String title, String body);

    /** A provider may refuse a message, for example when it is too long. */
    protected boolean accepts(String payload) {
        return true;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    /** Simulates an outage, used by the demo and the fallback tests. */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    public List<String> delivered() {
        return List.copyOf(delivered);
    }

    public String lastDelivered() {
        if (delivered.isEmpty()) {
            throw new IllegalStateException(getChannelName() + " delivered nothing");
        }
        return delivered.get(delivered.size() - 1);
    }

    public int deliveredCount() {
        return delivered.size();
    }

    private static String oneLine(String payload) {
        String flat = payload.replace("\n", " | ");
        return flat.length() <= 70 ? flat : flat.substring(0, 67) + "...";
    }
}
