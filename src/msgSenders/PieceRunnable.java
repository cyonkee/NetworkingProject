package msgSenders;

import connection.PeerProcess;
import setup.Config;
import setup.Neighbor;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;

/**
 * Created by cyonkee on 12/1/17.
 */
public class PieceRunnable implements Runnable {
    private Thread t;
    private String name;
    private BufferedOutputStream out;
    private PeerProcess peer;
    private Config attributes;
    private BitSet myBitfield;
    private String neighborID;
    private byte[] payload;

    public PieceRunnable(String name, BufferedOutputStream out, PeerProcess peer, String neighborID, byte[] payload) {
        this.name = name;
        this.out = out;
        this.peer = peer;
        this.neighborID = neighborID;
        this.payload = payload;
        attributes = peer.getAttributes();
        Neighbor thisPeer = (Neighbor) peer.getMap().get(peer.getPeerID());
        myBitfield = (BitSet) thisPeer.getBitfield();
    }

    public void start() {
        t = new Thread(this, name);
        t.run();
    }

    @Override
    public void run() {
        try {
            byte[] output = formPieceMessage();

            System.out.println("sent piece");
            out.write(output);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] formPieceMessage() {
        //get pieceIndex and size of piece
        String piece = new String(payload);
        int pieceIndex = Integer.parseInt(piece);
        int offset = pieceIndex * attributes.getPieceSize();
        int thisPieceSize;
        if(pieceIndex == attributes.getNumOfPieces() - 1)
            thisPieceSize = attributes.getLastPieceSize();
        else
            thisPieceSize = attributes.getPieceSize();

        //get piece of file. If last piece, the byte[] array is smaller.
        byte[] pieceOfFile = new byte[thisPieceSize];
        try {
            RandomAccessFile raf = new RandomAccessFile("peer_" + peer.getPeerID() + "/" + attributes.getFileName(), "rw");
            raf.seek(offset);
            raf.readFully(pieceOfFile);
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //get msg values
        byte[] output = new byte[1 + 4 + 4 + thisPieceSize];
        String lengthMsg = Integer.toString(1 + 4 + thisPieceSize);
        lengthMsg = padLeft(lengthMsg,4);
        String type = "7";
        byte[] lengthMsgBytes = lengthMsg.getBytes();
        byte[] typeBytes = type.getBytes();

        //msg length
        for(int i=0; i<4; i++)
            output[i] = lengthMsgBytes[i];

        //msg type
        output[4] = typeBytes[0];

        //msg payload (piece index and piece being sent)
        for(int i=0; i<4; i++)
            output[i+5] = payload[i];

        for(int i=0; i < thisPieceSize; i++)
            output[i+9] = pieceOfFile[i];

        peer.incrementDownloads(neighborID);

        return output;
    }

    private String padLeft(String s, int length) {
        return String.format("%" + length + "s", s).replace(' ','0');
    }
}