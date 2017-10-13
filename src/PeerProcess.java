/**
 * Created by cyonkee on 9/22/17.
 */

import java.io.*;

public class PeerProcess {
    private TCPState state;
    private String peerID;
    private Config attributes;

    public PeerProcess(String peerID){
        this.peerID = peerID;
        configurePeerProcess();
    }

    private void configurePeerProcess(){
        String list[] = new String[6];

        try {
            BufferedReader in = new BufferedReader(new FileReader("Common.cfg"));
            fillListWithFile(list, in);
            attributes = new Config(list);
            in.close();
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
        }

        readPeersFile();
    }

    private void readPeersFile() {
        String str;
        try {
            BufferedReader in = new BufferedReader(new FileReader("Common.cfg"));
            while((str = in.readLine()) != null) {
                String[] tokens = str.split("\\s+");

            }
            in.close();
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    private void fillListWithFile(String[] list, BufferedReader in) throws IOException {
        String str;
        int i=0;
        while((str = in.readLine()) != null) {
            String[] tokens = str.split("\\s+");
            list[i] = tokens[1];
            i++;
        }
    }

    public String getPeerID(){ return peerID; }
    public Config getAttributes(){ return attributes; }

    public static void main(String[] args){
        PeerProcess peerProcess = new PeerProcess(args[0]);
    }
}
