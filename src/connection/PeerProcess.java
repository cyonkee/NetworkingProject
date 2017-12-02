package connection; /**
 * Created by cyonkee on 9/22/17.
 */

import setup.Config;
import setup.Neighbor;
import setup.PeersInfo;

import java.io.*;
import java.util.*;

/*
This class is where the program begins by constructing a peer process.

First, the peer is configured by parsing the "Common.cfg" file. The attributes
of the peer process are contained by an instance of setup.Config (they can be accessed by getters).

Second, the info for all peer processes is obtained by parsing the "PeerInfo.cfg" file.
An instance of setup.PeersInfo will create a hash map with the individual peerIDs as the keys.
The values associated with the keys are instances of the setup.Neighbor class which holds the
hostname, port, and boolean hasFile for each neighbor peer (also contains current peer).

Third, after the files are parsed then the bitfields for each setup.Neighbor are set by
accessing each peer through the hashmap and checking the boolean hasFile. Also, the
size of the file is attained from the setup.Config attributes object.
 */

public class PeerProcess {
    private String peerID;
    private Config attributes;
    private PeersInfo peersInfo;
    private ArrayList neighborIDs = new ArrayList();
    private PrintWriter logWriter;
    private HashMap<String, Integer> downloads;

    public PeerProcess(String peerID) throws IOException {
        this.peerID = peerID;
        logWriter = new PrintWriter("log_peer_" + peerID + ".log");
        configurePeer();
        parsePeersFile();
        setBitfields();
        this.downloads = new HashMap<>();
        initializeMap();
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

    private void setBitfields(){
        ArrayList neighborids = getNeighborIDs();
        int numOfPieces = attributes.getFileSize() / attributes.getPieceSize();
        for (int i=0; i<getMaxCount(); i++){
            Neighbor n = (Neighbor) peersInfo.getMap().get(neighborids.get(i));
            BitSet bitfield = new BitSet(numOfPieces);
            if (n.getHasFile() == true) {
                bitfield.set(0, numOfPieces+1, true);
            }
            n.setBitfield(bitfield);
        }
    }

    private void initializeMap() {
        int i=0;
        Iterator it = neighborIDs.iterator();
        for(;it.hasNext();it.next()) {
            downloads.put((String) neighborIDs.get(i), 0);
            i++;
        }
    }

    public void incrementDownloads(String neighborID) {
        if (downloads.get(neighborID) == null)
            downloads.put(neighborID, 1);
        else
            downloads.put(neighborID, downloads.get(neighborID) + 1);
    }

    public String getPeerID(){ return peerID; }
    public Config getAttributes(){ return attributes; }
    public PeersInfo getPeersInfo(){ return peersInfo; }
    public ArrayList getNeighborIDs() { return neighborIDs; }
    public HashMap getMap() { return peersInfo.getMap(); }
    public int getMaxCount() { return peersInfo.getMaxPeerscount(); }
    public PrintWriter getLogWriter() { return logWriter; }
    public HashMap<String, Integer> getDownloads() { return downloads; }

    public static void main(String[] args) throws IOException {
        //Start the connection.PeerProcess and parse the files and set bitfields.
        String currentPeerID = args[0];
        PeerProcess peerProcess = new PeerProcess(currentPeerID);

        //Get info from the peer
        HashMap map = peerProcess.getMap();
        Neighbor currentPeer = (Neighbor) map.get(currentPeerID);
        int countNumber = currentPeer.getPeerCount();

        //Initialize a connection.TCPConnection
        TCPConnection conn = new TCPConnection(peerProcess);

        //if first peer in list then just listen
        //other peers connect to peers below in the list
        if(countNumber > 0){
            Iterator it = peerProcess.getNeighborIDs().iterator();
            while(it.hasNext()){
                String id = (String) it.next();
                Neighbor n = (Neighbor) map.get(id);
                if(countNumber > n.getPeerCount()){
//                    DoStartClientRunner runner = new DoStartClientRunner(conn, n);
//                    Thread clientThread = new Thread(runner);
//                    clientThread.start();
                    conn.startClient(n);
                }
            }
        }

        //Start Listening for incoming connections if not the last peer
        if(countNumber < peerProcess.getMaxCount() - 1)
            conn.startServer();

        peerProcess.getLogWriter().close();
    }
}
