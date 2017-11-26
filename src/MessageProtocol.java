import java.io.*;
import java.util.*;

/**
 * Created by cyonkee on 10/23/17.
 */
public class MessageProtocol {
    private boolean isClient;
    private String neighborID;
    private ObjectInputStream in;	//stream read from the socket
    private ObjectOutputStream out;    //stream write to the socket

    public MessageProtocol(boolean isClient, String neighborID, ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
        this.isClient = isClient;
        this.neighborID = neighborID;
        this.out = out;
        this.in = in;

//        if(isClient)
//            doClientMessage(type);
//        else
//            doServerMessage(type);
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
        byte[] newMessage = null;

        byte[] neighborIdBytes = neighborID.getBytes();
        int i;
        switch (type) {
            case 0:
            case 1:
            case 2:
            case 3:
                newMessage = new byte[5];
                String y = "1";
                byte[] x = y.getBytes();
                for (i = 0; i < y.length(); i++) {
                    System.out.println(x[i]);
                }
                break;
            case 4:
            case 6:
            case 7:
                newMessage = new byte[9];
            case 5:
        }



        out.write(newMessage);
        out.flush(); //must flush each time a message is written to stream
    }

    private void receiveMessage() throws IOException, ClassNotFoundException {

    }

    public boolean getIsClient(){ return isClient; }
    public String getNeighborID(){ return neighborID; }
}
