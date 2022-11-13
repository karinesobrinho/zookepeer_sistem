package zookepeer;

import java.io.*;
import java.net.*;

public class Servidor {
    String ip;
    int port;
    String ipLider;
    int portLider;

    public Servidor(String ip, int port, String ipLider, int portLider) {
        this.ip = ip;//TODO add ip to the logic
        this.port = port;
        this.ipLider = ipLider;
        this.portLider = portLider;

        initSocket();

        if(port == portLider){
            //initDataTable();
        }
    }

    public void initSocket(){
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            new Thread(() -> {
                while (true) {

                    try {

                        System.out.println("esperando conexao na porta " + port);
                        Socket no = serverSocket.accept(); //block
                        System.out.println("conexao ok");

                        InputStreamReader is = new InputStreamReader(no.getInputStream());
                        BufferedReader bufferedReader = new BufferedReader(is);

                        OutputStream os = no.getOutputStream();
                        DataOutputStream writer = new DataOutputStream(os);

                        String text = bufferedReader.readLine();

                        writer.writeBytes(text.toUpperCase() + '\n');
                        System.out.println(text);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }).start();
        } catch (Exception e) {

        }
    }
}