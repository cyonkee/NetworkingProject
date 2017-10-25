import java.net.*;
import java.util.*;
import java.io.*;
import java.lang.*;

/**
 * Created by cyonkee on 10/22/17.
 */
public class Client {
    private Socket socket = null;
    private ObjectInputStream in;	//stream read from the socket
    private ObjectOutputStream out;    //stream write to the socket
    private boolean isClient = true;

    public Client(Socket socket) throws IOException{
        this.socket = socket;
    }

    //@Override
    public void startConnection(int serverPort, PeerProcess peer) {
        try{
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            HandshakeProtocol handshake = new HandshakeProtocol(isClient,peer.getPeerID(),in,out);

//            String fromServer;
//            String fromUser;
//            fromUser = "Hello Server at port " + serverPort;
//            System.out.println(fromUser);
//            out.println(fromUser);
//
//            fromServer = in.readLine();
//            System.out.println("Server: " + fromServer);
//
//            fromUser = "I am Client " + peer.getPeerID();
//            System.out.println(fromUser);
//            out.println(fromUser);
//            while ((fromServer = in.readLine()) != null) {
//                System.out.println("Server: " + fromServer);
//
//                fromUser = stdIn.readLine();
//                if (fromUser != null) {
//                    System.out.println("Client: " + fromUser);
//                    out.println(fromUser);
//                }
//            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
