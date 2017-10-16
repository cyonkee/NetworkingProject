/**
 * Created by cyonkee on 10/15/17.
 */
public class TCPConnection {
    private PeerProcess peer;
    static TCPState state;

    public TCPConnection(PeerProcess peer){
        this.peer = peer;
        state = new TCPClosed(this, peer);
    }

    
}
