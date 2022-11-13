package zookepeer;

import javafx.scene.text.Text;

import java.io.*;
import java.net.*;

public class Servidor {


    public Servidor() {
        try {
            ServerSocket serverSocket = new ServerSocket(10097);
            new Thread(() -> {
                while (true) {

                    try {

                        System.out.println("esperando conexao na porta 10097");
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