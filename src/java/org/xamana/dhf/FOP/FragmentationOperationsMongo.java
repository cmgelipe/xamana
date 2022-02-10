/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.dhf.FOP;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.List;
import java.util.stream.Collectors;
import org.bson.Document;
import org.xamana.datos.AccesoDatosMG;
import org.xamana.db.bd;

/**
 *
 * @author abryn
 */
public class FragmentationOperationsMongo implements FragmentationOperations {

    AccesoDatosMG ACD;
    Long currentTuples;
    private List<String> atributes;
    String database;
    String table;

    public FragmentationOperationsMongo(bd bd) {
        ACD = bd.getAccexternomongo();
        this.database = bd.getNombd();
        this.table = bd.getTabla().getNombre();
    }

    @Override
    public void initialize() {
        this.atributes = this.ACD.atributos(database, table).stream()
                .filter(v -> !v.contains("_id"))
                .collect(Collectors.toList());
        MongoDatabase db = this.ACD.getMongoClient().getDatabase(this.database);
        MongoCollection<Document> colec = db.getCollection(this.table);
        this.currentTuples = (Long) colec.countDocuments();
        System.out.println(this.currentTuples);
    }

    public List<String> getAtributes() {
        return atributes;
    }

    @Override
    public long getCurrentTuples() {
        return this.currentTuples;
    }

}
