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

    public void startConnection(PeerProcess peer) {
        try{
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            HandshakeProtocol handshake = new HandshakeProtocol(isClient,peer.getPeerID(),in,out);
            String neighborID = handshake.getNeighborID();

            HashMap map = peer.getMap();
            Neighbor n = (Neighbor) map.get(neighborID);
            MessageProtocol m = new MessageProtocol(isClient,peer,neighborID,in,out);
            n.setConnection(m);

            //Testing connections
            System.out.println("Connected as Client: " + m.getIsClient() + " With neighbor: " + m.getNeighborID());

            m.doClientMessage();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
