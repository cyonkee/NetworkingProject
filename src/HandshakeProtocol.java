import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by cyonkee on 10/23/17.
 */
public class HandshakeProtocol {
    private boolean isClient;

    public HandshakeProtocol(boolean isClient, PrintWriter out, BufferedReader in) throws IOException{
        this.isClient = isClient;
    }

}
