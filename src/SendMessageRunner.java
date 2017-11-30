import java.io.IOException;

public class SendMessageRunner implements Runnable {
    int type;
    byte[] payload;
    MessageProtocol m;

    public SendMessageRunner (int type, byte[] payload, MessageProtocol m) {
        this.type = type;
        this.payload = payload;
        this.m = m;
    }

    public void run() {
        synchronized (m) {
            try {
                m.sendMessageImpl(type, payload);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
