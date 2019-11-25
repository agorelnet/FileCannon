package filecannon.server;

import filecannon.FileProcessor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server extends Thread {
    private ServerSocket servSock;
    private ArrayList<ClientHandler> handlers;
    private boolean isActive;

    /**
     * creates a new server
     * @param port the port to bind to
     * @throws IOException if there is an issue binding to the port
     */
    public Server(int port) throws IOException {
        this.servSock = new ServerSocket(port);
        this.handlers = new ArrayList();
        this.isActive = true;
    }

    /**
     * create a server with port 4230
     * @throws IOException if there is an issue binding to port 4230
     */
    public Server() throws IOException {
        this(4230);
    }

    @Override
    public void run() {
        Socket nextConnection = new Socket();
        ClientHandler nextHandler = new ClientHandler();
        FileProcessor currentProcessor;
        while(this.isActive) {
            try {
                nextConnection = this.servSock.accept();
                nextHandler = new ClientHandler(nextConnection);
                nextHandler.start();
            }

            catch(IOException e) {
                if(!this.isActive) {
                    continue;
                }
                System.err.printf("Error initializing connection with %s.\n", nextConnection.getInetAddress().getHostAddress());
                continue;
            }

            catch(Exception e) {}
            if(this.handlers.size() > 1) {
                for(int i = 0; i < this.handlers.size() - 1; i++) {
                    if(!this.handlers.get(i).isActive()) {
                        this.handlers.remove(i--);
                    }
                }
            }
        }
        this.close();
    }

    /**
     * closes the server
     */
    public void close() {
        this.isActive = false;
        try {
            this.servSock.close();
        }

        catch(IOException e) {
            System.err.println("Error closing server socket.\n");
        }
        for(ClientHandler eachHandler : this.handlers) {
            eachHandler.close();
        }
        this.interrupt();
    }
}
