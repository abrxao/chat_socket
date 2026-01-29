package mychat.client;

import mychat.client.gui.ClientGUI;
import mychat.common.ChatProtocol;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class MessageListener implements Runnable {
    private final Socket socket;
    private final ClientGUI gui;
    private final String savePath;

    public MessageListener(Socket socket, ClientGUI gui, String savePath) {
        this.socket = socket;
        this.gui = gui;
        this.savePath = savePath;
    }

    @Override
    public void run() {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            while (!socket.isClosed()) {
                // Delegates parsing to Protocol, but passes a callback (lambda) to update GUI
                ChatProtocol.handleClientResponse(dis, savePath, message -> {

                    gui.appendMessage(message);
                });
            }
        } catch (IOException e) {
            gui.appendMessage("[SYSTEM] Connection lost.");
        }
    }
}