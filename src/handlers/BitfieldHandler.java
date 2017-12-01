package handlers;

import connection.PeerProcess;
import msgSenders.*;
import setup.Config;
import setup.Neighbor;

import java.io.BufferedOutputStream;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Created by cyonkee on 12/1/17.
 */
public class BitfieldHandler {
    private byte[] payload;
    private PeerProcess peer;
    private Config attributes;
    private Neighbor n;

    public BitfieldHandler(byte[] payload, PeerProcess peer){
        this.payload = payload;
        this.peer = peer;
        attributes = peer.getAttributes();
        n = (Neighbor) peer.getMap().get(peer.getPeerID());
    }

    public void handle(String neighborID, BufferedOutputStream out){
        updateNeighborBitfield(neighborID);
        boolean interested = findPieces(payload);
        if (interested) {
            InterestedRunnable interestedSender = new InterestedRunnable("interested", out, peer);
            interestedSender.start();
        }
        else {
            NotInterestedRunnable notInterestedSender = new NotInterestedRunnable("not interested", out, peer);
            notInterestedSender.start();
        }
    }

    private void updateNeighborBitfield(String neighborID) {
        int numOfPieces = attributes.getNumOfPieces();
        BitSet bitfield = new BitSet(numOfPieces);
        for(int i=0; i<numOfPieces; i++) {
            if (payload[i] == 1)
                bitfield.set(i);
        }
        Neighbor neighbor = (Neighbor) peer.getMap().get(neighborID);
        neighbor.setBitfield(bitfield);
    }

    private boolean findPieces(byte[] payload) {
        int numOfPieces = attributes.getNumOfPieces();
        if (payload.length == 0) {
            payload = Arrays.copyOf(n.getBitfield().toByteArray(), numOfPieces);
        }
        for (int i = 0; i<numOfPieces; i++) {
            if (payload[i] == 1 && n.getBitfield().get(i) == false) {
                return true;
            }
        }
        return false;
    }
}
