package mychat.common;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class FileHandler {

    /**
     * Sends a file through the DataOutputStream.
     * Includes a header with type, filename, and file size.
     */
    public static void sendFile(DataOutputStream dos, File file) throws IOException {
        dos.writeByte(ChatProtocol.TYPE_FILE);
        dos.writeUTF(file.getName());
        dos.writeLong(file.length());

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, read);
            }
        }   
        dos.flush();
    }

    /**
     * Receives a file from the DataInputStream and saves it to the specified directory.
     * Returns the name of the saved file.
     */
    public static String receiveFile(DataInputStream dis, String saveDir) throws IOException {
        String fileName = dis.readUTF();
        long size = dis.readLong();

        File dir = new File(saveDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int read;
            long totalRead = 0;
            
            while (totalRead < size && (read = dis.read(buffer, 0, (int) Math.min(buffer.length, size - totalRead))) != -1) {
                fos.write(buffer, 0, read);
                totalRead += read;
            }
        }
        return fileName;
    }

    /**
     * Server-side relay: Reads a file from one client and streams it to all others.
     * This avoids loading the entire file into memory by using a streaming buffer.
     */
    public static void relayFile(DataInputStream dis, Socket sender, Map<Socket, String> clients) throws IOException {
        String fileName = dis.readUTF();
        long size = dis.readLong();

        // Step 1: Send the file header to all recipients
        for (Socket target : clients.keySet()) {
            if (target != sender) {
                try {
                    DataOutputStream dos = new DataOutputStream(target.getOutputStream());
                    dos.writeByte(ChatProtocol.TYPE_FILE);
                    dos.writeUTF(fileName);
                    dos.writeLong(size);
                    dos.flush();
                } catch (IOException e) {
                    // Specific client delivery failure handled silently
                }
            }
        }

        // Step 2: Stream the file body in chunks
        byte[] buffer = new byte[4096];
        long totalRead = 0;

        while (totalRead < size) {
            int remaining = (int) (size - totalRead);
            int read = dis.read(buffer, 0, Math.min(buffer.length, remaining));
            if (read == -1) break;
            totalRead += read;

            for (Socket target : clients.keySet()) {
                if (target != sender) {
                    try {
                        DataOutputStream targetDos = new DataOutputStream(target.getOutputStream());
                        targetDos.write(buffer, 0, read);
                        targetDos.flush();
                    } catch (IOException e) {
                        // Ignore individual target failures
                    }
                }
            }
        }
    }
}