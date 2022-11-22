package zookepeer;

import java.io.*;
import java.net.Socket;
import java.util.*;
//import zookepeer.Servidor;
import com.google.gson.Gson;

public class Cliente {
    OutputStream os;
    DataOutputStream writer;
    InputStreamReader is;
    BufferedReader reader;
    Map<String, String> clientDataTable;
    Map<Integer, ArrayList<String>> servers;

    public Cliente(Map<Integer, ArrayList<String>> servers) throws IOException {
        this.servers = servers;
        clientDataTable = new HashMap<String, String>();
    }

    public void process(String host, int port) {
        //new Thread(() -> {

        System.out.println("host: " + host + " port " + port);
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", port);
            System.out.println("Cliente criado");

            this.os = socket.getOutputStream();
            this.writer = new DataOutputStream(os);

            this.is = new InputStreamReader(socket.getInputStream());
            this.reader = new BufferedReader(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void put(String key, String value) throws IOException {
        ArrayList<String> host = servers.get(1);
        process(host.get(0), Integer.parseInt(host.get(1)));

        // --- criar objeto cliente --- //
        Mensagem msg = new Mensagem();

        msg.setType("PUT");
        msg.setKey(key);
        msg.setValue(value);

        // --- transformando em JSON --- //
        Gson gson = new Gson(); // conversor
        String json = gson.toJson( msg );

        // exibindo o JSON //
        System.out.println( json );
        
        writer.writeBytes(json + "\n");
        System.out.println( "depois" );

        String response = reader.readLine(); //block
        System.out.println("do servidor " + response);

        //if(response.equals("PUT_OK"))
        clientDataTable.put(key, value);
    }

    public void get(String key) throws IOException {
        ArrayList<String> host = servers.get(1);
        process(host.get(0), Integer.parseInt(host.get(1)));

        writer.writeBytes("GET " + key + '\n');

        String response = reader.readLine(); //block
        System.out.println("do servidor " + response);
    }


    public static void main(String args[]) {

        (new Thread(() -> {
            Scanner entrada = new Scanner(System.in);
            Cliente cliente = null;
            //new Mensagem("type", "key", "value", "timestamp");

            String opcao = "";

            //if(){
            //TODO colocar condicao se ja existem os servidores
            Map<Integer, ArrayList<String>> servers = menuServer(entrada);

            while (!opcao.equals("QUIT")) {//se usuario digitar quit sai do menu
                System.out.println("Selecione uma das opções abaixo:");
                System.out.println("INIT");
                System.out.println("PUT");
                System.out.println("GET");

                opcao = entrada.nextLine();

                switch (opcao) {
                    case "INIT": {
                        //No caso do INIT, realize a inicialização do cliente
                        try {
                            cliente = new Cliente(servers);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
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

    public static Map<Integer, ArrayList<String>> menuServer(Scanner entrada) {
        Map<Integer, ArrayList<String>> servers = new HashMap<>();

        for (int i = 1; i < 4; i++) {
            System.out.println("Digite o ip do servidor " + i + " a ser inicializado");
            String ip = entrada.nextLine();

            System.out.println("Digite a porta do servidor " + i + " a ser inicializado");
            int port = Integer.parseInt(entrada.nextLine());

            System.out.println("Digite o ip do servidor líder");
            String ipLider = entrada.nextLine();

            System.out.println("Digite a porta do servidor líder");
            int portLider = Integer.parseInt(entrada.nextLine());

            ArrayList<String> list = new ArrayList<>();
            list.add(ip);
            list.add(String.valueOf(port));
            servers.put(i, list);

            new Servidor(ip, port, ipLider, portLider);
        }
        System.out.println(servers);
        return servers;
    }
}
