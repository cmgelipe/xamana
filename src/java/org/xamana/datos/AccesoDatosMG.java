package org.xamana.datos;

import java.io.Serializable;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import static com.mongodb.client.model.Aggregates.project;
import com.mongodb.client.model.Projections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
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
    private MongoClient mongoClient;  // La conexión

    public AccesoDatosMG(String url, String nombd, String tabla, String usr, String pwd) throws Exception {
        sUrl = url;//"jdbc:mysql://localhost/Master";
        sUsr = usr;//"root";
        sPwd = pwd;//"sunny_4teamo";
        this.setTabla(tabla);
    }

    public boolean conectar() throws Exception {
        String mongoClientURI = "";
        if (this.isNullorEmpy(sUsr)) {
            mongoClientURI = "mongodb://" + sUrl + "/";
        } else {
            mongoClientURI = "mongodb://" + sUsr + ":" + sPwd + "@" + sUrl + "/";
        }
        setMongoClient(MongoClients.create(mongoClientURI));

        return getMongoClient() != null;
    }
    
    public boolean isNullorEmpy(String q){
        boolean ret=false;
        if(q!=null){
            if(q.isEmpty()){
                ret=true;
            }else{
                ret=false;
            }
        }else{
            ret=true;
        }
        return ret;
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
        MongoDatabase db = this.getMongoClient().getDatabase(nombreBase);
        MongoCollection<Document> collection = db.getCollection(nombre);
        Document doc = collection.find().first();
        try {
            Iterator js = new JSONObject(doc.toJson()).keys();
            while (js.hasNext()) {
                String key=js.next().toString();
                Bson group=project(Projections.computed(key, Document.parse("{$type: '$"+key+"'}")));
                List<Document> r=collection.aggregate(Arrays.asList(group)).into(new ArrayList<>());
                String tipo=""+(new JSONObject(r.get(0).toJson()).get(key));
                System.out.println(key+":"+tipo);
                res.add(key+":"+tipo);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return res;
    }
    
    public synchronized ArrayList<String> tablas(String nombreBase) {
        ArrayList<String> res=new ArrayList<>();
        MongoDatabase db = this.getMongoClient().getDatabase(nombreBase);
        db.listCollectionNames().forEach(x->{res.add(x);});
        return res;
    }

    /**
     * @return the mongoClient
     */
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * @param mongoClient the mongoClient to set
     */
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }
}
