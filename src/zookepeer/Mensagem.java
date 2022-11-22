package zookepeer;

//import org.json.JSONObject;

public class Mensagem {
    String type;
    String key;
    String value;
    //String timestamp;

    public void setType(String type) {
        this.type = type;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    /*public Mensagem(String type, String key, String value, String timestamp) {//cria um JSONObject
        this.type = type;
        this.key = key;
        this.value = value;
        this.timestamp = timestamp;

        create();
    }

    public JSONObject create(){
        //instancia um novo JSONObject
        org.json.JSONObject my_obj = new org.json.JSONObject();

        //preenche o objeto com os campos: titulo, ano e genero
        my_obj.put("type", type);
        my_obj.put("key", key);
        my_obj.put("value", value);
        my_obj.put("timestamp", timestamp);

        //serializa para uma string e imprime
        String json_string = my_obj.toString();
        System.out.println("objeto json -> " + json_string);
        System.out.println();

        return my_obj;
    }*/
}