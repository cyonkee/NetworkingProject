package connection;

import setup.Neighbor;

public class DoStartClientRunner implements Runnable{
    TCPConnection conn;
    Neighbor n;

    public DoStartClientRunner(TCPConnection conn, Neighbor n){
        this.conn = conn;
        this.n = n;
    }

    public void run() {
        conn.startClient(n);
    }
}
