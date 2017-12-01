import java.net.*;
import java.time.LocalDateTime;
import java.util.*;
import java.io.*;
import java.lang.*;

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
            MessageProtocol m = new MessageProtocol(isClient,peer,neighborID,in,out);

            //Testing connections
            System.out.println("Connected as Client: " + m.getIsClient() + " With neighbor: " + m.getNeighborID());

            m.doClientMessage();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void writeClientConnLog(PrintWriter logWriter,PeerProcess peer,String neighborID){
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        int hour = now.getHour();
        int minute = now.getMinute();
        int second = now.getSecond();
        String output = month+"/"+day+"/"+year+" "+hour+":"+minute+":"+second+": ";
        output += "Peer "+peer.getPeerID()+" makes connection to Peer "+neighborID+".";
        logWriter.println(output);
        logWriter.flush();
    }
}
