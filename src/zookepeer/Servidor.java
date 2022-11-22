package zookepeer;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class Servidor {
    String ip;
    int port;
    String ipLider;
    int portLider;
    boolean lider;
    ServerSocket serverSocket;
    Map<String, String> dataTable;
    Map<String, Timestamp> timestamps;

    InputStreamReader is;
    BufferedReader bufferedReader;

    OutputStream os;
    DataOutputStream writer;


    public Servidor(String ip, int port, String ipLider, int portLider) {
        this.ip = ip; //TODO add ip to the logic
        this.port = port;
        this.ipLider = ipLider;
        this.portLider = portLider;
        this.lider = false;

        initSocket();
        initDataTable();

        if (port == portLider) {
            System.out.println("é lider");
            this.lider = true;
        } else {
            System.out.println(port + " port lider " + portLider);
        }
    }

    public void initSocket() {
        try {
            serverSocket = new ServerSocket(port);
            new Thread(() -> {
                while (true) {
                    try {
                        System.out.println("esperando conexao na porta " + port);
                        Socket no = serverSocket.accept(); //block
                        System.out.println("conexao ok");

                        is = new InputStreamReader(no.getInputStream());
                        bufferedReader = new BufferedReader(is);

                        os = no.getOutputStream();
                        writer = new DataOutputStream(os);

                        System.out.println("buffer ok");
                        String text = bufferedReader.readLine();
                        System.out.println(text);

                        Gson gson = new Gson(); // conversor
                        Mensagem msg = gson.fromJson(text, Mensagem.class);

                        select(msg);

                        /*com.google.gson.Gson gson = new Gson(); // conversor
                        Mensagem recived = gson.fromJson(text, Mensagem.class);

                        System.out.println("chamo select com isso" + recived);

                        select(recived);

                       text = text.toUpperCase();
                        String[] textSplited = text.split(" ");

                        if (textSplited[0].equals("PUT")) {
                            System.out.println("put encontrado");
                            putRecived(textSplited[1], textSplited[2]);

                        } else if (textSplited[0].equals("GET")) {
                            System.out.println("get encontrado");
                            getRecived(textSplited[1]);

                        } else if (textSplited[0].equals("REPLICATION")) {
                            System.out.println("Replication encontrado");
                            //replicationRecived(textSplited[1], textSplited[2]);
                        }

                        //writer.writeBytes(text + '\n');
                        System.out.println(text);*/

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void select(Mensagem msg) throws IOException {
        String type = msg.getType();
        System.out.println("cheguei em select! type : " + type);

        if (type.equals("PUT")) {
            System.out.println("put encontrado");

            putRecived(msg.getKey(), msg.getValue());


        } else if (type.equals("GET")) {
            System.out.println("get encontrado");
            getRecived(msg.getKey());

        } else if (type.equals("REPLICATION")) {
            System.out.println("Replication encontrado");
            //replicationRecived(textSplited[1], textSplited[2]);
        }
    }

    public void putRecived(String key, String value) throws IOException {
        //se é o líder trata requisicao
        System.out.println("putRecived");
        new Thread(() -> {
            System.out.println("Thread");
            if (this.lider) {
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                if (dataTable.containsKey(key)) {
                    //se já contem chava atualiza valores e timestamp

                    dataTable.remove(key);
                    timestamps.remove(key);

                    dataTable.put(key, value);
                    timestamps.put(key, timestamp);
                    System.out.println("dentro do serv PUT_OK " + timestamp);

                    try {
                        writer.writeBytes("PUT_OK " + timestamp + '\n');
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    dataTable.put(key, value);

                    try {
                        writer.writeBytes("PUT_OK " + timestamp + '\n');
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                replication();
                return;
            }

            //se nao envia requisicao para o líder
        }).start();

    }

    public void getRecived(String key) {
        //se é o líder trata requisicao
        if (this.lider) {
            if (dataTable.containsKey(key)) {
                System.out.println("achei a chave");
            }
            return;
        }

        //se nao envia requisicao para o líder
    }

    public void replication() {
        //TODO replicar informacao para outros servidores
    }

    public void initDataTable() {
        dataTable = new HashMap<>();
        timestamps = new HashMap<>();
    }
}