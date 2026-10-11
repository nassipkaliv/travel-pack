package kz.aitu.sdp.notify.notification;

import kz.aitu.sdp.notify.sender.MessageSender;

/**
 * Abstraction of the Bridge pattern: a kind of notification.
 *
 * <p>The field {@code sender} is the bridge itself — a notification <em>has a</em> provider
 * instead of inheriting from one. Subclasses decide what the message looks like and when it
 * is sent; the provider decides how it travels. Either side can be replaced without touching
 * the other, and {@link #setSender} replaces it while the program is running.
 */
public abstract class Notification {

    protected MessageSender sender;

    protected Notification(MessageSender sender) {
        this.sender = sender;
    }

    public void setSender(MessageSender sender) {
        this.sender = sender;
    }

    public abstract void send(String recipient, String message);
}
