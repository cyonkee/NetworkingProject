/**
 * Created by cyonkee on 10/22/17.
 */
import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

public class ServerThread extends Thread {
    private Socket socket = null;

    public ServerThread(Socket socket) {
        super("ClientThread");
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            String inputLine, outputLine;
            ServerProtocol lp = new ServerProtocol();
            outputLine = lp.processInput(null);
            out.println(outputLine);

            while ((inputLine = in.readLine()) != null) {
                outputLine = lp.processInput(inputLine);
                out.println(outputLine);
//                if (outputLine.equals("Bye"))
//                    break;
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
