import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    Socket requestSocket;
    ObjectOutputStream out;
    ObjectInputStream in;

    void run(int port) {
        try {
            requestSocket = new Socket("localhost", port);
            System.out.println("Connected to localhost on port " + port);
            Scanner scanner = new Scanner(System.in);

            // Initialize ObjectOutputStream once outside the loop
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());

            String command;
            do {
                System.out.println("Write 'get' or 'upload' with file name or 'c' to quit:");
                command = scanner.nextLine();

                if (command.startsWith("get")) {
                    // Send the get command to the server
                    sendCommand(command);
                    // Receive and process the file from the server
                    receiveFile(command.split(" ")[1]);
                    System.out.println("File received: " + command.split(" ")[1]);
                } else if (command.startsWith("upload")) {
                    // Send the upload command to the server
                    sendCommand(command);
                    // Upload the file to the server
                    sendFile(command.split(" ")[1]);
                    System.out.println("File uploaded: " + command.split(" ")[1]);
                } else if (!"c".equals(command)) {
                    // Send other commands to the server
                    sendCommand(command);
                    // Process the response from the server
                    processResponse();
                }
            } while (!"c".equals(command));
        } catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                requestSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void sendCommand(String command) {
        try {
            out.writeObject(command);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void receiveFile(String filename) {
        try (FileOutputStream fileOutputStream = new FileOutputStream("new_" + filename)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendFile(String filename) {
        try (FileInputStream fileInputStream = new FileInputStream(filename)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();

            // After sending the file, process the server's response
            processResponse();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    String processResponse() throws IOException, ClassNotFoundException {
        return (String) in.readObject();
    }

    public static void main(String[] args) {
        Client client = new Client();
        int port = Integer.parseInt(args[0]);
        client.run(port);
    }
}
