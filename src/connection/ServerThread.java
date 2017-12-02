package connection; /**
 * Created by cyonkee on 10/22/17.
 */
import msgSenders.*;
import setup.*;
import handlers.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
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

            //connection.MessageProtocol m = new connection.MessageProtocol(isClient,peer,neighborID,in,out);
            //Testing connections
            //System.out.println("Connected as connection.Client: " + m.getIsClient() + " With neighbor: " + m.getNeighborID());
            //m.doServerMessage();

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
