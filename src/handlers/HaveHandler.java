package handlers;

import connection.PeerProcess;
import msgSenders.InterestedRunnable;
import msgSenders.NotInterestedRunnable;
import setup.Config;
import setup.Neighbor;

import java.io.BufferedOutputStream;
import java.util.BitSet;

/**
 * Created by cyonkee on 12/1/17.
 */
public class HaveHandler {
    private byte[] payload;
    private PeerProcess peer;
    private Config attributes;
    private Neighbor thisPeer;

    public HaveHandler(byte[] payload, PeerProcess peer) {
        this.payload = payload;
        this.peer = peer;
        attributes = peer.getAttributes();
        thisPeer = (Neighbor) peer.getMap().get(peer.getPeerID());
    }

    public void handle(String neighborID, BufferedOutputStream out){
        updateNeighborBitfield(neighborID);
        boolean interested = findPiece();
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
        Neighbor neighbor = (Neighbor) peer.getMap().get(neighborID);
        BitSet neighborBitfield = neighbor.getBitfield();
        String s = new String(payload);
        int piece = Integer.valueOf(s);
        neighborBitfield.set(piece);
    }

    public boolean findPiece() {
        String s = new String(payload);
        int piece = Integer.valueOf(s);
        if (thisPeer.getBitfield().get(piece) == false) {
            return true;
        }
        return false;
    }

}
