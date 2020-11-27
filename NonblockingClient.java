import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;


public class ProductClient {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        ByteBuffer input_buffer = ByteBuffer.allocate(1024);

        try
        {
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.connect(new InetSocketAddress("localhost",8880));

           for (int loopcount = 0 ; !sc.finishConnect() ; loopcount++)
            {
                // do something 
                System.out.println("Loop count = " + loopcount);
                try 
                {
                    Thread.sleep(1000);
                } 
                catch (InterruptedException e) 
                {
                    System.err.println(e);
                }
            }

            System.out.println("input the number you want to send:");
            int content = scanner.nextInt();
            byte[] outbuf = ByteBuffer.allocate(100).putInt(content).array();
            ByteBuffer out_buffer = ByteBuffer.wrap(outbuf);
            sc.write(out_buffer);
            

            int check = 0;

            do{
            check = sc.read(input_buffer);
            try{
                Thread.sleep(2000);
            }catch(InterruptedException e) {
                System.out.println(e);
            }
            } while (check == 0);




            String status = new String(input_buffer.array(),0,1);
            if(status.equals("C")){
                System.out.println("Successful");
            }else{
                System.out.println("error");
            }


        }catch (Exception error)
        {
            System.out.println(error);
        }
    }
}
