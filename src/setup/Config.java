package setup; /**
 * Created by cyonkee on 10/12/17.
 */
import java.util.*;


/*
This is the object that will hold the info obtained from
parsing the common.cfg file.
 */
public class Config {
    private int numOfPreferredNeighbors = 0;
    private int unchokingInterval = 0;
    private int optimisticUnchokingInterval = 0;
    private String fileName = "";
    private int fileSize = 0;
    private int pieceSize = 0;
    private int numOfPieces = 0;
    private int lastPieceSize = 0;

    public Config(ArrayList list){
        numOfPreferredNeighbors = Integer.valueOf((String) list.get(0));
        unchokingInterval = Integer.valueOf( (String) list.get(1) );
        optimisticUnchokingInterval = Integer.valueOf( (String) list.get(2) );
        fileName = (String) list.get(3);
        fileSize = Integer.valueOf( (String) list.get(4));
        pieceSize = Integer.valueOf( (String) list.get(5));
        numOfPieces = (fileSize / pieceSize) + 1;
        lastPieceSize = fileSize % pieceSize;
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

    public int getNumOfPieces() { return numOfPieces; }

    public int getLastPieceSize() { return lastPieceSize; }
}
