import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by cyonkee on 10/14/17.
 */
public class PeersInfo {
    HashMap map = new HashMap();

    public PeersInfo(ArrayList list){
        fillHashMap(list);
    }

    private void fillHashMap(ArrayList list) {
        int i=0;
        Iterator it = list.iterator();
        for(;it.hasNext();it.next()){
            String[] extractedArray = (String[]) list.get(i);
            Neighbor neighbor = new Neighbor(extractedArray[1],extractedArray[2],extractedArray[3]);
            map.put(extractedArray[0], neighbor);
            i++;
        }
    }

    public HashMap getMap(){
        return map;
    }
}
