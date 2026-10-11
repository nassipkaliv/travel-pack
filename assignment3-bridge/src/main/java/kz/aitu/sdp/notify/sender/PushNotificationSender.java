package kz.aitu.sdp.notify.sender;

/**
 * Provider added after the notifications were finished (section 6 of the assignment).
 *
 * <p>Mobile push: a short title, a body trimmed to the size of a notification banner and a
 * device token instead of an address. No notification class had to change for it to work.
 */
public class PushNotificationSender extends SimulatedSender {

    public static final int BANNER_LENGTH = 120;

    @Override
    protected String render(String recipient, String title, String body) {
        String banner = body.length() <= BANNER_LENGTH ? body : body.substring(0, BANNER_LENGTH - 3) + "...";
        return "device=%s push[%s] %s".formatted(recipient, title, banner);
    }

    @Override
    public String getChannelName() {
        return "Push";
    }
}
