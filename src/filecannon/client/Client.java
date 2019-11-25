package filecannon.client;

import filecannon.FileProcessor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client extends Thread {
    private Socket connection;
    private String filename;
    private boolean isActive;
    private DataOutputStream dos;
    private DataInputStream dis;

    /**
     * Makes a placeholder client
     */
    public Client() {
        this.connection = null;
        this.dis = null;
        this.dos = null;
        this.isActive = false;
        this.filename = "";
    }

    /**
     * Creates a new client thread
     * @param connection The socket connected to the server
     * @param filename The name of the file to send
     * @throws IOException If there is an issue opening the io streams
     */
    public Client(Socket connection, String filename) throws IOException {
        this.connection = connection;
        this.filename = filename;
        this.isActive = true;
        this.dis = new DataInputStream(this.connection.getInputStream());
        this.dos = new DataOutputStream(this.connection.getOutputStream());
    }

    /**
     * writes a line to the server
     * @param s the line to write
     * @throws IOException if there is an issue writing the data
     */
    private void writeLine(String s) throws IOException {
        this.dos.writeBytes(s.replace("\n", "") + "\n");
    }

    /**
     * reads a line of data from the server
     * @return the data read
     * @throws IOException if there is an issue reading the data
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
     * closes the thread
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
            System.err.printf("Error closing connection to %s.\n", this.connection.getInetAddress().getHostAddress());
        }
    }
    @Override
    public void run() {
        if(!this.isActive) {
            return;
        }

        try {
            this.writeLine(this.filename.substring(this.filename.lastIndexOf('/') + 1));
            String check = this.readLine();
            if(this.filename.equals(check)) {
                this.writeLine("ok");
            }
            else {
                this.writeLine("err");
                this.close();
                return;
            }
        }
        catch(IOException e) {
            System.err.printf("Error initializing connection with %s. Exiting.\n", this.connection.getInetAddress().getHostAddress());
            this.close();
            return;
        }
        FileProcessor fp;
        try {
            fp = new FileProcessor(filename);
            byte[] data = fp.getFileContents();
            this.writeLine(String.format("%d", data.length));
            this.dos.write(data);
            System.out.printf("Wrote file: %s\n", filename);
        }
        catch(IOException e) {
            System.err.printf("Error sending the file to %s. Exiting.\n", this.connection.getInetAddress().getHostAddress());
        }
        this.close();
    }


}
