package handlers;

import connection.PeerProcess;
import msgSenders.HaveRunnable;
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
            RandomAccessFile raf = new RandomAccessFile("peer_" + peer.getPeerID() + "/" + attributes.getFileName(), "rw");
            raf.seek(offset);
            raf.write(filepiece);
            raf.close();

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

        //set appropriate bit in bitfield
        thisPeer.getBitfield().set(piece);
    }
}
