package filecannon;

import filecannon.client.Client;
import filecannon.server.Server;

import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
	    String usage = "java -jar FileCannon.jar [-c | -s | -u]";
        // if no args, start a headless server on 4230
        if(args.length == 0) {
            Server serv;
            try {
                serv = new Server(4230);
                serv.start();
                while(serv.isAlive()) {}
                return;
            }

            catch(IOException e) {
                System.err.println("Error starting headless server on port 4230.");
                return;
            }

        }
        // if invalid args,
	    if(args.length != 1) {
            System.out.println(usage);
            return;
        }
        // check if we run client or server
	    char flag1 = 0;
	    try {
	        // this exception is just used to trip the catch block afterwards if the arguments are invalid
	        if(args[0].charAt(0) != '-') {
	            throw new Exception();
            }

            flag1 = args[0].charAt(1);
        }

	    catch(Exception e) {
            System.out.println(usage);
            return;
        }

	    switch(flag1) {
            case 'c':
                runClient();
                break;
            case 's':
                runServer();
                break;
            case 'u':
                runUpdate();
                break;
            default:
                System.out.println(usage);
                return;
        }
    }
    // prompts for parameters and runs the client
    public static void runClient() {
        Scanner s = new Scanner(System.in);
        System.out.printf("Server address to connect to: ");
        String server = s.nextLine();
        System.out.printf("Port [4230]: ");
        String portstring = s.nextLine();
        if("".equals(portstring))
            portstring = "4230";
        System.out.printf("File to send: ");
        String filename = s.nextLine();

        int port;

        try {
            port = Integer.parseInt(portstring);
        }

        catch(Exception e) {
            System.out.printf("Port number, \"%s\", is invalid.\n", portstring);
            return;
        }

        Socket sock;
        Client client = null;

        try {
            sock = new Socket(server, port);
            client = new Client(sock, filename);
        }

        catch(UnknownHostException e) {
            System.out.printf("Hostname, \"%s\", is unknown to me.\n");
            return;
        }

        catch(IOException e) {
            System.err.printf("Error establishing socket.\n");
            return;
        }

        client.start();


    }
    // prompts for parameters and runs the server
    public static void runServer() {
        Scanner s = new Scanner(System.in);
        System.out.printf("Port [4230]: ");
        String portstring = s.nextLine();

        if("".equals(portstring))
            portstring = "4230";

        int port;

        try {
            port = Integer.parseInt(portstring);
        }

        catch(Exception e) {
            System.out.printf("Invalid port number: \"%s\".\n", portstring);
            return;
        }

        Server server;

        try {
            server = new Server(port);
        }

        catch(IOException e) {
            System.err.printf("There was an issue starting the server. Do you have permission on this port? :(\n");
            return;
        }

        server.start();
        System.out.println("Type \"exit\" at any time to exit.");
        String line;
        while(server.isAlive()) {
            line = s.nextLine();
            if("exit".equals(line)) {
                server.close();
            }
        }
    }

    /**
     * sends an update to the server, is the same as sending the current jarfile to the server
     */
    public static void runUpdate() {
        Scanner s = new Scanner(System.in);
        String hostname, portstring;
        System.out.printf("Hostname to connect to: ");
        hostname = s.nextLine();
        System.out.printf("Port [4230]: ");
        portstring = s.nextLine();
        if("".equals(portstring))
            portstring = "4230";
        int port = -1;
        try {
            port = Integer.parseInt(portstring);
        }
        catch(Exception e) {
            System.out.printf("Invalid port number: %s. Exiting.\n", portstring);
        }

        Client client;
        try {
            Socket sock = new Socket(hostname, port);
            // set the client to use the filename of the jar that executed this class
            String absolutePath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            String filename = absolutePath.substring(absolutePath.lastIndexOf('/') + 1);
            // assuming the jar name is unchanged, filename should be FileCannon.jar. If the jarfile name is changed between installs, this will break, but can be fixed by making sure both the client and the server have the same name for their jarfile. This could be purposefully used to add security
            System.out.println(filename);
            client = new Client(sock, filename);
        }
        catch(IOException e) {
            System.out.printf("Invalid hostname: %s.\n", hostname);
            return;
        }
        catch(URISyntaxException e) {
            System.err.println("URISyntaxException: try renaming the file or moving it to a new location.");
            return;
        }
        client.start();
    }
}
