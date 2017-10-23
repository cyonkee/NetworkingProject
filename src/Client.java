import java.net.*;
import java.util.*;
import java.io.*;
import java.lang.*;

/**
 * Created by cyonkee on 10/22/17.
 */
public class Client {
    private Socket socket = null;
    private boolean open = true;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader stdIn;

    public Client(Socket socket) throws IOException{
        //super("Client");
        this.socket = socket;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        stdIn = new BufferedReader(new InputStreamReader(System.in));
    }

    //@Override
    public void startConnection(int serverPort, PeerProcess peer) {
        try{
            String fromServer;
            String fromUser;

            fromUser = "Hello Server at port " + serverPort;
            System.out.println(fromUser);
            out.println(fromUser);

            fromServer = in.readLine();
            System.out.println("Server: " + fromServer);

            fromUser = "I am Client " + peer.getPeerID();
            System.out.println(fromUser);
            out.println(fromUser);
//            while ((fromServer = in.readLine()) != null) {
//                System.out.println("Server: " + fromServer);
//
//                fromUser = stdIn.readLine();
//                if (fromUser != null) {
//                    System.out.println("Client: " + fromUser);
//                    out.println(fromUser);
//                }
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
