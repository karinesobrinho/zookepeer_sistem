package zookepeer;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
//import zookepeer.Servidor;

public class Cliente {
    String ip1;
    int port1;

    String ip2;
    int port2;

    String ip3;
    int port3;

    OutputStream os;
    DataOutputStream writer;
    InputStreamReader is;
    BufferedReader reader;

    public Cliente(
            //String ip1, int port1, String ip2, int port2, String ip3, int port3
    ) throws IOException {
        // this.ip1 = ip1; TODO
        process();
    }

    public void process() {
        //new Thread(() -> {
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", 10097);

            this.os = socket.getOutputStream();
            this.writer = new DataOutputStream(os);

            this.is = new InputStreamReader(socket.getInputStream());
            this.reader = new BufferedReader(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void put(String key, String value) throws IOException {
        writer.writeBytes(key + value + '\n');

        String response = reader.readLine(); //block
        System.out.println("do servidor " + response);
    }


    public static void main(String args[]) {

        (new Thread(() -> {
            Scanner entrada = new Scanner(System.in);
            Cliente cliente = null;

            String opcao = "";

            //if(){
            //TODO colocar condicao se ja existem os servidores
            menuServer(entrada);

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
                            cliente = new Cliente();
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
                            String keyToSearch = entrada.nextLine();
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

    public static void menuServer(Scanner entrada) {
        for (int i = 1; i < 4; i++) {
            System.out.println("Digite o ip do servidor " + i + " a ser inicializado");
            String ip = entrada.nextLine();

            System.out.println("Digite a porta do servidor " + i + " a ser inicializado");
            int port = Integer.parseInt(entrada.nextLine());

            System.out.println("Digite o ip do servidor líder");
            String ipLider = entrada.nextLine();

            System.out.println("Digite a porta do servidor líder");
            int portLider = Integer.parseInt(entrada.nextLine());

            new Servidor(ip, port, ipLider, portLider);
        }
    }
}
