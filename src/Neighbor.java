/**
 * Created by cyonkee on 10/15/17.
 */
public class Neighbor {
    private String hostname;
    private int port;
    private boolean hasFile;

    public Neighbor(String hostname, String port, String hasFile){
        this.hostname = hostname;
        this.port = Integer.valueOf(port);
        this.hasFile = hasFile.equals("1") ? true : false;
    }

    public String getHostname(){ return hostname; }

    public int getPort() {
        return port;
    }

    public boolean getHasFile() {
        return hasFile;
    }
}
