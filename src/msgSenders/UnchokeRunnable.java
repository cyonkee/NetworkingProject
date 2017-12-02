package msgSenders;

import connection.PeerProcess;
import setup.Config;
import setup.Neighbor;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.BitSet;

/**
 * Created by cyonkee on 12/1/17.
 */
public class UnchokeRunnable implements Runnable {
    private Thread t;
    private String name;
    private BufferedOutputStream out;
    private PeerProcess peer;
    private Config attributes;
    private BitSet myBitfield;
    private String neighborID;
    private Neighbor thisPeer;

    public UnchokeRunnable(String name, BufferedOutputStream out, PeerProcess peer, String neighborID){
        this.name = name;
        this.out = out;
        this.peer = peer;
        this.neighborID = neighborID;
        attributes = peer.getAttributes();
        thisPeer = (Neighbor) peer.getMap().get(peer.getPeerID());
        myBitfield = (BitSet) thisPeer.getBitfield();
    }

    public void start(){
        t = new Thread(this, name);
        t.run();
    }

    @Override
    public void run() {
        try {
            byte[] output = formUnchokeMessage();
            Neighbor neighbor = (Neighbor) peer.getMap().get(neighborID);
            BufferedOutputStream os = neighbor.getOutputStream();
            System.out.println("sent unchoke");
            synchronized (this) {
                os.write(output);
                os.flush();
            }
        } catch(IOException e){
            e.printStackTrace();
        }

    }

    private byte[] formUnchokeMessage() {
        byte[] output = new byte[5];
        String lengthMsg = "0001";
        byte[] lengthMsgBytes = lengthMsg.getBytes();
        for(int i=0; i<4; i++)
            output[i] = lengthMsgBytes[i];

        String type = "1";
        byte[] typeBytes = type.getBytes();
        output[4] = typeBytes[0];

        return output;
    }
}
