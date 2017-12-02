package msgSenders;

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
        try {
            byte[] output = formUnchokeMessage();

            List<Integer> list = new ArrayList<>();
            ArrayList ids = peer.getNeighborIDs();
            HashMap map = peer.getMap();
            for(int i = 0; i<ids.size(); i++){
                Neighbor n = (Neighbor) map.get(ids.get(i));
                if (n.getSocket() != null && n.getIsInterested()) {
                    list.add(Integer.valueOf((String) ids.get(i)));
                }
            }
            for(int i = 0; i<list.size(); i++){
                System.out.println(list.get(i));
                System.out.println("Size: "+ list.size());
            }

            if (list.size() <= peer.getAttributes().getNumOfPreferredNeighbors()) {
                for (int i = 0; i<list.size(); i++) {
                    Neighbor neighbor = (Neighbor) map.get(String.valueOf(list.get(i)));
                    if (neighbor == null )System.out.println("null");
                    BufferedOutputStream os = neighbor.getOutputStream();
                    System.out.println("sent unchoke");
                    os.write(output);
                    os.flush();
                }
            }
            else {
                Collections.sort(list, Collections.reverseOrder());
                List<Integer> top = list.subList(0, peer.getAttributes().getNumOfPreferredNeighbors());
                System.out.println("top 5 = ");
                for (int i = 0; i< list.size(); i++) {
                    System.out.println(top.get(i));
                }
                for (int i = 0; i<top.size(); i++) {
                    Neighbor neighbor = (Neighbor) peer.getMap().get(top.get(i));
                    BufferedOutputStream os = neighbor.getOutputStream();
                    System.out.println("sent unchoke");
                    os.write(output);
                    os.flush();
                }
            }


            /*Neighbor neighbor = (Neighbor) peer.getMap().get(neighborID);
            BufferedOutputStream os = neighbor.getOutputStream();
            System.out.println("sent unchoke");
            os.write(output);
            os.flush();*/

        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
