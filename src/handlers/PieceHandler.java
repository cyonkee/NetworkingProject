package handlers;

import connection.PeerProcess;
import setup.Config;
import setup.Neighbor;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

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
//        HaveRunnable haveSender = new HaveRunnable("haveSender", out, peer, neighborID, payload);
//        haveSender.start();
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
