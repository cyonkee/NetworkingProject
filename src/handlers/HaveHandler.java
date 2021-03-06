package handlers;

import connection.Helper;
import connection.PeerProcess;
import msgSenders.InterestedRunnable;
import msgSenders.NotInterestedRunnable;
import setup.Config;
import setup.Neighbor;

import java.io.BufferedOutputStream;
import java.io.PrintWriter;
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
        synchronized (this) {
            Neighbor neighbor = (Neighbor) peer.getMap().get(neighborID);
            BitSet neighborBitfield = neighbor.getBitfield();
            String s = new String(payload);
            int piece = Integer.valueOf(s);
            neighborBitfield.set(piece);
            writeReceivedHaveLog(peer.getLogWriter(), peer, neighborID, piece);
        }
    }

    public boolean findPiece() {
        String s = new String(payload);
        int piece = Integer.valueOf(s);
        synchronized (this) {
            if (thisPeer.getBitfield().get(piece) == false) {
                return true;
            }
            return false;
        }
    }

    private void writeReceivedHaveLog(PrintWriter logWriter, PeerProcess peer, String neighborID, int piece){
        String output;
        Helper helper = new Helper();
        output = helper.getCurrentTime();
        output += "Peer " + peer.getPeerID() + " received the 'have' message from " + neighborID + " for the piece " + piece + ".";
        logWriter.println(output);
        logWriter.flush();
    }

}
