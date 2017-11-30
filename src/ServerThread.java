/**
 * Created by cyonkee on 10/22/17.
 */
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;

/*
Using an ObjectInputStream and ObjectOutputStream for transferring messages.
 */
public class ServerThread extends Thread {
    private Socket socket = null;
    //private ObjectInputStream in;	//stream read from the socket
    //private ObjectOutputStream out;    //stream write to the socket
    private BufferedInputStream in;
    private BufferedOutputStream out;
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
            //out = new ObjectOutputStream(socket.getOutputStream());
            out = new BufferedOutputStream(socket.getOutputStream());
            out.flush();
            //in = new ObjectInputStream(socket.getInputStream());
            in = new BufferedInputStream(socket.getInputStream());

            HandshakeProtocol handshake = new HandshakeProtocol(isClient,peer.getPeerID(),in,out);
            String neighborID = handshake.getNeighborID();
            PrintWriter logWriter = peer.getLogWriter();
            writeServerConnLog(logWriter,peer,neighborID);

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

    private void writeServerConnLog(PrintWriter logWriter, PeerProcess peer, String neighborID){
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        int hour = now.getHour();
        int minute = now.getMinute();
        int second = now.getSecond();
        String output = month+"/"+day+"/"+year+" "+hour+":"+minute+":"+second+": ";
        output += "Peer "+peer.getPeerID()+" is connected from Peer "+neighborID+".";
        logWriter.println(output);
        logWriter.flush();
    }
}
