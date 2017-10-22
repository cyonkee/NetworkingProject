import java.net.*;
import java.util.*;
import java.io.*;
import java.lang.*;

/**
 * Created by cyonkee on 10/22/17.
 */
public class ClientThread extends Thread {
    private Socket socket = null;

    public ClientThread(Socket socket) {
        super("ClientThread");
        this.socket = socket;
    }

    @Override
    public void run() {
        try(
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String fromServer;
            String fromUser;

            while ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer);

                fromUser = stdIn.readLine();
                if (fromUser != null) {
                    System.out.println("Client: " + fromUser);
                    out.println(fromUser);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
