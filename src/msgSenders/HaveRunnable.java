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
public class HaveRunnable implements Runnable {
    private Thread t;
    private String name;
    private BufferedOutputStream out;
    private PeerProcess peer;
    private Config attributes;
    private BitSet myBitfield;
    private byte[] payload;
    private String neighborID;

    public HaveRunnable(String name, BufferedOutputStream out, PeerProcess peer, String neighborID, byte[] payload) {
        this.name = name;
        this.out = out;
        this.peer = peer;
        this.payload = payload;
        this.neighborID = neighborID;
        attributes = peer.getAttributes();
        Neighbor thisPeer = (Neighbor) peer.getMap().get(peer.getPeerID());
        myBitfield = (BitSet) thisPeer.getBitfield();
    }

    public void start() {
        t = new Thread(this, name);
        t.run();
    }

    @Override
    public void run() {
        try {
            byte[] output = formHaveMessage();

            Neighbor neighbor = (Neighbor) peer.getMap().get(neighborID);
            BufferedOutputStream os = neighbor.getOutputStream();
            System.out.println("sent have");
            os.write(output);
            os.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] formHaveMessage() {
        byte[] output = new byte[9];
        String lengthMsg = "0005";
        String type = "4";
        byte[] lengthMsgBytes = lengthMsg.getBytes();
        byte[] typeBytes = type.getBytes();
        byte[] pieceIndexBytes = new byte[4];
        for(int i=0; i<4; i++)
            pieceIndexBytes[i] = payload[i];

        //msg length
        for(int i=0; i<4; i++)
            output[i] = lengthMsgBytes[i];

        //msg type
        output[4] = typeBytes[0];

        //payload (piece that was received)
        for(int i=0; i<4; i++)
            output[i+5] = pieceIndexBytes[i];

        return output;
    }
}