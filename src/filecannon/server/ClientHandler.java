package filecannon.server;

import filecannon.FileProcessor;
import filecannon.client.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket connection;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean isActive;

    /**
     * Instantiates a placeholder handler.
     */
    public ClientHandler() {
        this.connection = null;
        this.dis = null;
        this.dos = null;
        this.isActive = false;
    }

    /**
     * Creates a new client handler
     * @param connection The socket connecting to the client
     * @throws IOException If there's an issue opening the input and output streams
     */
    public ClientHandler(Socket connection) throws IOException {
        this.connection = connection;
        this.dis = new DataInputStream(this.connection.getInputStream());
        this.dos = new DataOutputStream(this.connection.getOutputStream());
        this.isActive = true;
    }

    /**
     * Writes a line to the socket
     * @param s The line to send
     * @throws IOException If there is an issue sending the data
     */
    private void writeLine(String s) throws IOException {
        this.dos.writeBytes(s.replace("\n", "") + "\n");
    }

    /**
     * Reads a line from the socket
     * @return The next line from the socket
     * @throws IOException If there is an issue reading the data
     */
    private String readLine() throws IOException {
        StringBuilder out = new StringBuilder();
        char current;
        while((current = (char) this.dis.readByte()) != '\n') {
            out.append(current);
        }
        return out.toString();
    }

    /**
     * Grabs a file from the client
     * @return A byte array representing the file
     * @throws IOException If there is an issue reading the data
     */
    private byte[] getFileContents() throws IOException {
        int filesize = 0;
        try {
            String line = this.readLine();
            filesize = Integer.parseInt(line);
        }
        catch(IOException e) {
            throw e;
        }

        catch(Exception e) {
            throw new IOException();
        }

        return dis.readNBytes(filesize);
    }

    @Override
    public void run() {
        if(!this.isActive) {
            return;
        }
        String filename = "";
        String eCheck;
        byte[] fileContents = new byte[0];
        try {
            filename = this.readLine();
            this.writeLine(filename);
            eCheck = this.readLine();
            if(!"ok".equals(eCheck)) {
                throw new Exception();
            }
            fileContents = this.getFileContents();
        }
        catch(IOException e) {
            System.err.printf("Error establishing a connection with %s.\n", this.connection.getInetAddress().getHostAddress());
            this.close();
        }
        catch(Exception e) {
            System.err.printf("Invalid filename check received from %s.\n", this.connection.getInetAddress().getHostAddress());
            this.close();
        }
        FileProcessor fp = new FileProcessor(filename, fileContents);
        try {
            fp.write();
        }

        catch(IOException e) {
            System.err.printf("Error writing file from %s. Skipping.\n", this.connection.getInetAddress().getHostAddress());
        }
        // check if this is an update
        try {
            String absolutePath = ClientHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            String jarfile = absolutePath;
            if(filename.equals(jarfile)) {
                System.exit(0);
            }
        }
        catch(Exception e) {
            this.close();
            return;
        }

        this.close();
    }

    /**
     * Closes the handler
     */
    public void close() {
        this.isActive = false;
        try {
            this.dos.close();
            this.dis.close();
            this.connection.close();
            this.interrupt();
        }
        catch(IOException e) {
            System.err.printf("Error closing a connection with %s.\n", this.connection.getInetAddress().getHostAddress());
        }
    }

    /**
     * Checks if the handler is currently valid
     * @return True if valid, false otherwise
     */
    public boolean isActive() {
        return this.isActive;
    }
}
