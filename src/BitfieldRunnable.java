import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.BitSet;

/**
 * Created by cyonkee on 12/1/17.
 */
public class BitfieldRunnable implements Runnable {
    private Thread t;
    private String name;
    private BufferedOutputStream out;
    private PeerProcess peer;
    private Config attributes;
    private BitSet myBitfield;

    public BitfieldRunnable(String name, BufferedOutputStream out, PeerProcess peer){
        this.name = name;
        this.out = out;
        this.peer = peer;
        attributes = peer.getAttributes();
        Neighbor thisPeer = (Neighbor) peer.getMap().get(peer.getPeerID());
        myBitfield = (BitSet) thisPeer.getBitfield();
    }

    @Override
    public void run() {
        try {
            byte[] output = formBitfieldMessage();

            System.out.println("sent bitfield");
            out.write(output);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        t = new Thread(this, name);
        t.run();
    }

    private byte[] formBitfieldMessage(){
        //Transform bitfield to byte[]
        byte[] pieces = new byte[attributes.getNumOfPieces()];
        for (int i = 0; i < pieces.length; i++) {
            if (myBitfield.get(i) == false)
                pieces[i] = 0;
            else
                pieces[i] = 1;
        }

        //Output msg
        byte[] output = new byte[5 + pieces.length];

        //msg length
        String lengthMsg = Integer.toString(pieces.length + 1);
        lengthMsg = padLeft(lengthMsg, 4);

        byte[] lengthMsgBytes = lengthMsg.getBytes();
        for (int i = 0; i < 4; i++)
            output[i] = lengthMsgBytes[i];

        //msg type
        String type = "5";
        byte[] typeBytes = type.getBytes();
        output[4] = typeBytes[0];

        //msg payload = bitfield
        for (int i = 0; i < pieces.length; i++) {
            output[i + 5] = pieces[i];
        }

        return output;
    }

    private String padLeft(String s, int length) {
        return String.format("%" + length + "s", s).replace(' ','0');
    }
}
