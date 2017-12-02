package handlers;

import connection.PeerProcess;
import msgSenders.HaveRunnable;
import msgSenders.NotInterestedRunnable;
import msgSenders.RequestRunnable;
import setup.Config;
import setup.Neighbor;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by cyonkee on 12/1/17.
 */
public class PieceHandler {
    private byte[] payload;
    private PeerProcess peer;
    private Config attributes;
    private Neighbor thisPeer;

    public PieceHandler(byte[] payload, PeerProcess peer){
        this.payload = payload;
        this.peer = peer;
        attributes = peer.getAttributes();
        thisPeer = (Neighbor) peer.getMap().get(peer.getPeerID());
    }

    public void handle(String neighborID, BufferedOutputStream out){
        updateFile();
        updateBitfield();

        //send to all peers with sockets open
        HashMap map = peer.getMap();
        Neighbor neighbor;
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            neighbor = (Neighbor) pair.getValue();

            if((neighbor.getSocket() != null) && (!pair.getKey().equals(peer.getPeerID()))) {
                HaveRunnable haveSender = new HaveRunnable("haveSender", out, peer,  (String) pair.getKey(), payload);
                haveSender.start();
            }
        }

        decideWhatToDoWithPieceSender(neighborID, out);

        HashMap<String, Neighbor> neighbors = peer.getMap();
        sendNotInterestedToAppropriateNeighbors(neighbors, neighborID);
    }

    private void updateFile() {
        //get piece index
        byte[] pieceIndex = new byte[4];
        for(int i=0; i<4; i++)
            pieceIndex[i] = payload[i];

        //get offset
        String index = new String(pieceIndex);
        int piece = Integer.parseInt(index);
        int offset = piece * attributes.getPieceSize();

        //get bytes of file
        byte[] filepiece = new byte[payload.length - 4];
        for(int i=0; i<payload.length - 4; i++)
            filepiece[i] = payload[i+4];

        try {
            synchronized (this) {
                RandomAccessFile raf = new RandomAccessFile("peer_" + peer.getPeerID() + "/" + attributes.getFileName(), "rw");
                raf.seek(offset);
                raf.write(filepiece);
                raf.close();
            }
        } catch(IOException e){
            e.printStackTrace();
        }

//        writeDownloadPieceLog(piece);
//        if(bitfield.cardinality() == numOfPieces)
//            writeFullFileDownloadLog();
    }

    private void updateBitfield() {
        //get piece index
        byte[] pieceIndex = new byte[4];
        for(int i=0; i<4; i++)
            pieceIndex[i] = payload[i];

        String index = new String(pieceIndex);
        int piece = Integer.parseInt(index);

        synchronized (this) {
            //set appropriate bit in bitfield
            thisPeer.getBitfield().set(piece);
        }
    }

    private void decideWhatToDoWithPieceSender(String neighborID, BufferedOutputStream out) {
        Neighbor n = (Neighbor) peer.getMap().get(neighborID);
        // if interested request another piece, if not send not interested
        if (isInterested((n))) {
            RequestRunnable requestSender = new RequestRunnable("requestSender", out, peer, neighborID);
            requestSender.start();
        } else {
            NotInterestedRunnable notInterestedSender = new NotInterestedRunnable("not interested", out, peer);
            notInterestedSender.start();
        }
    }

    private void sendNotInterestedToAppropriateNeighbors(HashMap<String, Neighbor> neighbors, String neighborID) {
        // if peer has the whole file, send not interested to all neighbors
        boolean hasFullFile = thisPeer.getBitfield().cardinality() == peer.getAttributes().getNumOfPieces();

        // loop through neighbors to see if you need to send not interested
        for (Map.Entry<String, Neighbor> entry : neighbors.entrySet()) {
            String nID = entry.getKey();
            Neighbor thisNeighbor = entry.getValue();

            if ((thisNeighbor.getSocket() != null) && (nID != neighborID) && (nID != peer.getPeerID())) {
                // if you have the full file send not interested to all other neighbors
                if (hasFullFile) {
                    NotInterestedRunnable notInterestedSender = new NotInterestedRunnable("not interested", thisNeighbor.getOutputStream(), peer);
                    notInterestedSender.start();
                } else {
                    // check if you are no longer interested in neighbor, if not send not interested
                    if (!isInterested(thisNeighbor)) {
                        NotInterestedRunnable notInterestedSender = new NotInterestedRunnable("not interested", thisNeighbor.getOutputStream(), peer);
                        notInterestedSender.start();
                    }
                }
            }
        }
    }

    private boolean isInterested (Neighbor n) {
        int numOfPieces = attributes.getNumOfPieces();

        synchronized (this) {
            for (int i = 0; i < numOfPieces; i++) {
                if (thisPeer.getBitfield().get(i) == false && n.getBitfield().get(i) == true) {
                    return true;
                }
            }
            return false;
        }
    }
}
