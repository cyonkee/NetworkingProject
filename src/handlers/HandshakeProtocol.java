package handlers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Created by cyonkee on 10/23/17.
 */
public class HandshakeProtocol {
    private boolean isClient;
    private String peerID;
    private String neighborID;
    private BufferedInputStream in;
    private BufferedOutputStream out;

    public HandshakeProtocol(boolean isClient, String peerID, BufferedInputStream in, BufferedOutputStream out) throws IOException, ClassNotFoundException {
        this.isClient = isClient;
        this.peerID = peerID;
        this.out = out;
        this.in = in;

        if(isClient)
            doClientHandshake();
        else if(!isClient)
            doServerHandshake();
    }

    private void doClientHandshake() throws IOException, ClassNotFoundException {
        sendHandshakeMessage();
        receiveHandshakeMessage();
    }

    private void doServerHandshake() throws IOException, ClassNotFoundException {
        receiveHandshakeMessage();
        sendHandshakeMessage();
    }

    //Create 32 byte handshake message and send
    private void sendHandshakeMessage() throws IOException {
        byte[] handshakeMessage = new byte[32];
        String header = "P2PFILESHARINGPROJ";
        BitSet bitfield = new BitSet(80);
        byte[] headerBytes = header.getBytes();
        byte[] bitfieldBytes = Arrays.copyOf(bitfield.toByteArray(),10);
        byte[] peerIdBytes = peerID.getBytes();

        for(int i=0; i<10; i++){
            if(bitfieldBytes[i] == 0)
                bitfieldBytes[i] = (byte) '0';
        }

        for(int i=0; i < 18; i++)
            handshakeMessage[i] = headerBytes[i];

        for(int i=0; i < 10; i++)
            handshakeMessage[i+18] = bitfieldBytes[i];

        for(int i=0; i < 4; i++)
            handshakeMessage[i+28] = peerIdBytes[i];

        out.write(handshakeMessage);
        out.flush(); //must flush each time a message is written to stream
    }

    private void receiveHandshakeMessage() throws IOException, ClassNotFoundException {
        byte[] input = new byte[32];
        in.read(input,0,32);

        byte[] neighbor = new byte[4];
        for(int i=0; i<4; i++){
            neighbor[i] = input[i+28];
        }

        String s = new String(input);
        System.out.println(s);

        String neighborID = new String(neighbor);
        setNeighborID(neighborID);
    }

    public void setNeighborID(String neighborID){ this.neighborID = neighborID; }
    public String getNeighborID(){ return neighborID; }
}
