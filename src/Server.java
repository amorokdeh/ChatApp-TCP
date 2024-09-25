import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        System.out.println("Server is ready!");
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                Chat chat = new Chat(socket);
                String username = chat.getClientUsername();
                System.out.println(username + " has connected!");

                Thread thread = new Thread(chat);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  void closeServerSocket() {
        try {
            if(serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
