package kz.aitu.sdp.notify.sender;

/**
 * Implementor of the Bridge pattern: a delivery provider.
 *
 * <p>Notifications talk only to this interface, never to a concrete provider, so the two
 * hierarchies can grow independently.
 */
public interface MessageSender {

    /**
     * Delivers one message.
     *
     * @return true when the provider accepted the message
     */
    boolean sendMessage(String recipient, String title, String body);

    /** False when the provider is down; the abstraction decides what to do about it. */
    boolean isAvailable();

    String getChannelName();
}
