package connection;

import handlers.HandshakeProtocol;
import handlers.ListenerRunnable;
import msgSenders.BitfieldRunnable;
import setup.Neighbor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.BitSet;
import java.util.HashMap;

/**
 * Created by cyonkee on 10/22/17.
 */
public class Client {
    private Socket socket = null;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private boolean isClient = true;

    public Client(Socket socket) throws IOException{
        this.socket = socket;
    }

    public void startConnection(PeerProcess peer) {
        try{
            out = new BufferedOutputStream(socket.getOutputStream());
            out.flush();
            in = new BufferedInputStream(socket.getInputStream());

            HandshakeProtocol handshake = new HandshakeProtocol(isClient,peer.getPeerID(),in,out);
            String neighborID = handshake.getNeighborID();
            PrintWriter logWriter = peer.getLogWriter();
            writeClientConnLog(logWriter,peer,neighborID);

            HashMap map = peer.getMap();
            Neighbor n = (Neighbor) map.get(neighborID);
            n.setSocket(socket);
            n.setOutputStream(out);

            ListenerRunnable listener = new ListenerRunnable("clientlistener", in, out, peer, neighborID);
            listener.start();

            Neighbor thisPeer = (Neighbor) map.get(peer.getPeerID());
            BitSet myBitfield = thisPeer.getBitfield();
            if(myBitfield.cardinality() > 0) {
                BitfieldRunnable bitfieldSender = new BitfieldRunnable("bitfieldSender", out, peer);
                bitfieldSender.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void writeClientConnLog(PrintWriter logWriter, PeerProcess peer, String neighborID){
        String output;
        Helper helper = new Helper();
        output = helper.getCurrentTime();
        output += "Peer " + peer.getPeerID() + " makes a connection to Peer " + neighborID + ".";
        logWriter.println(output);
        logWriter.flush();
    }
}
