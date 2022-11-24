package zookepeer;

import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.*;
import com.google.gson.Gson;

public class Cliente {
    OutputStream os;
    DataOutputStream writer;
    InputStreamReader is;
    BufferedReader reader;
    Map<String, String> clientDataTable;
    Map<String, Timestamp> timestamps;
    String ip1;
    int port1;
    String ip2;
    int port2;
    String ip3;
    int port3;

    public Cliente(String ip1, int port1, String ip2, int port2, String ip3, int port3) throws IOException {
        this.ip1 = ip1;
        this.port1 = port1;

        this.ip2 = ip2;
        this.port2 = port2;

        this.ip3 = ip3;
        this.port3 = port3;

        clientDataTable = new HashMap<>();
        timestamps = new HashMap<>();

        //adiciona tempo futuro para teste do get fail
        clientDataTable.put("future", "hard_coded");
        timestamps.put("future", Timestamp.valueOf("2023-11-22 20:57:45.985"));
    }

    public void process(String host, int port) {
        //new Thread(() -> {

        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", port); //cria socket com porta do serv

            //seta as variaveis de in/out put para comunicacao com o server
            this.os = socket.getOutputStream();
            this.writer = new DataOutputStream(os);

            this.is = new InputStreamReader(socket.getInputStream());
            this.reader = new BufferedReader(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void put(String key, String value) throws IOException {
        process(ip1, port1);// cria conexao com o servidor

        //instancia mensagem com os parametros
        Mensagem msg = new Mensagem();

        msg.setType("PUT");
        msg.setKey(key);
        msg.setValue(value);

        // --- transformando em JSON --- //
        Gson gson = new Gson(); // conversor
        String json = gson.toJson(msg);

        // exibindo o JSON //
        System.out.println(json);

        writer.writeBytes(json + "\n");

        String response = reader.readLine(); //block
        System.out.println("do servidor " + response);

        Gson gsonres = new Gson(); // conversor
        Mensagem res = gsonres.fromJson(response, Mensagem.class);

        clientDataTable.put(key, value);
        timestamps.put(key, Timestamp.valueOf(res.getTimestamp()));
    }

    public void get(String key) throws IOException {
        process(ip1, port1);// cria conexao com o servidor

        //instancia mensagem com os parametros
        Mensagem msg = new Mensagem();

        msg.setType("GET");
        msg.setKey(key);
        msg.setTimestamp(String.valueOf(timestamps.get(key)));

        // --- transformando em JSON --- //
        Gson gson = new Gson(); // conversor
        String json = gson.toJson(msg);

        // exibindo o JSON //
        System.out.println(json);

        writer.writeBytes(json + "\n"); //envia json para o servidor

        String response = reader.readLine(); //block espera resposta do servidor
        System.out.println("Do servidor " + response);
    }


    public static void main(String args[]) {

        (new Thread(() -> {// com a Thread o menu fica sempre ativo
            Scanner entrada = new Scanner(System.in);
            Cliente cliente = null;

            String opcao = "";

            while (!opcao.equals("QUIT")) {//se usuario digitar quit sai do menu
                System.out.println("Selecione uma das opções abaixo:");
                System.out.println("INIT");
                System.out.println("PUT");
                System.out.println("GET");

                opcao = entrada.nextLine();

                switch (opcao) {
                    case "INIT": {
                        //No caso do INIT, realize a inicialização do cliente/servidor
                        System.out.println("Selecione um para inciar:");
                        System.out.println("SERVIDOR");
                        System.out.println("CLIENTE");

                        String op = entrada.nextLine();

                        switch (op) {
                            case "SERVIDOR": {
                                menuServer(entrada);
                                break;
                            }
                            case "CLIENTE": {
                                try {
                                    cliente = menuClient(entrada);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                break;
                            }
                        }
                        break;
                    }
                    case "PUT": {
                        //Envio da requisição PUT, capturando do teclado as informações necessárias.
                        if (cliente != null) {
                            System.out.println("Digite a key");
                            String key = entrada.nextLine();

                            System.out.println("Digite o value");
                            String value = entrada.nextLine();

                            try {
                                cliente.put(key, value);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            System.out.println("Nenhum cliente inicializado no console.");
                        }
                        break;
                    }
                    case "GET": {
                        //Envio da requisição GET, capturando do teclado as informações necessárias.
                        if (cliente != null) {
                            System.out.println("Digite a key a ser procurada");
                            String key = entrada.nextLine();

                            try {
                                cliente.get(key);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            System.out.println("Nenhum cliente inicializado no console.");
                        }

                        break;
                    }
                    default: {
                        //por default diz que a opcao é invalida
                        System.out.println("Opção " + opcao + " inválida.");
                        break;
                    }
                }
            }
            entrada.close();
        })).start();
    }

    public static Cliente menuClient(Scanner entrada) throws IOException {
        //auxiliar no recebimento de parametros do cliente
        ArrayList<String> serverInfo = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            System.out.println("Digite o ip do servidor " + i);
            serverInfo.add(entrada.nextLine());

            System.out.println("Digite a porta do servidor " + i);
            serverInfo.add(entrada.nextLine());
        }
        return new Cliente(serverInfo.get(0), Integer.parseInt(serverInfo.get(1)), serverInfo.get(2), Integer.parseInt(serverInfo.get(3)), serverInfo.get(4), Integer.parseInt(serverInfo.get(5)));
    }

    public static void menuServer(Scanner entrada) {
        //auxiliar no recebimento de parametros do servidor
        System.out.println("Digite o ip do servidor a ser inicializado");
        String ip = entrada.nextLine();

        System.out.println("Digite a porta do servidor a ser inicializado");
        int port = Integer.parseInt(entrada.nextLine());

        System.out.println("Digite o ip do servidor líder");
        String ipLider = entrada.nextLine();

        System.out.println("Digite a porta do servidor líder");
        int portLider = Integer.parseInt(entrada.nextLine());

        new Servidor(ip, port, ipLider, portLider);
    }
}
