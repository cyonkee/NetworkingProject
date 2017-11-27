import java.io.*;
import java.util.*;

/**
 * Created by cyonkee on 10/23/17.
 */
public class MessageProtocol {
    private boolean isClient;
    private PeerProcess peer;
    private String neighborID;
    private ObjectInputStream in;	//stream read from the socket
    private ObjectOutputStream out;    //stream write to the socket

    public MessageProtocol(boolean isClient, PeerProcess peer, String neighborID, ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
        this.isClient = isClient;
        this.peer = peer;
        this.neighborID = neighborID;
        this.out = out;
        this.in = in;
    }

    private void doClientMessage(int type) throws IOException, ClassNotFoundException {
        sendMessage(type);
        receiveMessage();
    }

    private void doServerMessage(int type) throws IOException, ClassNotFoundException {
        receiveMessage();
        sendMessage(type);
    }

    //Create 32 byte message and send
    public void sendMessage(int type) throws IOException {
        switch (type) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                sendRequest();
                break;
            case 7:
                sendPiece();
                break;
        }
    }

    private void sendPiece() {
    }

    private void sendRequest() throws IOException {
        //send request
        int randomIndex = chooseRandomMissingPiece();
        byte[] output = new byte[9];
        String lengthMsg = "0005";
        String type = "6";
        String pieceIndex = Integer.toString(randomIndex);
        byte[] lengthMsgBytes = lengthMsg.getBytes();
        byte[] typeBytes = type.getBytes();
        byte[] pieceIndexBytesOrig = pieceIndex.getBytes();
        byte[] pieceIndexBytes = Arrays.copyOf(pieceIndexBytesOrig,4);

        for(int i=0; i<4; i++)
            output[i] = lengthMsgBytes[i];

        output[5] = typeBytes[0];

        for(int i=0; i<4; i++)
            output[i+4] = pieceIndexBytes[i];

        out.write(output);
        out.flush();
    }

    private void receiveMessage() throws IOException, ClassNotFoundException{
        //Read in the message length
        byte[] lengthMsg = new byte[4];
        in.read(lengthMsg,0,4);

        //read in the message type and the payload
        String mLength = new String(lengthMsg);
        int length = Integer.valueOf(mLength) + 1;
        byte[] input = new byte[length];
        System.out.println(input.length);

        in.read(input, 0, length);
        byte[] msgType = new byte[1];
        msgType[0] = input[0];
        String mType = new String(msgType);

        switch (mType) {
            case "0":
            case "1":
                //received Unchoke, so send request for piece
                sendMessage(6);
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
                //received request, so send piece
                sendMessage(7);
            case "7":
        }

    }

    public int chooseRandomMissingPiece(){
        //Get Bitfield
        HashMap map = peer.getMap();
        Neighbor n = (Neighbor) map.get(peer.getPeerID());
        BitSet currentBitField = n.getBitfield();
        int bitFieldLength = currentBitField.length();

        //Fill list with missing pieces indices
        ArrayList<Integer> indicesOfMissingPieces = new ArrayList<Integer>();
        for(int i=0; i < bitFieldLength; i++){
            if(currentBitField.get(i) == false)
                indicesOfMissingPieces.add(i);
        }

        //Randomly select index from missing
        Random random = new Random();
        return random.nextInt(indicesOfMissingPieces.size());
    }

    public boolean getIsClient(){ return isClient; }
    public String getNeighborID(){ return neighborID; }
}
