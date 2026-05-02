import java.io.*;
import java.net.*;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TCPClient {

    public static void main(String argv[]) throws Exception {
        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        // Connect to the server running on your local machine
        Socket clientSocket = new Socket("localhost", 1999);//Changed to localhost so it works on any machine (root)

        DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

        BufferedReader inFromServer =
                new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        System.out.println("Connected to the server.");
        System.out.println("Commands:");
        System.out.println("  AUTH [username] [password]");
        System.out.println("  MSG [username] [message]");
        System.out.println("  FILE [username] [filepath]");

        // Background thread to continuously listen for server messages
        new Thread(() -> {
            try {
                String incoming;
                while ((incoming = inFromServer.readLine()) != null) {

                    // Intercept incoming files and decode them back to the hard drive
                    if (incoming.startsWith("FILE from ")) {

                        // Split into 5 parts because we added the filename to the network string
                        String[] parts = incoming.split(" ", 5);
                        String sender = parts[2];
                        String fileName = parts[3]; // Extract the original filename
                        String base64Data = parts[4];

                        byte[] fileBytes = Base64.getDecoder().decode(base64Data);

                        // Save recieved file with same extension. Named this way to help demo and avoid replace
                        String saveName = "received_from_" + sender + "_" + fileName;

                        FileOutputStream fos = new FileOutputStream(saveName);
                        fos.write(fileBytes);
                        fos.close();

                        System.out.println("==> File received and saved as: " + saveName);
                    } else {
                        // Print normal text messages to the screen
                        System.out.println(incoming);
                    }
                }
            } catch (Exception e) {
                System.out.println("Disconnected from server.");//Added to track when user leaves
            }
        }).start();//Built-in java method when using threads

        // Main thread loop to continuously read your keyboard input
        String sentence;
        while ((sentence = inFromUser.readLine()) != null) {

            // Intercept the FILE command to encode the file before sending
            if (sentence.startsWith("FILE ")) {
                try {
                    String[] parts = sentence.split(" ", 3);
                    String target = parts[1];
                    String filePath = parts[2];

                    // Read raw file bytes and convert to Base64 text
                    byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
                    String base64File = Base64.getEncoder().encodeToString(fileBytes);

                    // Extract just the file name (e.g., "image.png")
                    File fileObj = new File(filePath);
                    String fileName = fileObj.getName();

                    // Send the filename right before the base64 data
                    outToServer.writeBytes("FILE " + target + " " + fileName + " " + base64File + "\n");
                    System.out.println("File successfully sent to " + target + ".");
                } catch (Exception e) {
                    System.out.println("Failed to read file. Double-check the file path.");
                }
            } else {
                // Send normal AUTH or MSG commands straight to the server
                outToServer.writeBytes(sentence + '\n');
            }
        }
        clientSocket.close();
    }
}


/*
class TCPClient {
    public static void main(String argv[]) throws Exception 
    { 
        String sentence; 
        String modifiedSentence; 

        BufferedReader inFromUser = 
          new BufferedReader(new InputStreamReader(System.in));

		Socket clientSocket = new Socket("localhost", 6789);

        DataOutputStream outToServer = 
          new DataOutputStream(clientSocket.getOutputStream()); 
          
        BufferedReader inFromServer = 
          new BufferedReader(new
          InputStreamReader(clientSocket.getInputStream())); 

        sentence = inFromUser.readLine(); 

        outToServer.writeBytes(sentence + '\n'); 

        modifiedSentence = inFromServer.readLine(); 

        System.out.println("FROM SERVER: " + modifiedSentence); 

        clientSocket.close(); 
                   
    } 
} 
*/