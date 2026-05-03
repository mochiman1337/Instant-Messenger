import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class TCPServer {

    private static int port = 6001;

    // A dictionary mapping authenticated Usernames to their network connection stream
    static ArrayList<UserObject> activeUsers = new ArrayList<UserObject>();

    public static void main(String argv[]) throws Exception {
        ServerSocket welcomeSocket = new ServerSocket(port);
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
            UserObject loggedInUser = null;

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

                    if (parts.length >= 3) {
                        switch (command) {
                            case "AUTH":
                                String username = parts[1];
                                String password = parts[2]; // Password validation would go here
                                loggedInUser = new UserObject(username, outToClient);
//                              Functionally the same as:
//                                  loggedInUser = new UserObject();
//                                  loggedInUser.setUsername(username);
//                                  loggedInUser.setDataStream(outToClient);
                                activeUsers.add(loggedInUser);
                                outToClient.writeBytes("SERVER: Authenticated successfully as " + loggedInUser + "\n");
                                System.out.println(loggedInUser + " logged in.");
                                break;
                            case "MSG":
                                if (loggedInUser != null) {
                                    String targetUser = parts[1];
                                    String message = parts[2];

                                    // Route the message to the target user if they are online
                                    UserObject targetUserObject = getUser(targetUser);
                                    if (targetUserObject != null) {
                                        DataOutputStream targetOut = targetUserObject.getDataStream();
                                        targetOut.writeBytes("MSG from " + loggedInUser + ": " + message + "\n");
                                    } else {
                                        outToClient.writeBytes("SERVER: User " + targetUser + " is offline.\n");
                                    }
                                } else {
                                    throw new UserNotFoundException("User does not exist.");
                                }
                                break;
                            case "FILE":
                                if (loggedInUser != null) {
                                    String targetUser = parts[1];

                                    // Split the data payload into filename and base64 string
                                    String[] fileParts = parts[2].split(" ", 2);

                                    if (fileParts.length == 2) {
                                        String fileName = fileParts[0];
                                        String fileData = fileParts[1];

                                        // Route the encoded file string to the target user
                                        UserObject targetUserObject = getUser(targetUser);
                                        if (targetUserObject != null) {
                                            DataOutputStream targetOut = targetUserObject.getDataStream();
                                            targetOut.writeBytes("FILE from " + loggedInUser + " " + fileName + " " + fileData + "\n");
                                        } else {
                                            outToClient.writeBytes("SERVER: User " + targetUser + " is offline.\n");
                                        }
                                    }
                                } else {
                                    throw new UserNotFoundException("User does not exist.");
                                }
                        }
                    } else{
                        throw new InvalidArgCount("Not enough arguments.");
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
                    for (UserObject remainingClient : activeUsers) {
                        try {
                            remainingClient.getDataStream().writeBytes("SERVER: " + loggedInUser.getUsername() + " has left the chat.\n");
                        } catch (Exception ex) {
                            // Ignore if another user happens to be disconnecting at the exact same time
                        }
                    }
                }
            }
        }

        public UserObject getUser(String username) {
            for (int i = 0; i < activeUsers.size(); i++) {
                UserObject current_user = activeUsers.get(i);
                if (current_user.getUsername() == username) {
                    return current_user;
                }
            }
            return null;
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
