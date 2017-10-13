/**
 * Created by cyonkee on 10/12/17.
 */

public class Config {
    private int numOfPreferredNeighbors = 0;
    private int unchokingInterval = 0;
    private int optimisticUnchokingInterval = 0;
    private String fileName = "";
    private int fileSize = 0;
    private int pieceSize = 0;

    public Config(String[] list){
        numOfPreferredNeighbors = Integer.valueOf(list[0]);
        unchokingInterval = Integer.valueOf(list[1]);
        optimisticUnchokingInterval = Integer.valueOf(list[2]);
        fileName = list[3];
        fileSize = Integer.valueOf(list[4]);
        pieceSize = Integer.valueOf(list[5]);
    }

    public int getNumOfPreferredNeighbors() {
        return numOfPreferredNeighbors;
    }

    public int getUnchokingInterval() {
        return unchokingInterval;
    }

    public int getOptimisticUnchokingInterval() {
        return optimisticUnchokingInterval;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getPieceSize() { return pieceSize; }
}
