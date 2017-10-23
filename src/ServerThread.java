/**
 * Created by cyonkee on 10/22/17.
 */
import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

public class ServerThread extends Thread {
    private Socket socket = null;
    private boolean open = true;
    private PrintWriter out;
    private BufferedReader in;

    public ServerThread(Socket socket) throws IOException {
        super("ServerThread");
        this.socket = socket;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        try{
            String inputLine, outputLine;
            ServerProtocol lp = new ServerProtocol();

            inputLine = in.readLine();
            System.out.println("Client: " + inputLine);

            outputLine = "Hello Client";
            System.out.println("Server: " + outputLine);
            out.println(outputLine);

            inputLine = in.readLine();
            System.out.println("Client: " + inputLine);

//            while ((inputLine = in.readLine()) != null) {
//                System.out.println("Client: " + inputLine);
//
//                outputLine = lp.processInput(inputLine);
//                out.println(outputLine);
//            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
