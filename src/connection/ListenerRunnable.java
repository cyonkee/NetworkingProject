package connection;

import handlers.BitfieldHandler;
import handlers.InterestedHandler;
import handlers.NotInterestedHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * Created by cyonkee on 12/1/17.
 */

public class ListenerRunnable implements Runnable {
    private Thread t;
    private String name;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private PeerProcess peer;
    private String neighborID;

    public ListenerRunnable(String name, BufferedInputStream in, BufferedOutputStream out, PeerProcess peer, String neighborID){
        this.name = name;
        this.in = in;
        this.out = out;
        this.peer = peer;
        this.neighborID = neighborID;
    }

    @Override
    public void run() {
        while(true){
            try {
                if(in.available() != 0) {
                    byte[] lengthMsg = new byte[4];
                    in.read(lengthMsg, 0, 4);

                    //read in the message type and the payload
                    String mLength = new String(lengthMsg);
                    int length = Integer.valueOf(mLength);
                    byte[] input = new byte[length];
                    in.read(input, 0, length);

                    //store type
                    byte[] msgType = new byte[1];
                    msgType[0] = input[0];
                    String mType = new String(msgType);

                    //store payload
                    byte[] payload = new byte[length - 1];
                    for (int i = 1; i < length; i++) {
                        payload[i - 1] = input[i];
                    }

                    handleMessage(mType, payload);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessage(String mType, byte[] payload) {
        switch (mType) {
            case "0":
                //received choke
                break;
                
            case "1":
                //received Unchoke, so send request for piece
                break;

            case "2":
                //received interested
                System.out.println("received interested");
                InterestedHandler interestedHandler = new InterestedHandler(peer);
                interestedHandler.handle(neighborID);
                break;

            case "3":
                //received not interested
                System.out.println("received not interested");
                NotInterestedHandler notInterestedHandler = new NotInterestedHandler(peer);
                notInterestedHandler.handle(neighborID);
                break;

            case "4":
                //received have, send interested or not
                break;

            case "5":
                //received Bitfield, so check if there are interesting pieces and send not/interested
                System.out.println("Received Bitfield");
                BitfieldHandler bitHandler = new BitfieldHandler(payload, peer);
                bitHandler.handle(neighborID,out);
                break;

            case "6":
                //received request, so send piece
                break;

            case "7":
                //received piece, so update bitfield and file, send "have" to peers
                break;

        }
    }

    public void start() {
        t = new Thread(this, name);
        t.start();
    }
}
