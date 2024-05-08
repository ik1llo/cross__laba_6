import java.io.*;
import java.net.*;
import java.util.Scanner;
import javax.swing.*;

class CONFIG {
    public static String MULTICAST_IP = "224.0.0.1"; 
    public static int PORT = 8080; 
    public static int BUFFER_SIZE = 2048;
}

public class Application_2_2 { 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Multicast UDP Chat");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
            JTextArea chat_area = new JTextArea(30, 30);
                chat_area.setEditable(false);
            JScrollPane scroll_pane = new JScrollPane(chat_area);
        
            JTextField message_field = new JTextField(null);

            ChatMember chat_member = new ChatMember(chat_area);
            chat_member.launch();

            JButton send_button = new JButton("SEND");
            send_button.addActionListener(e -> {
                String message = message_field.getText();
                if (!message.isEmpty()) {
                    chat_member.send_message(message);
                    message_field.setText("");
                }
            });

            JPanel main_panel = new JPanel();
            main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.Y_AXIS));

            JPanel upper_panel = new JPanel();
                upper_panel.add(scroll_pane);
        
            JPanel lower_panel = new JPanel();
            lower_panel.setLayout(new BoxLayout(lower_panel, BoxLayout.X_AXIS));
                lower_panel.add(message_field);
                lower_panel.add(send_button);
                
            main_panel.add(upper_panel);
            main_panel.add(lower_panel);

            frame.add(main_panel);
            frame.pack();
            frame.setVisible(true);
        });
    }
}

class ChatMember {
    InetAddress group;
    MulticastSocket socket;

    JTextArea chat_area;

    boolean is_last_message_mine;

    public ChatMember(JTextArea chat_area) {
        try {
            this.group = InetAddress.getByName(CONFIG.MULTICAST_IP);
            this.socket = new MulticastSocket(CONFIG.PORT);

            this.chat_area = chat_area;
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

                        if (!this.is_last_message_mine) { show_message(this.chat_area, "[group member]: " + received_message); }
                        else { show_message(this.chat_area, "[me]: " + received_message); }

                        this.is_last_message_mine = false;
                    } catch (IOException e) { e.printStackTrace(); }
                }
            });
            receiver_thread.start();

        } catch (Exception err) { err.printStackTrace(); System.out.println("[error while multicast server launching]"); }
    }

    protected void send_message(String message) {
        try {
            this.is_last_message_mine = true;
            byte[] buffer = message.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, this.group, CONFIG.PORT);
            socket.send(packet);
        } catch (Exception err) { System.out.println("[error while sending a message]"); }
    }

    protected void show_message(JTextArea chat_area, String message) {
        String new_text = chat_area.getText() + "\n" + message;
        chat_area.setText(new_text);
    }
}