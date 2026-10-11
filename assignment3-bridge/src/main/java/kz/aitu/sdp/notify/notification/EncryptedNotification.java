package kz.aitu.sdp.notify.notification;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import kz.aitu.sdp.notify.sender.MessageSender;

/**
 * Refined abstraction: transforms the message before handing it to the provider.
 *
 * <p>XOR with a fixed key and Base64 on top. It is not real cryptography; it only shows
 * that an abstraction can change the message without knowing which provider carries it.
 */
public class EncryptedNotification extends Notification {

    private static final byte KEY = 0x2A;
    private static final String TITLE = "Encrypted message";

    public EncryptedNotification(MessageSender sender) {
        super(sender);
    }

    @Override
    public void send(String recipient, String message) {
        sender.sendMessage(recipient, TITLE, encrypt(message));
    }

    public static String encrypt(String text) {
        return Base64.getEncoder().encodeToString(xor(text.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decrypt(String encoded) {
        return new String(xor(Base64.getDecoder().decode(encoded)), StandardCharsets.UTF_8);
    }

    private static byte[] xor(byte[] bytes) {
        byte[] result = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = (byte) (bytes[i] ^ KEY);
        }
        return result;
    }
}
