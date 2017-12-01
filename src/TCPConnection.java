import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created by cyonkee on 10/15/17.
 */

/*

 */
public class TCPConnection {
    private PeerProcess peer;
    private HashMap map;

    public TCPConnection(PeerProcess peer){
        this.peer = peer;
        map = peer.getMap();
    }

    public void startServer() {
        Neighbor thisPeer = (Neighbor) map.get(peer.getPeerID());
        boolean listening = true;
        int port = thisPeer.getPort();

        //SERVER SOCKET
        //keeps listening for new Client connections until the socket is closed
        //Creates ServerThread for each new connection
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server " + peer.getPeerID() + " listening on port " + port);
            while (listening) {
                new ServerThread(serverSocket.accept(),peer).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + port);
            System.exit(-1);
        }
    }
    
    public void startClient(Neighbor n){
        String hostName = n.getHostname();
        int port = n.getPort();

        //CLIENT SOCKET
        //Initiates connection to a server
        try
        {
            Socket clientSocket = new Socket(hostName, port);
            Client client = new Client(clientSocket);
            client.startConnection(peer);
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);

        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }
}
