/**
 * Created by cyonkee on 10/15/17.
 */

import java.io.BufferedOutputStream;
import java.util.*;
import java.net.*;

/*
This is the object that holds info about each peer from
parsing the PeersInfo.cfg.
 */
public class Neighbor {
    private String hostname;
    private int port;
    private boolean hasFile;
    private BitSet bitfield;
    private int peerCount;
    private Socket socket;
    private BufferedOutputStream out;

    public Neighbor(String hostname, String port, String hasFile, int peerCount){
        this.hostname = hostname;
        this.port = Integer.valueOf(port);
        this.hasFile = hasFile.equals("1") ? true : false;
        this.peerCount = peerCount;
    }

    public void setSocket(Socket socket){ this.socket = socket; }
    public void setOutputStream(BufferedOutputStream out){ this.out = out; }
    public void setBitfield(BitSet bitfield) {
        this.bitfield = bitfield;
    }
    public String getHostname(){ return hostname; }
    public int getPort() { return port; }
    public boolean getHasFile() {
        return hasFile;
    }
    public BitSet getBitfield() {
        return bitfield;
    }
    public int getPeerCount() {
        return peerCount;
    }
    public Socket getSocket(){ return socket; }
    public BufferedOutputStream getOutputStream(){ return out; }
}
