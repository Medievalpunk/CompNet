import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Test {



    public static void main(String[] args) throws Exception {
        String ip="192.168.0.1";

        String[] s_adr=ip.split("\\.");
        byte[] adr= new byte[]{(byte)256+16,(byte)168,0,1};
        for (int i =0;i<4;i++)
        {
            System.out.println(String.format("%h : %s",adr[i],s_adr[i]));
        }


    }
}
