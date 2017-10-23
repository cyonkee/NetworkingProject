/**
 * Created by cyonkee on 10/15/17.
 */
public class Neighbor {
    private String hostname;
    private int port;
    private boolean hasFile;
    private int peerCount;

    public Neighbor(String hostname, String port, String hasFile, int peerCount){
        this.hostname = hostname;
        this.port = Integer.valueOf(port);
        this.hasFile = hasFile.equals("1") ? true : false;
        this.peerCount = peerCount;
    }

    public String getHostname(){ return hostname; }
    public int getPort() { return port; }
    public boolean getHasFile() { return hasFile; }
    public int getPeerCount(){ return peerCount; }
}
