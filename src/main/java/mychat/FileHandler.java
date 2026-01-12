package mychat;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class FileHandler {

    // Cliente/Server ENVIANDO
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
        System.out.println("Arquivo enviado: " + file.getName());
    }

    // Cliente RECEBENDO (Salva no disco)
    public static void saveFile(DataInputStream dis, String baseDir) throws IOException {
        String fileName = dis.readUTF();
        long size = dis.readLong();

        File dir = new File(baseDir);
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(dir, fileName);

        System.out.println("Recebendo arquivo: " + fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            streamBytes(dis, new DataOutputStream(fos), size, false); // false = não envia cabeçalho de novo
        }
        System.out.println("Arquivo salvo em: " + file.getAbsolutePath());
    }

    // Servidor RETRANSMITINDO (Lê de um, escreve para vários)
    public static void relayFile(DataInputStream dis, Socket sender, Map<Socket, String> clients) throws IOException {
        String fileName = dis.readUTF();
        long size = dis.readLong();
        String senderName = clients.get(sender);

        System.out.println("Relay de arquivo iniciado: " + fileName + " de " + senderName);

        // 1. Avisar todos os destinatários (Header)
        for (Socket target : clients.keySet()) {
            if (target != sender) {
                try {
                    DataOutputStream dos = new DataOutputStream(target.getOutputStream());
                    dos.writeByte(ChatProtocol.TYPE_FILE);
                    dos.writeUTF(fileName);
                    dos.writeLong(size);
                    dos.flush();
                } catch (IOException e) {
                    /* Tratar desconexão */ }
            }
        }

        // 2. Transferir o corpo (Body)
        // Criamos um buffer temporário para ler do remetente e escrever nos
        // destinatários
        byte[] buffer = new byte[4096];
        long totalRead = 0;

        while (totalRead < size) {
            int remaining = (int) (size - totalRead);
            int read = dis.read(buffer, 0, Math.min(buffer.length, remaining));
            if (read == -1)
                break;
            totalRead += read;

            // Escreve o pedaço lido para todos
            for (Socket target : clients.keySet()) {
                if (target != sender) {
                    try {
                        DataOutputStream targetDos = new DataOutputStream(target.getOutputStream());
                        targetDos.write(buffer, 0, read);
                        targetDos.flush();
                    } catch (IOException e) {
                        /* Ignorar erro de cliente único */ }
                }
            }
        }
        System.out.println("Relay de arquivo concluído.");
    }

    // Método auxiliar privado para mover bytes de um stream para outro
    private static void streamBytes(InputStream in, OutputStream out, long size, boolean closeOut) throws IOException {
        // Lógica genérica de streaming se quiser reutilizar
    }
}