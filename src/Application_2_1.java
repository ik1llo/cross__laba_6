import java.io.*;
import java.net.*;
import java.util.Scanner;

class CONFIG {
    public static String MULTICAST_IP = "224.0.0.1"; 
    public static int PORT = 8080; 
    public static int BUFFER_SIZE = 2048;
}

public class Application_2_1 { 
    public static void main(String[] args) {
        ChatMember chat_member = new ChatMember();
        chat_member.launch();
    }
}

class ChatMember {
    InetAddress group;
    MulticastSocket socket;

    boolean is_last_message_mine;

    public ChatMember() {
        try {
            this.group = InetAddress.getByName(CONFIG.MULTICAST_IP);
            this.socket = new MulticastSocket(CONFIG.PORT);

            this.is_last_message_mine = false;
        } catch (Exception err) { System.out.println("[error while multicast server initialization]"); }
    }

    protected void launch() {
        try( Scanner scanner = new Scanner(System.in) ) {
            NetworkInterface network_interface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            this.socket.joinGroup(new InetSocketAddress(group, CONFIG.PORT), network_interface);

            Thread receiver_thread = new Thread(() -> {
                while (true) {
                    try {
                        byte[] buffer = new byte[CONFIG.BUFFER_SIZE];

                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);

                        String received_message = new String(packet.getData(), 0, packet.getLength());
                        if (!this.is_last_message_mine) { System.out.println("[group member]: " + received_message); }
                        else { System.out.println("[me]: " + received_message); }

                        this.is_last_message_mine = false;
                    } catch (IOException e) { e.printStackTrace(); }
                }
            });
            receiver_thread.start();
            
            while (true) {
                String message = scanner.nextLine();
                byte[] buffer = message.getBytes();

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, CONFIG.PORT);
                socket.send(packet);
                this.is_last_message_mine = true;
            }
        } catch (Exception err) { err.printStackTrace(); System.out.println("[error while multicast server launching]"); }
    }
}

