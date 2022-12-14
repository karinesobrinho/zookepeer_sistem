package zookepeer;

public class Mensagem {
    //Classe nao possui construtor, só parametros que são acessados por getters e setters,
    //A transformação para json acontece fora da classe
    String type;
    String key;
    String value;
    String timestamp;
    String clientAddress;

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getClientAddress() {return clientAddress;}

    public void setType(String type) {
        this.type = type;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setClientAddress(String clientAddress) {this.clientAddress = clientAddress;}
}