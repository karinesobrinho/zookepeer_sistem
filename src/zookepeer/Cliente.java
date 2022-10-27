package zookepeer;

import java.util.Scanner;

public class Cliente {
    String ip1;
    int port1;

    String ip2;
    int port2;

    String ip3;
    int port3;

    public Cliente(
            //String ip1, int port1, String ip2, int port2, String ip3, int port3
    ) {
        // this.ip1 = ip1; TODO

    }


    public static void main(String args[]) {
        (new Thread() {
            public void run() {
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
                            //No caso do INIT, realize a inicialização do cliente
                            cliente = new Cliente();
                            break;
                        }
                        case "PUT": {
                            //Envio da requisição PUT, capturando do teclado as informações necessárias.
                            if (cliente != null) {
                                System.out.println("Digite a key");
                                String key = entrada.nextLine();

                                System.out.println("Digite o value");
                                String value = entrada.nextLine();
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
            }
        }).start();
    }
}
