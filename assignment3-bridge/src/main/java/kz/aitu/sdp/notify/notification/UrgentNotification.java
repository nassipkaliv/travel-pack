package kz.aitu.sdp.notify.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kz.aitu.sdp.notify.sender.MessageSender;

/**
 * Refined abstraction: marks the message as urgent and keeps trying providers until one
 * accepts it.
 *
 * <p>The chain is the current sender followed by the registered fallbacks. A provider is
 * skipped when {@link MessageSender#isAvailable()} is false and after
 * {@link MessageSender#sendMessage} returns false — both are interface methods, so the
 * notification never asks which class a provider is.
 */
public class UrgentNotification extends Notification {

    public static final String PREFIX = "[URGENT]";
    private static final String TITLE = "Urgent notification";

    private final List<MessageSender> fallbacks = new ArrayList<>();
    private String lastUsedChannel;

    public UrgentNotification(MessageSender sender) {
        super(sender);
    }

    public UrgentNotification(MessageSender sender, MessageSender... fallbacks) {
        super(sender);
        this.fallbacks.addAll(List.of(fallbacks));
    }

    public void addFallback(MessageSender fallback) {
        fallbacks.add(fallback);
    }

    /** Channel that delivered the last message, empty when every provider failed. */
    public Optional<String> lastUsedChannel() {
        return Optional.ofNullable(lastUsedChannel);
    }

    @Override
    public void send(String recipient, String message) {
        String body = PREFIX + " " + message;
        lastUsedChannel = null;
        for (MessageSender candidate : chain()) {
            if (!candidate.isAvailable()) {
                System.out.printf("    %s is unavailable, trying the next provider%n",
                        candidate.getChannelName());
                continue;
            }
            if (candidate.sendMessage(recipient, TITLE, body)) {
                lastUsedChannel = candidate.getChannelName();
                return;
            }
            System.out.printf("    %s refused the message, trying the next provider%n",
                    candidate.getChannelName());
        }
        System.out.println("    no provider could deliver the urgent message");
    }

    private List<MessageSender> chain() {
        List<MessageSender> chain = new ArrayList<>();
        chain.add(sender);
        chain.addAll(fallbacks);
        return chain;
    }
}
