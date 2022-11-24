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
        this.ip = ip;
        this.port = port;
        this.ipLider = ipLider;
        this.portLider = portLider;
        this.lider = false;

        initSocket();
        initDataTable();

        if (port == portLider) {
            this.lider = true;
        }
    }

    public void initSocket() {
        try {
            serverSocket = new ServerSocket(port);
            new Thread(() -> { //Thread do servidor sempre ouvindo na porta
                while (true) {
                    try {
                        System.out.println("esperando conexao na porta " + port);
                        Socket no = serverSocket.accept(); //block
                        System.out.println("conexao ok");

                        is = new InputStreamReader(no.getInputStream());
                        bufferedReader = new BufferedReader(is);

                        os = no.getOutputStream();
                        writer = new DataOutputStream(os);

                        //quando recebe converte de string para mensagem e manda para select
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
        //faz triagem por tipo da mensagem
        String type = msg.getType();

        if(type.equals("PUT")) {
            putRecived(msg.getKey(), msg.getValue(), text);
        } else if (type.equals("GET")) {
            getRecived(msg.getKey(), Timestamp.valueOf(msg.getTimestamp()));
        } else if (type.equals("REPLICATION")) {
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

        writer.writeBytes(json + "\n");//responde ao servidor que enviou a mensagem com replication_ok
    }

    public void putRecived(String key, String value, String text) throws IOException {
        //se é o líder trata requisicao
        System.out.println("putRecived");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        new Thread(() -> {
            if (this.lider) {
                //se é o lider processa o put
                Mensagem msg = new Mensagem();

                msg.setType("PUT_OK");
                msg.setKey(key);
                msg.setValue(value);
                msg.setTimestamp(String.valueOf(timestamp));

                // --- transformando em JSON --- //
                Gson gson = new Gson(); // conversor
                String json = gson.toJson(msg);

                if (dataTable.containsKey(key)) {
                    //se já contem chava atualiza valores e timestamp

                    dataTable.remove(key);
                    timestamps.remove(key);

                    dataTable.put(key, value);
                    timestamps.put(key, timestamp);

                    // exibindo o JSON //
                    System.out.println(json);

                    try {
                        writer.writeBytes(json + "\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    dataTable.put(key, value);
                    timestamps.put(key, timestamp);
                    try {
                        writer.writeBytes(json + '\n');
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                //replica para outros servers
                replication(key, value, timestamp, 10098);
                replication(key, value, timestamp, 10097);
                replication(key, value, timestamp, 10099);
                return;
            } else {
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
        }).start();
    }

    public void getRecived(String key, Timestamp timestampClient) throws IOException {
        //se é o líder trata requisicao
        Mensagem msg = new Mensagem();

        //se possui timestamp atualizado ou se timestamp do cliente é null envia get
        if (dataTable.containsKey(key) && (timestamps.get(key).after(timestampClient) || timestampClient == null)) {
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
            return;
        } else if (timestamps.get(key).before(timestampClient)) {
            //se o timestamp for atrasado envia TRY_ANOTHER_SERVER_OR_LATER
            msg.setType("TRY_ANOTHER_SERVER_OR_LATER");

            // --- transformando em JSON --- //
            Gson gson = new Gson(); // conversor
            String json = gson.toJson(msg);

            // exibindo o JSON //
            System.out.println(json);

            writer.writeBytes(json + "\n");
            return;
        }
        //caso contrário nao encontrou a chave e envia nulo para cliente
        msg.setType("NULL");

        Gson gson = new Gson(); // conversor
        String json = gson.toJson(msg);

        writer.writeBytes(msg + "\n");
        return;
    }

    public void replication(String key, String value, Timestamp timestamp, int port) {
        if (this.port == port) return; //nao envia para sua porta, retorna

        try {//envia solicitacao de replicacao
            Socket nsocket = new Socket("127.0.0.1", port);

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

        //future para teste do get com tempo atrasado
        dataTable.put("future", "hard_coded");
        timestamps.put("future", Timestamp.valueOf("2022-11-22 20:57:45.985"));
    }
}