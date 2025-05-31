import javax.sound.sampled.*;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class AudioReceiver {
    public static void main(String[] args) {
        int port = 5000;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("🎧 Listening for audio on port " + port + "...");

            Socket clientSocket = serverSocket.accept();
            System.out.println("🔗 Connection established with sender!");

            AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, false);
            SourceDataLine speakers = AudioSystem.getSourceDataLine(format);
            speakers.open(format);
            speakers.start();

            InputStream inputStream = clientSocket.getInputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                speakers.write(buffer, 0, bytesRead);
            }

            speakers.drain();
            speakers.close();
            clientSocket.close();
            System.out.println("🔌 Connection closed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
