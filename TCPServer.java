import java.io.*;
import java.net.*;
import java.util.HashMap;

public class TCPServer {

    // A dictionary mapping authenticated Usernames to their network connection stream
    static HashMap<String, DataOutputStream> activeUsers = new HashMap<>();

    public static void main(String argv[]) throws Exception {
        ServerSocket welcomeSocket = new ServerSocket(1999);
        System.out.println("Server is running on port " + welcomeSocket.getLocalPort());

        while (true) {
            // Pause and wait for a client to connect
            Socket connectionSocket = welcomeSocket.accept();
            System.out.println("A new client connection established!");

            // Hand the connection off to a background thread
            ClientHandler handler = new ClientHandler(connectionSocket);
            new Thread(handler).start();
        }
    }

    // Nested class to handle individual clients concurrently
    static class ClientHandler implements Runnable {
        Socket connectionSocket;

        public ClientHandler(Socket socket) {
            this.connectionSocket = socket;
        }

        public void run() {
            String loggedInUser = null;

            try {
                BufferedReader inFromClient =
                        new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient =
                        new DataOutputStream(connectionSocket.getOutputStream());

                String clientSentence;

                // Continuously read lines from this specific client
                while ((clientSentence = inFromClient.readLine()) != null) {

                    // Split the incoming text into: Command, Target, and Data
                    String[] parts = clientSentence.split(" ", 3);
                    String command = parts[0];

                    if (command.equals("AUTH") && parts.length >= 3) {
                        loggedInUser = parts[1];
                        String password = parts[2]; // Password validation would go here

                        activeUsers.put(loggedInUser, outToClient);
                        outToClient.writeBytes("SERVER: Authenticated successfully as " + loggedInUser + "\n");
                        System.out.println(loggedInUser + " logged in.");

                    } else if (command.equals("MSG") && parts.length >= 3 && loggedInUser != null) {
                        String targetUser = parts[1];
                        String message = parts[2];

                        // Route the message to the target user if they are online
                        if (activeUsers.containsKey(targetUser)) {
                            DataOutputStream targetOut = activeUsers.get(targetUser);
                            targetOut.writeBytes("MSG from " + loggedInUser + ": " + message + "\n");
                        } else {
                            outToClient.writeBytes("SERVER: User " + targetUser + " is offline.\n");
                        }

                    } else if (command.equals("FILE") && parts.length >= 3 && loggedInUser != null) {
                        String targetUser = parts[1];

                        // Split the data payload into filename and base64 string
                        String[] fileParts = parts[2].split(" ", 2);

                        if (fileParts.length == 2) {
                            String fileName = fileParts[0];
                            String fileData = fileParts[1];

                            // Route the encoded file string to the target user
                            if (activeUsers.containsKey(targetUser)) {
                                DataOutputStream targetOut = activeUsers.get(targetUser);
                                targetOut.writeBytes("FILE from " + loggedInUser + " " + fileName + " " + fileData + "\n");
                            } else {
                                outToClient.writeBytes("SERVER: User " + targetUser + " is offline.\n");
                            }
                        }
                    }
                }

            } catch (Exception e) {
                // This catches forced disconnects (like hitting Ctrl+C)
            } finally {
                // This block runs no matter HOW they disconnected
                if (loggedInUser != null) {

                    // 1. Remove them from the active list
                    activeUsers.remove(loggedInUser);
                    System.out.println(loggedInUser + " has disconnected.");

                    // 2. Broadcast the disconnection to everyone else
                    for (DataOutputStream remainingClient : activeUsers.values()) {
                        try {
                            remainingClient.writeBytes("SERVER: " + loggedInUser + " has left the chat.\n");
                        } catch (Exception ex) {
                            // Ignore if another user happens to be disconnecting at the exact same time
                        }
                    }
                }
            }
        }
    }
}


/*
class TCPServer {
  public static void main(String argv[]) throws Exception 
    { 
      String clientSentence; 
      String capitalizedSentence; 

      ServerSocket welcomeSocket = new ServerSocket(6789); 
  
      while(true) { 
  
           Socket connectionSocket = welcomeSocket.accept(); 

           BufferedReader inFromClient = 
              new BufferedReader(new
              InputStreamReader(connectionSocket.getInputStream())); 

           DataOutputStream  outToClient = 
             new DataOutputStream(connectionSocket.getOutputStream()); 

           clientSentence = inFromClient.readLine(); 

           capitalizedSentence = clientSentence.toUpperCase() + '\n'; 

           outToClient.writeBytes(capitalizedSentence); 
           System.out.println(capitalizedSentence); 
        } 
    } 
} 
*/
