import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.Configuration;
import java.util.Iterator;

/**
 * Created by cyonkee on 10/12/17.
 */
public class PeerProcessTest {
    @Test
    public void constructorTest(){
        PeerProcess p = new PeerProcess("1000");
        String id = p.getPeerID();
        System.out.println(id + "\n");
        Assertions.assertEquals("1000", id);
    }

    @Test
    public void configurePeerTest(){
        PeerProcess p = new PeerProcess("1000");
        Config attr = p.getAttributes();

        System.out.println(attr.getNumOfPreferredNeighbors());
        Assertions.assertEquals(2, attr.getNumOfPreferredNeighbors());

        System.out.println(attr.getUnchokingInterval());
        Assertions.assertEquals(5, attr.getUnchokingInterval());

        System.out.println(attr.getOptimisticUnchokingInterval());
        Assertions.assertEquals(15, attr.getOptimisticUnchokingInterval());

        System.out.println(attr.getFileName());
        Assertions.assertEquals("TheFile.dat", attr.getFileName());

        System.out.println(attr.getFileSize());
        Assertions.assertEquals(10000232, attr.getFileSize());

        System.out.println(attr.getPieceSize() + "\n");
        Assertions.assertEquals(32768, attr.getPieceSize());

    }
}
