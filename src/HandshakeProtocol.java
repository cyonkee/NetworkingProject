import java.io.*;
import java.util.*;

/**
 * Created by cyonkee on 10/23/17.
 */
public class HandshakeProtocol {
    private boolean isClient;
    private String peerID;
    private ObjectInputStream in;	//stream read from the socket
    private ObjectOutputStream out;    //stream write to the socket

    public HandshakeProtocol(boolean isClient, String peerID, ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
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

    private void sendHandshakeMessage() throws IOException {
        String output = "";
        output += "P2PFILESHARINGPROJ";
        BitSet bitfield = new BitSet(10);
        output += bitfield.toString();
        output += peerID;
        out.writeObject(output);
    }

    private void receiveHandshakeMessage() throws IOException, ClassNotFoundException {
        String input = "";
        byte[] buffer = new byte[32];
        input += (String) in.readObject();
        System.out.println(input);
    }
}
