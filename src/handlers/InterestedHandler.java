package handlers;

import connection.Helper;
import connection.PeerProcess;
import setup.Config;
import setup.Neighbor;

import java.io.PrintWriter;

/**
 * Created by cyonkee on 12/1/17.
 */
public class InterestedHandler {
    private PeerProcess peer;
    private Config attributes;
    private Neighbor thisPeer;

    public InterestedHandler(PeerProcess peer){
        this.peer = peer;
        attributes = peer.getAttributes();
        thisPeer = (Neighbor) peer.getMap().get(peer.getPeerID());
    }

    public void handle(String neighborID){
        writeReceivedInterestedLog(peer.getLogWriter(), peer, neighborID);
        Neighbor neighbor = (Neighbor) peer.getMap().get(neighborID);
        neighbor.setIsInterested(true);
    }

    private void writeReceivedInterestedLog(PrintWriter logWriter, PeerProcess peer, String neighborID){
        String output;
        Helper helper = new Helper();
        output = helper.getCurrentTime();
        output += "Peer " + peer.getPeerID() + " received the 'interested' message from " + neighborID + ".";
        logWriter.println(output);
        logWriter.flush();
    }
}
