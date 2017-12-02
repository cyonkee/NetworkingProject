package connection; /**
 * Created by cyonkee on 10/22/17.
 */
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

/*
Using an ObjectInputStream and ObjectOutputStream for transferring messages.
 */
public class ServerThread extends Thread {
    private Socket socket = null;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private boolean isClient = false;
    private PeerProcess peer;

    public ServerThread(Socket socket, PeerProcess peer) throws IOException {
        super("connection.ServerThread");
        this.socket = socket;
        this.peer = peer;
    }

    //run() method is called on when .start() is
    //invoked (in connection.TCPConnection class startServer() method) to start the thread.
    @Override
    public void run() {
        try{
            out = new BufferedOutputStream(socket.getOutputStream());
            out.flush();
            in = new BufferedInputStream(socket.getInputStream());

            HandshakeProtocol handshake = new HandshakeProtocol(isClient,peer.getPeerID(),in,out);
            String neighborID = handshake.getNeighborID();
            PrintWriter logWriter = peer.getLogWriter();
            writeServerConnLog(logWriter,peer,neighborID);

            HashMap map = peer.getMap();
            Neighbor n = (Neighbor) map.get(neighborID);
            n.setSocket(socket);
            n.setOutputStream(out);

            ListenerRunnable listener = new ListenerRunnable("serverlistener", in, out, peer, neighborID);
            listener.start();

            Neighbor thisPeer = (Neighbor) map.get(peer.getPeerID());
            BitSet myBitfield = thisPeer.getBitfield();
            if(myBitfield.cardinality() > 0) {
                BitfieldRunnable bitfieldSender = new BitfieldRunnable("bitfieldSender", out, peer);
                bitfieldSender.start();
            }


            //socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void writeServerConnLog(PrintWriter logWriter, PeerProcess peer, String neighborID){
        String output;
        Helper helper = new Helper();
        output = helper.getCurrentTime();
        output += "Peer "+peer.getPeerID()+" is connected from Peer "+neighborID+".";
        logWriter.println(output);
        logWriter.flush();
    }
}
