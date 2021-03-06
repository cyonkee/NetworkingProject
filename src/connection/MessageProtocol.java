package connection;

import setup.Config;
import setup.Neighbor;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by cyonkee on 10/23/17.
 */
public class MessageProtocol {
    private boolean isClient;
    private PeerProcess peer;
    private String neighborID;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private PrintWriter logWriter;
    private int pieceSize;
    private int lastPieceSize;
    private int numOfPieces;
    private String filename;
    private HashMap<String, Neighbor> map;
    private Neighbor n;
    private Config attributes;
    private BitSet bitfield;

    public MessageProtocol(boolean isClient, PeerProcess peer, String neighborID, BufferedInputStream in, BufferedOutputStream out) throws IOException, ClassNotFoundException {
        this.isClient = isClient;
        this.peer = peer;
        this.neighborID = neighborID;
        this.out = out;
        this.in = in;
        logWriter = peer.getLogWriter();
        attributes = peer.getAttributes();
        pieceSize = attributes.getPieceSize();
        lastPieceSize = attributes.getLastPieceSize();
        filename = attributes.getFileName();
        numOfPieces = attributes.getNumOfPieces();
        map = peer.getMap();
        n = (Neighbor) map.get(peer.getPeerID());
        bitfield = n.getBitfield();
    }

    public void doClientMessage() throws IOException, ClassNotFoundException {
        //start messaging by sending bitfield
        if (bitfield.length() != 0)
            sendMessage(5,null);
        receiveMessage();
    }

    public void doServerMessage() throws IOException, ClassNotFoundException {
        sendMessage(5,null);
        receiveMessage();
    }

    public void receiveMessage() throws IOException, ClassNotFoundException{
        //Read in the message length
        byte[] lengthMsg = new byte[4];
        in.read(lengthMsg,0,4);

        //read in the message type and the payload
        String mLength = new String(lengthMsg);
        //System.out.println("mLength: " + mLength);
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
        //String s = new String(input);
        //System.out.println(s);

        //prepare to send appropriate message for type received
        switch (mType) {
            case "0":
                break;
            case "1":
                //received Unchoke, so send request for piece
                System.out.println("received unchoke from: " + neighborID);
                sendMessage(6, null);
                break;
            case "2":
                System.out.println("received interested from " + neighborID);
                writeReceivedInterestedLog("interested");
                sendMessage(1,null);
                break;
            case "3":
                writeReceivedInterestedLog("not interested");
                System.out.println("received not interested from " + neighborID);
                break;
            case "4":
                System.out.println("Received have from: " + neighborID);
                break;
            case "5":
                //receives bitfield
                /*for (int i = 0; i<payload.length; i++) {
                    System.out.print(payload[i]+ " ");
                }
                System.out.println();*/

                //received Bitfield, so check if there are interesting pieces and send not/interested
                //System.out.println("received bitfield");
                updateNeighborBitfield(payload);
                boolean interested = findPieces(payload);
                if (interested) sendMessage(2, null);
                else sendMessage(3, null);

                break;
            case "6":
                //received request, so send piece
                //System.out.println("received request");
                sendMessage(7, payload);
                break;
            case "7":
                //received piece, so update bitfield and file, send "have" to peers
                updateFile(payload);
                updateBitfield(payload);
                sendMessage(4,payload);
                if (bitfield.cardinality() != numOfPieces) {
                    //sendMessage(6, null);
                } else {
                    System.out.println("HAS FULL FILE");
                }

                break;
        }
        receiveMessage();
    }

    public void sendMessage(int type, byte[] payload) {
        SendMessageRunner runner = new SendMessageRunner(type, payload, this);
        Thread sendMessageThread = new Thread(runner);
        sendMessageThread.start();
    }

    public void sendMessageImpl(int type, byte[] payload) throws IOException, ClassNotFoundException {
        //handle sending messages
        switch (type) {
            case 0:
                sendChoke(true);
                break;
            case 1:
                sendChoke(false);
                break;
            case 2:
                sendInterested(true);
                break;
            case 3:
                sendInterested(false);
                break;
            case 4:
                sendHaves(payload);
                break;
            case 5:
                sendBitfield();
                break;
            case 6:
                sendRequest();
                // System.out.println("sent request");
                break;
            case 7:
                sendPiece(payload);
                //System.out.println("sent piece");
                break;
        }
    }

    public void sendChoke(boolean choke) throws IOException {
        byte[] output = new byte[5];
        String lengthMsg = "0001";
        byte[] lengthMsgBytes = lengthMsg.getBytes();
        for(int i=0; i<4; i++)
            output[i] = lengthMsgBytes[i];

        String type;
        if (choke) {
            type = "0";
            System.out.println("sent choke to: " + neighborID);
        }
        else {
            type = "1";
            System.out.println("sent unchoke to " + neighborID);
        }
        byte[] typeBytes = type.getBytes();
        output[4] = typeBytes[0];

        out.write(output);
        out.flush();
    }

    public void sendInterested(boolean interested) throws IOException {
        byte[] output = new byte[5];
        String lengthMsg = "0001";
        byte[] lengthMsgBytes = lengthMsg.getBytes();
        //System.out.print("lengthMsgBytes: ");
        for(int i=0; i<4; i++) {
            output[i] = lengthMsgBytes[i];
            //System.out.print(lengthMsgBytes[i]);
        }
        //System.out.println("");

        String type;
        if (interested) {
            type = "2";
            System.out.println("sent interested to neighbor: " + neighborID);
        }
        else {
            type = "3";
            System.out.println("sent not interested to neighbor: " + neighborID);
        }
        byte[] typeBytes = type.getBytes();
        output[4] = typeBytes[0];

        out.write(output);
        out.flush();
    }

    public void sendHaves(byte[] payload) throws IOException{
        byte[] output = new byte[9];
        String lengthMsg = "0005";
        String type = "4";
        byte[] lengthMsgBytes = lengthMsg.getBytes();
        byte[] typeBytes = type.getBytes();
        byte[] pieceIndexBytes = new byte[4];
        for(int i=0; i<4; i++)
            pieceIndexBytes[i] = payload[i];

        //msg length
        for(int i=0; i<4; i++)
            output[i] = lengthMsgBytes[i];

        //msg type
        output[4] = typeBytes[0];

        //payload (piece that was received)
        for(int i=0; i<4; i++)
            output[i+5] = pieceIndexBytes[i];

        for (Map.Entry<String, Neighbor> entry : map.entrySet()) {
            String id = entry.getKey();
            //setup.Neighbor neighbor = entry.getValue();
            Neighbor neighbor = map.get(id);
            if (id.equals("1002")) {
                BufferedOutputStream bOS = neighbor.getOutputStream();
                bOS.write(output);
                bOS.flush();
            }
        }
    }

    public boolean findPieces(byte[] payload) {
        if (payload.length == 0) {
            payload = Arrays.copyOf(n.getBitfield().toByteArray(), numOfPieces);
        }
        for (int i = 0; i<numOfPieces; i++) {
            if (payload[i] == 1 && bitfield.get(i) == false) {
                return true;
            }
        }
        return false;
    }

    public boolean findPiece(byte [] pieceIndex) {
        String s = new String(pieceIndex);
        int piece = Integer.valueOf(s);
        if (bitfield.get(piece) == false) {
            return true;
        }
        return false;
    }

    public void sendBitfield() throws IOException {
        //Transform bitfield to byte[]
        byte[] pieces = new byte[numOfPieces];
        for(int i=0; i<pieces.length; i++){
            if(bitfield.get(i) == false)
                pieces[i] = 0;
            else
                pieces[i] = 1;
        }

        //Output msg
        byte[] output = new byte[5+pieces.length];

        //msg length
        String lengthMsg = Integer.toString( pieces.length + 1);
        lengthMsg = padLeft(lengthMsg, 4);

        byte[] lengthMsgBytes = lengthMsg.getBytes();
        for(int i=0; i<4; i++)
            output[i] = lengthMsgBytes[i];

        //msg type
        String type = "5";
        byte[] typeBytes = type.getBytes();
        output[4] = typeBytes[0];

        //msg payload = bitfield
        for(int i=0; i<pieces.length; i++) {
            output[i+5] = pieces[i];
        }

        System.out.println("sent bitfield");
        out.write(output);
        out.flush();
    }

    public void sendPiece(byte[] payload) throws IOException {
        //get pieceIndex and size of piece
        String piece = new String(payload);
        int pieceIndex = Integer.parseInt(piece);
        //System.out.println("pieceIndex: "+ pieceIndex);
        int offset = pieceIndex * pieceSize;
        //System.out.println("offset: " + offset);
        int thisPieceSize;
        if(pieceIndex == numOfPieces - 1)
            thisPieceSize = lastPieceSize;
        else
            thisPieceSize = pieceSize;

        //get piece of file. If last piece, the byte[] array is smaller.
        RandomAccessFile raf = new RandomAccessFile("peer_" + peer.getPeerID() + "/" + filename,"rw");
        raf.seek(offset);
        byte[] pieceOfFile = new byte[thisPieceSize];
        raf.readFully(pieceOfFile);
        raf.close();


        //get msg values
        byte[] output = new byte[1 + 4 + 4 + thisPieceSize];
        String lengthMsg = Integer.toString(1 + 4 + thisPieceSize);
        lengthMsg = padLeft(lengthMsg,4);
        //System.out.println("lengthMsg: " + lengthMsg);
        String type = "7";
        byte[] lengthMsgBytes = lengthMsg.getBytes();
        byte[] typeBytes = type.getBytes();

        //msg length
        //System.out.print("msg length: ");
        for(int i=0; i<4; i++) {
            output[i] = lengthMsgBytes[i];
            //System.out.print(lengthMsgBytes[i] + " ");
        }
        //System.out.println("");

        //msg type
        output[4] = typeBytes[0];

        //msg payload (piece index and piece being sent)
        for(int i=0; i<4; i++)
            output[i+5] = payload[i];

        for(int i=0; i < thisPieceSize; i++)
            output[i+9] = pieceOfFile[i];

        out.write(output);
        out.flush();

        peer.incrementDownloads(neighborID);
        //System.out.println("Number of downloads for peer " + neighborID + ": " + peer.getDownloads().get(neighborID));
    }

    public void sendRequest() throws IOException {
        int randomIndex = chooseRandomMissingPiece();
        if (randomIndex == -1) {
            return;
        }
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

        out.write(output);
        out.flush();
    }

    private void updateBitfield(byte[] payload) {
        //get piece index
        byte[] pieceIndex = new byte[4];
        for(int i=0; i<4; i++)
            pieceIndex[i] = payload[i];

        String index = new String(pieceIndex);
        int piece = Integer.parseInt(index);

        //set appropriate bit in bitfield
        bitfield.set(piece);
    }

    private void updateNeighborBitfield(byte[] payload) {
        BitSet bitfield = new BitSet(numOfPieces);
        for(int i=0; i<numOfPieces; i++) {
            if (payload[i] == 1)
                bitfield.set(i);
        }
        Neighbor neighbor = map.get(neighborID);
        neighbor.setBitfield(bitfield);
        //System.out.println("neighborID: " + neighborID);
        //map.put(neighborID, neighbor);
    }

    private void updateFile(byte[] payload) throws IOException {
        //get piece index
        byte[] pieceIndex = new byte[4];
        for(int i=0; i<4; i++)
            pieceIndex[i] = payload[i];

        //get offset
        String index = new String(pieceIndex);
        int piece = Integer.parseInt(index);
        int offset = piece * pieceSize;

        //get bytes of file
        byte[] filepiece = new byte[payload.length - 4];
        for(int i=0; i<payload.length - 4; i++)
            filepiece[i] = payload[i+4];

        RandomAccessFile raf = new RandomAccessFile("peer_" + peer.getPeerID() + "/" + filename,"rw");
        raf.seek(offset);
        raf.write(filepiece);
        raf.close();

        writeDownloadPieceLog(piece);
        if(bitfield.cardinality() == numOfPieces)
            writeFullFileDownloadLog();
    }
    private void writeReceivedInterestedLog(String interested){
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        int hour = now.getHour();
        int minute = now.getMinute();
        int second = now.getSecond();
        String output = month+"/"+day+"/"+year+" "+hour+":"+minute+":"+second+": ";
        output += "Peer "+peer.getPeerID()+ "received the ‘"+interested+"’ message from "+neighborID+".";
        logWriter.println(output);
        logWriter.flush();
    }

    private void writeReceivedHaveLog(int pieceIndex) {
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        int hour = now.getHour();
        int minute = now.getMinute();
        int second = now.getSecond();
        String output = month + "/" + day + "/" + year + " " + hour + ":" + minute + ":" + second + ": ";
        output += "Peer " + peer.getPeerID() + " received the ‘have’ message from " + neighborID + " for the piece " + pieceIndex + ".";
        logWriter.println(output);
        logWriter.flush();
    }

    private void writeDownloadPieceLog(int pieceIndex){
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        int hour = now.getHour();
        int minute = now.getMinute();
        int second = now.getSecond();
        String output = month+"/"+day+"/"+year+" "+hour+":"+minute+":"+second+": ";
        output += "Peer "+peer.getPeerID()+" has downloaded the piece "+pieceIndex+" from Peer "+neighborID+".";
        output += "\nNow the number of pieces it has is " + bitfield.cardinality()+".";
        logWriter.println(output);
        logWriter.flush();
    }

    private void writeFullFileDownloadLog() {
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        int hour = now.getHour();
        int minute = now.getMinute();
        int second = now.getSecond();
        String output = month+"/"+day+"/"+year+" "+hour+":"+minute+":"+second+": ";
        output += "Peer "+peer.getPeerID()+" has downloaded the complete file.";
        logWriter.println(output);
        logWriter.flush();
    }

    public int chooseRandomMissingPiece(){
        int bitFieldLength = bitfield.length();

        //Fill list with missing pieces indices
        ArrayList<Integer> indicesOfMissingPieces = new ArrayList<Integer>();
        boolean indexAdded = false;
        if(bitFieldLength == 0){
            for(int i=0; i<numOfPieces; i++) {
                indicesOfMissingPieces.add(i);
                indexAdded = true;
            }
        }
        else{
            for(int i=0; i<numOfPieces; i++){
                if(!bitfield.get(i) && map.get(neighborID).getBitfield().get(i)) {
                    indicesOfMissingPieces.add(i);
                    indexAdded = true;
                }
            }
        }

        if (!indexAdded) {
            return -1;
        } else {
            //Randomly select index from missing
            //System.out.println("indicesOfMissingPieces.size(): " + indicesOfMissingPieces.size() + "neighborID: " + neighborID);
            Random random = new Random();
            int randomIndex = random.nextInt(indicesOfMissingPieces.size());
            //System.out.println("Random Index: " + randomIndex);
            return indicesOfMissingPieces.get(randomIndex);
        }
    }

    public String padLeft(String s, int length) {
        return String.format("%" + length + "s", s).replace(' ','0');
    }

    public boolean getIsClient(){ return isClient; }
    public String getNeighborID(){ return neighborID; }
}
