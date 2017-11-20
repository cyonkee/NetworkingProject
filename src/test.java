import java.util.Arrays;

public class test {


    public static void main(String[] args){
        String y = "1";
        int i ;
        byte[] x = Arrays.copyOf(y.getBytes(), 4);
        for (i = 0; i<x.length; i++) {
            System.out.println(x[i]);
        }
    }
}
