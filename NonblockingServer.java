import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.lang.*;


public class NonblockingServer {
    public static void main(String[] args) throws Exception
    {
        //value
        Selector selector = Selector.open();
        ArrayList<Integer> queue = new ArrayList<Integer>();
        int[] input_info = new int[2];
        int queue_count = 0;
        int content;
        Scanner scanner = new Scanner(System.in);

        //input queue maximum size
        System.out.print("請輸入 queue maximum size: ");
        int queue_max = scanner.nextInt();
        //input the maximum of queue
        System.out.print("請輸入Product port： ");
        input_info[0] = scanner.nextInt();
        if (input_info[0] > 0)
        {
            System.out.print("請輸入Consumer port： ");
            input_info[1] = scanner.nextInt();

            if (input_info[1] > 0)
            {
                //Server building the port SocketChannel
                for (int port : input_info) {
                    ServerSocketChannel ssc = ServerSocketChannel.open();
                    ssc.configureBlocking(false);
                    ServerSocket ss = ssc.socket();
                    ss.bind(new InetSocketAddress(port));
                    SelectionKey key = ssc.register(selector, SelectionKey.OP_ACCEPT);
                    System.out.println(port + " is on listening");
                }

                while(true)
                {	
                    int num = selector.select();
                    Set selectedKeys = selector.selectedKeys();
                    for (Object o : selectedKeys) {
                        String data = null;
                        SelectionKey key = (SelectionKey) o;

                        //for accept the connect request from client
                        if (key.isAcceptable()) {
                            while(true) {
                                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                                SocketChannel sc = ssc.accept();
                                if(sc == null){
                                    Thread.sleep(500);
                                }else {
                                    sc.configureBlocking(false);
                                    SelectionKey newKey = sc.register(selector, SelectionKey.OP_READ);
                                    System.out.println("Got connection from " + sc);
                                    break;
                                }
                            }
                            //receive the message from client
                        } else if (key.isReadable()) {
                            SocketChannel sc = (SocketChannel) key.channel();
                            try {
                                ByteBuffer input_buffer = ByteBuffer.allocate(1024);
                                ByteBuffer out_buffer = ByteBuffer.allocate(1024);
                               
                                int check = sc.read(input_buffer);
                                System.out.println(check);
                                if (check > 0) {
                                    input_buffer.rewind();
                                    byte[] bytes = new byte[input_buffer.remaining()];
                                    input_buffer.get(bytes,0,bytes.length);
                                    content = ByteBuffer.wrap(bytes).getInt();
                                    System.out.println(content);
                                    if (content > 0) {
                                        if (queue_count == queue_max) {
                                            data = "E";
                                            System.out.println("queue is full");

                                        } else {
                                            System.out.println(content + " is saved");
                                            data = "C";
                                            queue.add(Integer.valueOf(content));
                                            queue_count += 1;
                                        }
                                    } else if (check == 0) {

                                            System.out.println(content + " pop out from queue");
                                            data = Integer.toString(content);
                                            queue.remove(0);
                                            queue_count -= 1;

                                    } else {
                                        System.out.println("nothing happened");
                                    }

                                    if(data != null) {
                                        out_buffer = ByteBuffer.wrap(data.getBytes());
                                        sc.write(out_buffer);
                                    }else{
                                        System.out.println("no data");
                                    }
                                }
                                
                                input_buffer.clear();
                            } catch (IOException error) {
                                System.out.println("error");
                                sc.close();
                            }
                        }
                    }
                }
            }
        }
    }
}
