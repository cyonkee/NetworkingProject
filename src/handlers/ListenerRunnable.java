package handlers;

import connection.PeerProcess;
import setup.*;
import msgSenders.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.*;

/**
 * Created by cyonkee on 12/1/17.
 */

public class ListenerRunnable implements Runnable {
    private Thread t;
    private String name;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private PeerProcess peer;
    private Config attributes;
    private String neighborID;

    public ListenerRunnable(String name, BufferedInputStream in, BufferedOutputStream out, PeerProcess peer, String neighborID){
        this.name = name;
        this.in = in;
        this.out = out;
        this.peer = peer;
        this.neighborID = neighborID;
        attributes = peer.getAttributes();
    }

    public void start() {
        t = new Thread(this, name);
        t.start();
    }

    @Override
    public void run() {
        startChokeTimer();
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
                System.out.println("received choke");
                break;

            case "1":
                //received Unchoke, so send request for piece
                System.out.println("received unchoke");
                UnchokeHandler unchokeHandler = new UnchokeHandler(peer);
                unchokeHandler.handle(neighborID,out);
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
                System.out.println("received have");
                HaveHandler haveHandler = new HaveHandler(payload, peer);
                haveHandler.handle(neighborID,out);
                break;

            case "5":
                //received Bitfield, so check if there are interesting pieces and send not/interested
                System.out.println("Received Bitfield");
                BitfieldHandler bitHandler = new BitfieldHandler(payload, peer);
                bitHandler.handle(neighborID,out);
                break;

            case "6":
                //received request, so send piece
                System.out.println("received request");
                RequestHandler requestHandler = new RequestHandler(payload, peer);
                requestHandler.handle(neighborID,out);
                break;

            case "7":
                //received piece, so update bitfield and file, send "have" to peers
                System.out.println("received piece");
                PieceHandler pieceHandler = new PieceHandler(payload, peer);
                pieceHandler.handle(neighborID, out);
                break;

        }
    }

    private void startChokeTimer() {
        TimerTask task = new TimerTask(){
            @Override
            public void run() {
                chokeOrUnchokePeers();
            }
        };

        Timer timer = new Timer("chokeTimer");
        timer.schedule(task, attributes.getUnchokingInterval() * 1000);
    }

    private void chokeOrUnchokePeers() {
        HashMap map = peer.getMap();
        Neighbor neighbor;
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            neighbor = (Neighbor) pair.getValue();

            if(neighbor.getSocket() != null) {
                //if neighbor peer is interested send unchoke
                if ((!pair.getKey().equals(peer.getPeerID())) && neighbor.getIsInterested()) {
                    UnchokeRunnable unchokeSender = new UnchokeRunnable("unchokeSender", out, peer, (String) pair.getKey());
                    unchokeSender.start();
                }
                //if neighbor peer is not interested send choke
                if ((!pair.getKey().equals(peer.getPeerID())) && !neighbor.getIsInterested()) {
                    ChokeRunnable chokeSender = new ChokeRunnable("chokeSender", out, peer, (String) pair.getKey());
                    chokeSender.start();
                }
            }
        }
        startChokeTimer();
    }

}
