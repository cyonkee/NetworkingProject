package handlers;

import connection.Helper;
import connection.PeerProcess;
import msgSenders.ChokeRunnable;
import msgSenders.UnchokeRunnable;
import setup.Config;
import setup.Neighbor;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by cyonkee on 12/2/17.
 */
public class PreferredPeers {
    private PeerProcess peer;
    private BufferedOutputStream out;
    private Config attributes;
    private BitSet myBitfield;
    private String neighborID;
    private Neighbor thisPeer;

    public PreferredPeers(BufferedOutputStream out,PeerProcess peer, String neighborID){
        this.out = out;
        this.peer = peer;
        this.neighborID = neighborID;
        attributes = peer.getAttributes();
        thisPeer = (Neighbor) peer.getMap().get(peer.getPeerID());
        myBitfield = (BitSet) thisPeer.getBitfield();
    }

    public void handle() {
        List<Integer> list = new ArrayList<>();
        ArrayList ids = peer.getNeighborIDs();
        HashMap map = peer.getMap();

        findingInterestedPeers(list, ids, map);

        //test
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
            System.out.println("Size: " + list.size());
        }
        List chosenPeers = determineUnchokePeers(list, map);
        thisPeer.setChosenPeers(chosenPeers);
        writeChangePreferredLog(peer.getLogWriter(), peer, neighborID, chosenPeers);
        sendChokeMessages(chosenPeers, list);
    }

    private void findingInterestedPeers(List list, ArrayList ids, HashMap map){
        for(int i = 0; i<ids.size(); i++){
            Neighbor n = (Neighbor) map.get(ids.get(i));
            if (n.getSocket() != null && n.getIsInterested()) {
                list.add(Integer.valueOf((String) ids.get(i)));
            }
        }
    }

    private List determineUnchokePeers(List list, HashMap map){
        try {
            //if number of interested is less than preferred neighbors limit
            if (list.size() <= peer.getAttributes().getNumOfPreferredNeighbors()) {
                sendUnchokes(list, map);
                return list;
            }
            else {
                //If the peer has the full file
                if (myBitfield.cardinality() == attributes.getNumOfPieces()) {
                    return sendRandomUnchokes(list, map);
                }
                else {
                    //get top download rates
                    list = bubbleSortByRates(list);
                    List top = list.subList(0, attributes.getNumOfPreferredNeighbors());
                    System.out.println("top 5 = ");
                    for (int i = 0; i < top.size(); i++) {
                        System.out.println(top.get(i));
                    }
                    sendUnchokesToPreferreds(list, map, top);
                    return top;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List bubbleSortByRates(List list) {
        HashMap dmap = peer.getDownloads();
        int temp = 0;
        for(int i=0; i < list.size(); i++){
            for(int j=1; j < list.size() - i; j++){
                String peerID1 = Integer.toString((int) list.get(j-1));
                String peerID2 = Integer.toString((int) list.get(j));
                int rate1 = (int) dmap.get(peerID1);
                int rate2 = (int) dmap.get(peerID2);
                if(rate1 < rate2){
                    temp = rate1;
                    list.set(j-1,rate2);
                    list.set(j,temp);
                }
            }
        }
        return list;
    }

    private void sendUnchokesToPreferreds(List list, HashMap map, List top) throws IOException {
        for (int i = 0; i < top.size(); i++) {
            Neighbor neighbor = (Neighbor) map.get(String.valueOf(list.get(i)));
            if (neighbor.getIsChoked()) {
                UnchokeRunnable unchokeSender = new UnchokeRunnable("unchokeSender",out,peer,String.valueOf(list.get(i)));
                unchokeSender.start();
            }
        }
    }

    private List sendRandomUnchokes(List list, HashMap map) throws IOException {
        ArrayList<Integer> chosenPeers = new ArrayList();
        for (int i = 0; i < attributes.getNumOfPreferredNeighbors(); i++) {
            Random random = new Random();
            int randomIndex = random.nextInt(list.size());
            chosenPeers.add((int) list.get(randomIndex));
            Neighbor neighbor = (Neighbor) map.get(String.valueOf(list.get(randomIndex)));
            if (neighbor.getIsChoked()) {
                UnchokeRunnable unchokeSender = new UnchokeRunnable("unchokeSender",out,peer,String.valueOf(list.get(randomIndex)));
                unchokeSender.start();
            }
        }
        return chosenPeers;
    }

    private void sendUnchokes(List list, HashMap map) throws IOException {
        for (int i = 0; i < list.size(); i++) {
            Neighbor neighbor = (Neighbor) map.get(String.valueOf(list.get(i)));
            if (neighbor.getIsChoked()) {
                UnchokeRunnable unchokeSender = new UnchokeRunnable("unchokeSender",out,peer,String.valueOf(list.get(i)));
                unchokeSender.start();
            }
        }
    }

    public void sendChokeMessages(List chosen, List list) {
        Neighbor neighbor;
        boolean chosenPeer = false;
        for(int i=0; i<list.size(); i++){
            chosenPeer = isChosenPeer(chosen, list, chosenPeer, i);
            ChokeIfUnchosenAndUnchoked(list, chosenPeer, i);
            chosenPeer = false;
        }
    }

    private void ChokeIfUnchosenAndUnchoked(List list, boolean chosenPeer, int i) {
        Neighbor neighbor;
        if(!chosenPeer){
            String peerID = Integer.toString((int) list.get(i));
            neighbor = (Neighbor) peer.getMap().get(peerID);
            if(!neighbor.getIsChoked()){
                ChokeRunnable chokeSender = new ChokeRunnable("chokeSender",out,peer,peerID);
                chokeSender.start();
            }
        }
    }

    private boolean isChosenPeer(List chosen, List list, boolean chosenPeer, int i) {
        for(int j=0; j<chosen.size(); j++){
            if(list.get(i).equals(chosen.get(j))){
                chosenPeer = true;
                break;
            }
        }
        return chosenPeer;
    }

    private void writeChangePreferredLog(PrintWriter logWriter, PeerProcess peer, String neighborID, List chosenPeers) {
        String output;
        Helper helper = new Helper();
        output = helper.getCurrentTime();
        output += "Peer "+peer.getPeerID()+" has the preferred neighbors ";
        for(int i=0; i < chosenPeers.size(); i++){
            output += chosenPeers.get(i);
            if(i != chosenPeers.size() - 1)
                output += ", ";
            else
                output += ".";
        }
        logWriter.println(output);
        logWriter.flush();
    }
}
