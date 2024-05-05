import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        System.out.println("The server is running on port " + port);
        try (ServerSocket listener = new ServerSocket(port)) {
            int clientNum = 1;
            while (true) {
                Socket connection = listener.accept();
                System.out.println("Client " + clientNum + " connected from " + connection.getInetAddress() + ":" + connection.getPort());
                new Handler(connection, clientNum).start();
                clientNum++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Handler extends Thread {
        private Socket connection;
        private int clientNum;

        public Handler(Socket connection, int clientNum) {
            this.connection = connection;
            this.clientNum = clientNum;
        }

        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream())) {

                System.out.println("Handler for Client " + clientNum + " started.");

                String command;
                while (true) {
                    command = (String) in.readObject();
                    System.out.println("Received command from Client " + clientNum + ": " + command);
                    if ("c".equals(command)) {
                        System.out.println("Client " + clientNum + " has ended the connection.");
                        break;
                    }
                    if (command.startsWith("get")) {
                        String filename = command.split(" ")[1];
                        System.out.println("Client " + clientNum + " requested file: " + filename);
                        sendFile(filename, out);
                    } else if (command.startsWith("upload")) {
                        String filename = command.split(" ")[1];
                        System.out.println("Client " + clientNum + " uploading file: " + filename);
                        receiveFile(filename, connection.getInputStream());
                    } else {
                        System.out.println("Unknown command from client " + clientNum);
                    }
                    out.writeObject("Command processed"); // Send response to client
                    out.flush();
                }
            } catch (IOException | ClassNotFoundException e) {
                //e.printStackTrace();
            } finally {
                try {
                    connection.close();
                    System.out.println("Handler for Client " + clientNum + " closed.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendFile(String filename, ObjectOutputStream out) {
            try (FileInputStream fileInputStream = new FileInputStream(filename)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void receiveFile(String filename, InputStream inputStream) {
            try (FileOutputStream fileOutputStream = new FileOutputStream("new_" + filename)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
