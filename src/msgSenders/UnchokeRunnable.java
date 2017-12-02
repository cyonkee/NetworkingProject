package msgSenders;

import com.sun.org.apache.xpath.internal.operations.Neg;
import connection.PeerProcess;
import setup.Config;
import setup.Neighbor;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.*;

/**
 * Created by cyonkee on 12/1/17.
 */
public class UnchokeRunnable implements Runnable {
    private Thread t;
    private String name;
    private BufferedOutputStream out;
    private PeerProcess peer;
    private Config attributes;
    private BitSet myBitfield;
    private String neighborID;

    public UnchokeRunnable(String name, BufferedOutputStream out, PeerProcess peer, String neighborID){
        this.name = name;
        this.out = out;
        this.peer = peer;
        this.neighborID = neighborID;
        attributes = peer.getAttributes();
        Neighbor thisPeer = (Neighbor) peer.getMap().get(peer.getPeerID());
        myBitfield = (BitSet) thisPeer.getBitfield();
    }

    public void start(){
        t = new Thread(this, name);
        t.run();
    }

    @Override
    public void run() {
        byte[] output = formUnchokeMessage();

        List<Integer> list = new ArrayList<>();
        ArrayList ids = peer.getNeighborIDs();
        HashMap map = peer.getMap();

        findingInterestedPeers(list, ids, map);

        //test
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
            System.out.println("Size: " + list.size());
        }
        sendUnchokeMessages(list, map, output);
    }

    private byte[] formUnchokeMessage() {
        byte[] output = new byte[5];
        String lengthMsg = "0001";
        byte[] lengthMsgBytes = lengthMsg.getBytes();
        for(int i=0; i<4; i++)
            output[i] = lengthMsgBytes[i];

        String type = "1";
        byte[] typeBytes = type.getBytes();
        output[4] = typeBytes[0];

        return output;
    }

    private void findingInterestedPeers(List list, ArrayList ids, HashMap map){
        for(int i = 0; i<ids.size(); i++){
            Neighbor n = (Neighbor) map.get(ids.get(i));
            if (n.getSocket() != null && n.getIsInterested()) {
                list.add(Integer.valueOf((String) ids.get(i)));
            }
        }
    }

    private void sendUnchokeMessages(List list, HashMap map, byte[] output){
        try {
            if (list.size() <= peer.getAttributes().getNumOfPreferredNeighbors()) {
                for (int i = 0; i < list.size(); i++) {
                    Neighbor neighbor = (Neighbor) map.get(String.valueOf(list.get(i)));
                    if (neighbor.getIsChoked()) {
                        BufferedOutputStream os = neighbor.getOutputStream();
                        System.out.println("sent unchoke");
                        synchronized (this) {
                            os.write(output);
                            os.flush();
                        }
                    }
                }
            }
            else {
                //If the peer has the full file
                if (myBitfield.cardinality() == attributes.getNumOfPieces()) {
                    for (int i = 0; i < attributes.getNumOfPreferredNeighbors(); i++) {
                        Random random = new Random();
                        int randomIndex = random.nextInt(list.size());
                        Neighbor neighbor = (Neighbor) map.get(String.valueOf(list.get(randomIndex)));
                        if (neighbor.getIsChoked()) {
                            BufferedOutputStream os = neighbor.getOutputStream();
                            System.out.println("sent unchoke");
                            synchronized (this) {
                                os.write(output);
                                os.flush();
                            }
                        }
                    }
                }
                else {
                    //MISSING: Order the "list" of interested peers by their download rate
                    //Get the top ids:
                    List top = list.subList(0, peer.getAttributes().getNumOfPreferredNeighbors());
                    System.out.println("top 5 = ");
                    for (int i = 0; i < top.size(); i++) {
                        System.out.println(top.get(i));
                    }
                    for (int i = 0; i < top.size(); i++) {
                        Neighbor neighbor = (Neighbor) map.get(String.valueOf(list.get(i)));
                        if (neighbor.getIsChoked()) {
                            BufferedOutputStream os = neighbor.getOutputStream();
                            System.out.println("sent unchoke");
                            synchronized (this) {
                                os.write(output);
                                os.flush();
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*public void sendChokeMessages(List chosen, List list) {
        Neighbor n;
        for (int i = 0; i<chosen.size(); i++) {
            n = (Neighbor) chosen.get(i);
            for (int j = 0; j< list.size(); j++) {
                if (!chosen.get(i).equals(list.get(i)) && list.get(i).)
                    ChokeRunnable chokeSender = new ChokeRunnable("chokeSender",out,peer,String.valueOf(list.get(randomIndex)));
                    chokeSender.start();
            }
        }
    }*/

}
