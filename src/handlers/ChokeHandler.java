package handlers;

import connection.Helper;
import connection.PeerProcess;
import setup.Config;
import setup.Neighbor;
import msgSenders.*;

import java.io.BufferedOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * Created by cyonkee on 12/1/17.
 */
public class ChokeHandler {
    private PeerProcess peer;
    private Config attributes;
    private Neighbor thisPeer;

    public ChokeHandler(PeerProcess peer){
        this.peer = peer;
        attributes = peer.getAttributes();
        thisPeer = (Neighbor) peer.getMap().get(peer.getPeerID());
    }

    public void handle(String neighborID, BufferedOutputStream out){
        writeReceivedChokeLog(peer.getLogWriter(), peer, neighborID);
    }

    private void writeReceivedChokeLog(PrintWriter logWriter, PeerProcess peer, String neighborID){
        String output;
        Helper helper = new Helper();
        output = helper.getCurrentTime();
        output += "Peer " + peer.getPeerID() + " is choked by " + neighborID + ".";
        logWriter.println(output);
        logWriter.flush();
    }
}
