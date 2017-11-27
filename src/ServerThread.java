/**
 * Created by cyonkee on 10/22/17.
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

/*
Using an ObjectInputStream and ObjectOutputStream for transferring messages.
 */
public class ServerThread extends Thread {
    private Socket socket = null;
    private ObjectInputStream in;	//stream read from the socket
    private ObjectOutputStream out;    //stream write to the socket
    private boolean isClient = false;
    private PeerProcess peer;

    public ServerThread(Socket socket, PeerProcess peer) throws IOException {
        super("ServerThread");
        this.socket = socket;
        this.peer = peer;
    }

    //run() method is called on when .start() is
    //invoked (in TCPConnection class startServer() method) to start the thread.
    @Override
    public void run() {
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

            m.doServerMessage();

            //socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
