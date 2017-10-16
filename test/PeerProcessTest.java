import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.Configuration;
import java.util.HashMap;
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

    @Test
    public void findPeersInfoTest(){
        PeerProcess p = new PeerProcess("1001");
        PeersInfo peersinfo = p.getPeersInfo();
        HashMap map = peersinfo.getMap();

        Neighbor n1 = (Neighbor) map.get("1001");
        System.out.println(n1.getHostname() + " " + n1.getPort() + " " + n1.getHasFile());
        Assertions.assertEquals("lin114-00.cise.ufl.edu", n1.getHostname());
        Assertions.assertEquals(6008, n1.getPort());
        Assertions.assertTrue(n1.getHasFile());

        Neighbor n2 = (Neighbor) map.get("1002");
        System.out.println(n2.getHostname() + " " + n2.getPort() + " " + n2.getHasFile());
        Assertions.assertEquals("lin114-01.cise.ufl.edu", n2.getHostname());
        Assertions.assertEquals(6008, n2.getPort());
        Assertions.assertFalse(n2.getHasFile());

        Neighbor n3 = (Neighbor) map.get("1003");
        System.out.println(n3.getHostname() + " " + n3.getPort() + " " + n3.getHasFile());
        Assertions.assertEquals("lin114-02.cise.ufl.edu", n3.getHostname());
        Assertions.assertEquals(6008, n3.getPort());
        Assertions.assertFalse(n3.getHasFile());

        Neighbor n4 = (Neighbor) map.get("1004");
        System.out.println(n4.getHostname() + " " + n4.getPort() + " " + n4.getHasFile());
        Assertions.assertEquals("lin114-03.cise.ufl.edu", n4.getHostname());
        Assertions.assertEquals(6008, n4.getPort());
        Assertions.assertFalse(n4.getHasFile());

        Neighbor n5 = (Neighbor) map.get("1005");
        System.out.println(n5.getHostname() + " " + n5.getPort() + " " + n5.getHasFile());
        Assertions.assertEquals("lin114-04.cise.ufl.edu", n5.getHostname());
        Assertions.assertEquals(6008, n5.getPort());
        Assertions.assertFalse(n5.getHasFile());

        Neighbor n6 = (Neighbor) map.get("1006");
        System.out.println(n6.getHostname() + " " + n6.getPort() + " " + n6.getHasFile());
        Assertions.assertEquals("lin114-05.cise.ufl.edu", n6.getHostname());
        Assertions.assertEquals(6008, n6.getPort());
        Assertions.assertFalse(n6.getHasFile());
    }
}