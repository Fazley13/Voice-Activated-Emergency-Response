import javax.sound.sampled.*;
import java.io.OutputStream;
import java.net.Socket;

public class AudioSender {
    public static void main(String[] args) {
        String receiverIP = "127.0.0.1";  // Replace with receiver's IP
        int port = 5000;

        try (Socket socket = new Socket(receiverIP, port)) {
            System.out.println("🎙️ Connected to receiver!");

            AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, false);
            TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
            microphone.open(format);
            microphone.start();

            OutputStream outputStream = socket.getOutputStream();
            byte[] buffer = new byte[4096];

            System.out.println("🎤 Sending audio...");
            while (true) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                outputStream.write(buffer, 0, bytesRead);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
