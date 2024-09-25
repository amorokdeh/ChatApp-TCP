import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Chat implements Runnable {

    public static ArrayList<Chat> chats = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public Chat(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            chats.add(this);
            broadcastMessage( clientUsername + " is here!");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                //Message to all
                if (messageFromClient.charAt(0) == '1') {
                    messageFromClient = messageFromClient.substring(1);
                    messageFromClient = clientUsername + " (Public): " + messageFromClient;
                    broadcastMessage(messageFromClient);
                //Message to one user
                } else if (messageFromClient.charAt(0) == '2') {
                    //find user
                    String username = "";
                    messageFromClient = messageFromClient.substring(1);
                    while (true) {
                        username = username + messageFromClient.charAt(0);
                        messageFromClient = messageFromClient.substring(1);
                        if(messageFromClient.charAt(0) == ' ') {
                            break;
                        }
                    }
                    messageFromClient = clientUsername + " (Private):" + messageFromClient;
                    oneToOneMessage(username, messageFromClient);
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {
        for(Chat chat : chats) {
            try {
                if (!chat.clientUsername.equals(clientUsername)) {
                    chat.bufferedWriter.write(messageToSend);
                    chat.bufferedWriter.newLine();
                    chat.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }

    }

    public void oneToOneMessage(String username, String messageToSend) {
        boolean found = false;
        for(Chat chat : chats) {
            try {
                if (chat.clientUsername.equals(username)) {
                    chat.bufferedWriter.write(messageToSend);
                    chat.bufferedWriter.newLine();
                    chat.bufferedWriter.flush();
                    found = true;
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
        if (! found) {
            try {
                this.bufferedWriter.write("User not found");
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        } else {
            try {
                this.bufferedWriter.write("Message has been sent");
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler() {
        chats.remove(this);
        broadcastMessage(clientUsername + " has left the chat");
    }
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if(bufferedReader != null) {
                bufferedReader.close();
            }
            if(bufferedWriter != null) {
                bufferedWriter.close();
            }
            if(socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getClientUsername() {
        return clientUsername;
    }
}
