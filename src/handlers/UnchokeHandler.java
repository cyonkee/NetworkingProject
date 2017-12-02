package handlers;

import connection.PeerProcess;
import setup.Config;
import setup.Neighbor;
import msgSenders.*;

import java.io.BufferedOutputStream;

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
        RequestRunnable requestSender = new RequestRunnable("requestSender", out, peer, neighborID);
        requestSender.start();
    }
}
