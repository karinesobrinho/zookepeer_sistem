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

                        String text = bufferedReader.readLine();
                        System.out.println("Recebido no servidor " + text);

                        Gson gson = new Gson(); // conversor
                        Mensagem msg = gson.fromJson(text, Mensagem.class);

                        select(msg, text);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void select(Mensagem msg, String text) throws IOException {
        String type = msg.getType();

        if (type.equals("PUT")) {
            System.out.println("put encontrado");
            putRecived(msg.getKey(), msg.getValue(), text);
        } else if (type.equals("GET")) {
            System.out.println("get encontrado");
            getRecived(msg.getKey(), text);

        } else if (type.equals("REPLICATION")) {
            System.out.println("Replication encontrado");
            replicationRecived(msg.getKey(), msg.getValue(), msg.getTimestamp());
        }
    }

    private void replicationRecived(String key, String value, String timestamp) throws IOException {
        dataTable.put(key, value);
        timestamps.put(key, Timestamp.valueOf(timestamp));

        Mensagem msg = new Mensagem();

        msg.setType("REPLICATION_OK");
        msg.setKey(key);
        msg.setValue(value);
        msg.setTimestamp(timestamp);

        // --- transformando em JSON --- //
        Gson gson = new Gson(); // conversor
        String json = gson.toJson(msg);

        // exibindo o JSON //
        System.out.println(json);

        writer.writeBytes(json + "\n");
    }

    public void putRecived(String key, String value, String text) throws IOException {
        //se é o líder trata requisicao
        System.out.println("putRecived");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        new Thread(() -> {
            System.out.println("Thread");
            if (this.lider) {
                if (dataTable.containsKey(key)) {
                    //se já contem chava atualiza valores e timestamp

                    dataTable.remove(key);
                    timestamps.remove(key);

                    dataTable.put(key, value);
                    timestamps.put(key, timestamp);
                    System.out.println("dentro do serv PUT_OK " + timestamp);

                    Mensagem msg = new Mensagem();

                    msg.setType("PUT_OK");
                    msg.setTimestamp(String.valueOf(timestamp));

                    // --- transformando em JSON --- //
                    Gson gson = new Gson(); // conversor
                    String json = gson.toJson(msg);

                    // exibindo o JSON //
                    System.out.println(json);

                    try {
                        writer.writeBytes(json + "\n");
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

                replication(key, value, timestamp, 10098);
                replication(key, value, timestamp, 10097);
                replication(key, value, timestamp, 10099);
                return;
            }
            //TODO call lider to do put

            //se nao envia requisicao para o líder
            try {
                Socket nsocket = new Socket("127.0.0.1", portLider);

                OutputStream outputStream = nsocket.getOutputStream();
                writer = new DataOutputStream(outputStream);
                writer.writeBytes(text + "\n");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void getRecived(String key, String text) throws IOException {
        //se é o líder trata requisicao
        if (this.lider) {
            if (dataTable.containsKey(key)) {
                Mensagem msg = new Mensagem();

                msg.setType("GET_OK");
                msg.setKey(key);
                msg.setValue(dataTable.get(key));
                msg.setTimestamp(String.valueOf(timestamps.get(key)));

                // --- transformando em JSON --- //
                Gson gson = new Gson(); // conversor
                String json = gson.toJson(msg);

                // exibindo o JSON //
                System.out.println(json);

                writer.writeBytes(json + "\n");
            }
            return;
        }

        //se nao envia requisicao para o líder
        try {
            Socket nsocket = new Socket("127.0.0.1", portLider);

            OutputStream outputStream = nsocket.getOutputStream();
            writer = new DataOutputStream(outputStream);
            writer.writeBytes(text + "\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void replication(String key, String value, Timestamp timestamp, int port) {
        if (this.port == port) return;

        try {
            Socket nsocket = new Socket("127.0.0.1", port);
            System.out.println("lider conn criado");

            OutputStream outputStream = nsocket.getOutputStream();
            writer = new DataOutputStream(outputStream);

            Mensagem msg = new Mensagem();

            msg.setType("REPLICATION");
            msg.setKey(key);
            msg.setValue(value);
            msg.setTimestamp(String.valueOf(timestamp));

            // --- transformando em JSON --- //
            Gson gson = new Gson(); // conversor
            String json = gson.toJson(msg);

            // exibindo o JSON //
            System.out.println(json);

            writer.writeBytes(json + "\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void initDataTable() {
        dataTable = new HashMap<>();
        timestamps = new HashMap<>();
    }
}