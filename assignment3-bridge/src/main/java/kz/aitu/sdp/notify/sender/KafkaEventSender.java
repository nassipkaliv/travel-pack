package kz.aitu.sdp.notify.sender;

/** Simulates publishing an event to a topic instead of delivering text to a person. */
public class KafkaEventSender extends SimulatedSender {

    public static final String TOPIC = "notifications";

    @Override
    protected String render(String recipient, String title, String body) {
        return "topic=%s key=%s value={\"title\":\"%s\",\"body\":\"%s\"}"
                .formatted(TOPIC, recipient, title, body);
    }

    @Override
    public String getChannelName() {
        return "Kafka";
    }
}
