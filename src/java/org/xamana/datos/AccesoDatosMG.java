package org.xamana.datos;

import java.io.Serializable;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author pipe
 */
public class AccesoDatosMG implements Serializable {

    private static String sUrl = null;  // sUrl de conexión
    private static String sUsr = null; // Usuario
    private static String sPwd = null;  // Contraseña
    private static String tabla = null;
    MongoClient mongoClient;  // La conexión

    public AccesoDatosMG(String url, String nombd, String tabla, String usr, String pwd) throws Exception {
        sUrl = url;//"jdbc:mysql://localhost/Master";
        sUsr = usr;//"root";
        sPwd = pwd;//"sunny_4teamo";
        this.setTabla(tabla);
    }

    public boolean conectar() throws Exception {
        String mongoClientURI = "";
        if (sUsr.isEmpty()) {
            mongoClientURI = "mongodb://" + sUrl + "/";
        } else {
            mongoClientURI = "mongodb://" + sUsr + ":" + sPwd + "@" + sUrl + "/";
        }
        mongoClient = MongoClients.create(mongoClientURI);

        return mongoClient != null;
    }

    /**
     * @return the tabla
     */
    public String getTabla() {
        return tabla;
    }

    /**
     * @param tabla the tabla to set
     */
    public void setTabla(String tabla) {
        this.tabla = tabla;
    }

    public synchronized ArrayList<String> atributos(String nombreBase, String nombre) {
        ArrayList<String> res=new ArrayList<>();
        MongoDatabase db = this.mongoClient.getDatabase(nombreBase);
        MongoCollection<Document> collection = db.getCollection(nombre);
        Document doc = collection.find().first();
        try {
            Iterator js = new JSONObject(doc.toJson()).keys();
            while (js.hasNext()) {
                res.add(js.next().toString());
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return res;
    }
}
