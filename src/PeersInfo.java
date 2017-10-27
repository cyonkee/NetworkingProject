import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
/**
 * Created by cyonkee on 10/14/17.
 */

/*
This class holds the hashmap which will contain the Neighbor objects.
 */
public class PeersInfo {
    HashMap map = new HashMap();
    static int MAXPEERSCOUNT = 0;

    public PeersInfo(ArrayList list){
        fillHashMap(list);
    }

    private void fillHashMap(ArrayList list) {
        int i=0;
        Iterator it = list.iterator();
        for(;it.hasNext();it.next()){
            String[] extractedArray = (String[]) list.get(i);
            Neighbor neighbor = new Neighbor(extractedArray[1],extractedArray[2],extractedArray[3],i);
            map.put(extractedArray[0], neighbor);
            i++;
            MAXPEERSCOUNT++;
        }
    }

    public HashMap getMap(){ return map; }
    public int getMaxPeerscount(){ return MAXPEERSCOUNT; }
}
