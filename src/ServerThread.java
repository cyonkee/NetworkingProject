/**
 * Created by cyonkee on 10/22/17.
 */
import java.net.*;
import java.io.*;
import java.lang.*;

/*
Using an ObjectInputStream and ObjectOutputStream for transferring messages.
 */
public class ServerThread extends Thread {
    private Socket socket = null;
    private ObjectInputStream in;	//stream read from the socket
    private ObjectOutputStream out;    //stream write to the socket
    private boolean isClient = false;
    private PeerProcess peer;

    public ServerThread(Socket socket, PeerProcess peer) throws IOException {
        super("ServerThread");
        this.socket = socket;
        this.peer = peer;
    }

    //run() method is called on when .start() is
    //invoked (in TCPConnection class startServer() method) to start the thread.
    @Override
    public void run() {
        try{
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            HandshakeProtocol handshake = new HandshakeProtocol(isClient,peer.getPeerID(),in,out);

//            String inputLine, outputLine;
//            inputLine = in.readLine();
//            System.out.println("Client: " + inputLine);
//
//            outputLine = "Hello Client";
//            System.out.println(outputLine);
//            out.println(outputLine);
//
//            inputLine = in.readLine();
//            System.out.println("Client: " + inputLine);

//            while ((inputLine = in.readLine()) != null) {
//                System.out.println("Client: " + inputLine);
//
//                outputLine = lp.processInput(inputLine);
//                out.println(outputLine);
//            }

            //socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
