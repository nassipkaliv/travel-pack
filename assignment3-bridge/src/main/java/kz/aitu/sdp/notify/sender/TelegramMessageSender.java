package kz.aitu.sdp.notify.sender;

/** Supports formatted messages: the title is sent in bold Markdown. */
public class TelegramMessageSender extends SimulatedSender {

    @Override
    protected String render(String recipient, String title, String body) {
        return "*%s*\n%s".formatted(title, body);
    }

    @Override
    public String getChannelName() {
        return "Telegram";
    }
}
