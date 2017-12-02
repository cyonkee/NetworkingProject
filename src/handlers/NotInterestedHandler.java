package handlers;

import connection.PeerProcess;
import setup.Config;
import setup.Neighbor;

/**
 * Created by cyonkee on 12/1/17.
 */
public class NotInterestedHandler {
    private PeerProcess peer;
    private Config attributes;
    private Neighbor thisPeer;

    public NotInterestedHandler(PeerProcess peer){
        this.peer = peer;
        attributes = peer.getAttributes();
        thisPeer = (Neighbor) peer.getMap().get(peer.getPeerID());
    }

    public void handle(String neighborID){
        Neighbor neighbor = (Neighbor) peer.getMap().get(neighborID);
        neighbor.setIsInterested(false);
    }
}
