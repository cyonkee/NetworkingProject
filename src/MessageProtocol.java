import java.io.*;
import java.util.*;
import java.lang.*;

/**
 * Created by cyonkee on 10/23/17.
 */
public class MessageProtocol {
    private boolean isClient;
    private PeerProcess peer;
    private String neighborID;
    private ObjectInputStream in;	//stream read from the socket
    private ObjectOutputStream out;    //stream write to the socket
    private int pieceSize;
    private String filename;

    public MessageProtocol(boolean isClient, PeerProcess peer, String neighborID, ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
        this.isClient = isClient;
        this.peer = peer;
        this.neighborID = neighborID;
        this.out = out;
        this.in = in;
        pieceSize = peer.getAttributes().getPieceSize();
        filename = peer.getAttributes().getFileName();
    }

    public void doClientMessage() throws IOException, ClassNotFoundException {
        //start messaging by sending bitfield
        //testing request
        sendMessage(6,null);
        receiveMessage();
    }

    public void doServerMessage() throws IOException, ClassNotFoundException {
        receiveMessage();
    }

    //Create 32 byte message and send
    public void sendMessage(int type, byte[] payload) throws IOException {
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
                sendPiece(payload);
                break;
        }
    }

    public void sendPiece(byte[] payload) throws IOException {
        String piece = new String(payload);
        int pieceIndex = Integer.parseInt(piece);
        int offset = pieceIndex * pieceSize;

        //get piece of file
        RandomAccessFile raf = new RandomAccessFile(filename,"r");
        raf.seek(offset);
        byte[] pieceOfFile = new byte[pieceSize];
        raf.readFully(pieceOfFile);

        //get msg values
        byte[] output = new byte[pieceSize + 5];
        String lengthMsg = Integer.toString(pieceSize + 1);
        lengthMsg = padLeft(lengthMsg,4);
        String type = "7";
        byte[] lengthMsgBytes = lengthMsg.getBytes();
        byte[] typeBytes = type.getBytes();

        //msg length
        for(int i=0; i<4; i++)
            output[i] = lengthMsgBytes[i];

        //msg type
        output[4] = typeBytes[0];

        //msg payload (piece being sent)
        for(int i=0; i<pieceSize; i++)
            output[i+5] = pieceOfFile[i];

        //test
        String s = new String(output);
        System.out.println(s);

        out.write(output);
        out.flush();
    }

    public void sendRequest() throws IOException {
        int randomIndex = chooseRandomMissingPiece();
        byte[] output = new byte[9];
        String lengthMsg = "0005";
        String type = "6";
        String pieceIndex = Integer.toString(randomIndex);
        pieceIndex = padLeft(pieceIndex,4);
        byte[] lengthMsgBytes = lengthMsg.getBytes();
        byte[] typeBytes = type.getBytes();
        byte[] pieceIndexBytes = pieceIndex.getBytes();

        //msg length
        for(int i=0; i<4; i++)
            output[i] = lengthMsgBytes[i];

        //msg type
        output[4] = typeBytes[0];

        //payload (piece being requested)
        for(int i=0; i<4; i++)
            output[i+5] = pieceIndexBytes[i];

        //test
        String s = new String(output);
        System.out.println(s);

        out.write(output);
        out.flush();
    }

    public void receiveMessage() throws IOException, ClassNotFoundException{
        //Read in the message length
        byte[] lengthMsg = new byte[4];
        in.read(lengthMsg,0,4);

        //read in the message type and the payload
        String mLength = new String(lengthMsg);
        int length = Integer.valueOf(mLength);
        byte[] input = new byte[length];

        //store type
        in.read(input, 0, length);
        byte[] msgType = new byte[1];
        msgType[0] = input[0];
        String mType = new String(msgType);

        //store payload
        byte[] payload = new byte[length - 1];
        for(int i=1; i < length; i++){
            payload[i-1] = input[i];
        }

        //test
        String s = new String(input);
        System.out.println(s);

        switch (mType) {
            case "0":
            case "1":
                //received Unchoke, so send request for piece
                sendMessage(6, null);
                break;
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
                //received request, so send piece
                sendMessage(7, payload);
                break;
            case "7":
                //received piece, so update bitfield and file, send "have" to peers
                //updateBitfield(payload);
                //updateFile(payload);
                sendMessage(4, payload);
                break;
        }

    }

    public int chooseRandomMissingPiece(){
        //Get Bitfield
        HashMap map = peer.getMap();
        Neighbor n = (Neighbor) map.get(peer.getPeerID());
        int numOfPieces = peer.getAttributes().getNumOfPieces();

        BitSet currentBitField = n.getBitfield();
        int bitFieldLength = currentBitField.length();

        //Fill list with missing pieces indices
        ArrayList<Integer> indicesOfMissingPieces = new ArrayList<Integer>();
        if(bitFieldLength == 0){
            for(int i=0; i<numOfPieces; i++)
                indicesOfMissingPieces.add(i);
        }
        else{
            for(int i=0; i<numOfPieces; i++){
                if(currentBitField.get(i) == false)
                    indicesOfMissingPieces.add(i);
            }
        }

        //Randomly select index from missing
        Random random = new Random();
        return random.nextInt(indicesOfMissingPieces.size()-1);
    }

//    private byte[] setZeroes(byte[] byteArray){
//        for(int i=0; i < byteArray.length; i++){
//            if(byteArray[i] == 0)
//                byteArray[i] = (byte) '0';
//        }
//        return byteArray;
//    }

    public String padLeft(String s, int length) {
        return String.format("%" + length + "s", s).replace(' ','0');
    }

    public boolean getIsClient(){ return isClient; }
    public String getNeighborID(){ return neighborID; }
}
