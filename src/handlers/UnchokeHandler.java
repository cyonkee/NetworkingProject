package handlers;

import connection.Helper;
import connection.PeerProcess;
import msgSenders.RequestRunnable;
import setup.Config;
import setup.Neighbor;

import java.io.BufferedOutputStream;
import java.io.PrintWriter;

/**
 * Created by cyonkee on 12/1/17.
 */
public class UnchokeHandler {
    private PeerProcess peer;
    private Config attributes;
    private Neighbor thisPeer;

    public UnchokeHandler(PeerProcess peer){
        this.peer = peer;
        attributes = peer.getAttributes();
        thisPeer = (Neighbor) peer.getMap().get(peer.getPeerID());
    }

    public void handle(String neighborID, BufferedOutputStream out){
        peer.clearDownloadRate(neighborID);
        writeReceivedUnchokeLog(peer.getLogWriter(), peer, neighborID);
        RequestRunnable requestSender = new RequestRunnable("requestSender", out, peer, neighborID);
        requestSender.start();
    }

    private void writeReceivedUnchokeLog(PrintWriter logWriter, PeerProcess peer, String neighborID){
        String output;
        Helper helper = new Helper();
        output = helper.getCurrentTime();
        output += "Peer " + peer.getPeerID() + " is unchoked by " + neighborID + ".";
        logWriter.println(output);
        logWriter.flush();
    }
}
