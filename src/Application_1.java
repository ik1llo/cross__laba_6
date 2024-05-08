import java.io.*;
import java.net.*;

class CONFIG {
    public static String HOST = "localhost";
    public static int PORT = 6060; 
    public static int BUFFER_SIZE = 4;
}

public class Application_1 {
    public static void main(String[] args) {
        EchoServer echo_server = new EchoServer();
            echo_server.start();

        try { Thread.sleep(100); } 
        catch (InterruptedException e) { e.printStackTrace(); }
            
        EchoClient echo_client_1 = new EchoClient();
            echo_client_1.echo_server();

        EchoClient echo_client_2 = new EchoClient();
            echo_client_2.echo_server();

        echo_server.terminate_server();
    }
}

class EchoServer extends Thread {
    private boolean thread_alive;
    private DatagramSocket server_socket;

    public EchoServer() { this.thread_alive = true; }

    @Override
    public void run() {
        while (thread_alive) {
            try {
                this.server_socket = new DatagramSocket(CONFIG.PORT);
                System.out.println("server started successfully on port: " + CONFIG.PORT);
                System.out.println();
     
                while (true && thread_alive) {
                    byte[] buffer = new byte[CONFIG.BUFFER_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    this.server_socket.receive(packet);
                    String received_message = new String(packet.getData(), 0, packet.getLength());


                    InetAddress client_addr = packet.getAddress();
                    int client_port = packet.getPort();
                    

                    byte[] response_data = received_message.getBytes();
                    DatagramPacket response_packet = new DatagramPacket(response_data, response_data.length, client_addr, client_port);
                    this.server_socket.send(response_packet);
    
                    try { Thread.sleep(10); } 
                    catch (InterruptedException e) { e.printStackTrace(); }
                }  
            } 
            catch (Exception err) { System.out.println("[error while server launching]"); }
        }
    }

    public void terminate_server() {
        try {
            this.thread_alive = false;
            this.server_socket.close();
        } catch (Exception err) { System.out.println("[error while server termination]"); }
    }
}

class EchoClient {
    public void echo_server() {
        try { 
            DatagramSocket socket = new DatagramSocket();
            InetAddress server_addr = InetAddress.getByName(CONFIG.HOST);
            
            String message_to_send = "echo";
            byte[] buffer = message_to_send.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, server_addr, CONFIG.PORT);
            socket.send(packet); 

            buffer = new byte[CONFIG.BUFFER_SIZE];
            packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String received_message = new String(packet.getData(), 0, packet.getLength());

            if (received_message.equals(message_to_send)) { System.out.println("[server " + server_addr + ":" + CONFIG.PORT + " works fine]"); } 
            else { System.out.println("[server " + server_addr + ":" + CONFIG.PORT + " does not work]"); }

            socket.close();
        } catch (IOException e) { System.out.println("[error while connecting to the server]"); }
    }
}