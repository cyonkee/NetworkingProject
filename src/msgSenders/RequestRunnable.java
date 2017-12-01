package msgSenders;

import connection.PeerProcess;
import setup.Config;
import setup.Neighbor;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;

/**
 * Created by cyonkee on 12/1/17.
 */
public class RequestRunnable implements Runnable {
    private Thread t;
    private String name;
    private BufferedOutputStream out;
    private PeerProcess peer;
    private Config attributes;
    private BitSet myBitfield;
    private String neighborID;

    public RequestRunnable(String name, BufferedOutputStream out, PeerProcess peer, String neighborID) {
        this.name = name;
        this.out = out;
        this.peer = peer;
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
            byte[] output = formRequestMessage();

            System.out.println("sent request");
            out.write(output);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] formRequestMessage() {
        int randomIndex = chooseRandomMissingPiece();
        if(randomIndex == -1) return null;

        byte[] output = new byte[9];
        String lengthMsg = "0005";
        String type = "6";
        String pieceIndex = Integer.toString(randomIndex);
        pieceIndex = padLeft(pieceIndex,4);
        byte[] lengthMsgBytes = lengthMsg.getBytes();
        byte[] typeBytes = type.getBytes();
        byte[] pieceIndexBytes = pieceIndex.getBytes();

        //msg length
        for(int i=0; i<4; i++)
            output[i] = lengthMsgBytes[i];

        //msg type
        output[4] = typeBytes[0];

        //payload (piece being requested)
        for(int i=0; i<4; i++)
            output[i+5] = pieceIndexBytes[i];

        return output;
    }

    private int chooseRandomMissingPiece(){
        int bitFieldLength = myBitfield.length();
        int numOfPieces = peer.getAttributes().getNumOfPieces();
        Neighbor neighbor = (Neighbor) peer.getMap().get(neighborID);

        //Fill list with missing pieces indices
        ArrayList<Integer> indicesOfMissingPieces = new ArrayList<Integer>();
        boolean indexAdded = false;
        if(bitFieldLength == 0){
            for(int i=0; i<numOfPieces; i++) {
                indicesOfMissingPieces.add(i);
                indexAdded = true;
            }
        }
        else{
            for(int i=0; i<numOfPieces; i++){
                if(!myBitfield.get(i) && neighbor.getBitfield().get(i)) {
                    indicesOfMissingPieces.add(i);
                    indexAdded = true;
                }
            }
        }

        //Randomly select index from missing
        if (!indexAdded) {
            return -1;
        }
        else {
            Random random = new Random();
            int randomIndex = random.nextInt(indicesOfMissingPieces.size());
            return indicesOfMissingPieces.get(randomIndex);
        }
    }

    private String padLeft(String s, int length) {
        return String.format("%" + length + "s", s).replace(' ','0');
    }
}