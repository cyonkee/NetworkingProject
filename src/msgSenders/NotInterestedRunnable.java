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
public class NotInterestedRunnable implements Runnable {
    private Thread t;
    private String name;
    private BufferedOutputStream out;
    private PeerProcess peer;
    private Config attributes;
    private BitSet myBitfield;

    public NotInterestedRunnable(String name, BufferedOutputStream out, PeerProcess peer){
        this.name = name;
        this.out = out;
        this.peer = peer;
        attributes = peer.getAttributes();
        Neighbor thisPeer = (Neighbor) peer.getMap().get(peer.getPeerID());
        myBitfield = (BitSet) thisPeer.getBitfield();
    }

    public void start(){
        t = new Thread(this, name);
        t.start();
    }

    @Override
    public void run() {
        try {
            synchronized (this) {
                byte[] output = formNotInterestedMessage();
                out.write(output);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] formNotInterestedMessage() {
        byte[] output = new byte[5];
        String lengthMsg = "0001";
        byte[] lengthMsgBytes = lengthMsg.getBytes();

        for(int i=0; i<4; i++)
            output[i] = lengthMsgBytes[i];

        String type = "3";
        byte[] typeBytes = type.getBytes();
        output[4] = typeBytes[0];

        return output;
    }
}
