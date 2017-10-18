/**
 * Created by cyonkee on 9/22/17.
 */

import java.io.*;
import java.util.*;

/*
This class is where the program begins by constructing a peer process.

First, the peer is configured by parsing the "Common.cfg" file. The attributes
of the peer process are contained by an instance of Config (they can be accessed by getters).

Second, the info for all peer processes is obtained by parsing the "PeerInfo.cfg" file.
An instance of PeersInfo will create a hash map with the individual peerIDs as the keys.
The values associated with the keys are instances of the Neighbor class which holds the
hostname, port, and boolean hasFile for each neighbor peer (also contains current peer).

Third, the current peer instance initializes a TCPconnection.
 */

public class PeerProcess {
    private String peerID;
    private Config attributes;
    private PeersInfo peersInfo;
    private ArrayList neighborIDs = new ArrayList();

    public PeerProcess(String peerID){
        this.peerID = peerID;
        configurePeer();
        parsePeersFile();
        TCPConnection conn = new TCPConnection(this);
    }

    private void configurePeer(){
        ArrayList list = new ArrayList();

        try {
            BufferedReader in = new BufferedReader(new FileReader("Common.cfg"));
            fillListWithCommonCfg(list, in);
            attributes = new Config(list);
            in.close();
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    private void fillListWithCommonCfg(ArrayList list, BufferedReader in) throws IOException {
        String str;
        while((str = in.readLine()) != null) {
            String[] tokens = str.split("\\s+");
            list.add(tokens[1]);
        }
    }

    private void parsePeersFile() {
        ArrayList list = new ArrayList();
        try {
            BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
            fillListWithPeersInfo(list, in);
            peersInfo = new PeersInfo(list);
            in.close();
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    private void fillListWithPeersInfo(ArrayList list, BufferedReader in) throws IOException {
        String str;
        while((str = in.readLine()) != null) {
            String[] tokens = str.split("\\s+");
            neighborIDs.add(tokens[0]);
            String[] arr = {tokens[0], tokens[1], tokens[2], tokens[3]};
            list.add(arr);
        }
    }

    public String getPeerID(){ return peerID; }
    public Config getAttributes(){ return attributes; }
    public PeersInfo getPeersInfo(){ return peersInfo; }
    public ArrayList getNeighborIDs() {
        return neighborIDs;
    }

    public static void main(String[] args){
        PeerProcess peerProcess = new PeerProcess(args[0]);
    }
}
