package handlers;

import connection.PeerProcess;
import setup.Config;
import setup.Neighbor;
import msgSenders.*;

import java.io.BufferedOutputStream;

/**
 * Created by cyonkee on 12/1/17.
 */
public class RequestHandler {
    private byte[] payload;
    private PeerProcess peer;
    private Config attributes;
    private Neighbor n;

    public RequestHandler(byte[] payload, PeerProcess peer){
        this.payload = payload;
        this.peer = peer;
        attributes = peer.getAttributes();
        n = (Neighbor) peer.getMap().get(peer.getPeerID());
    }

    public void handle(String neighborID, BufferedOutputStream out){
        PieceRunnable pieceSender = new PieceRunnable("pieceSender", out, peer, neighborID, payload);
        pieceSender.start();
    }
}
