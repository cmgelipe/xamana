/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.dhf.FOP;

import java.util.ArrayList;
import java.util.Map;
import org.xamana.datos.AccesoDatos;
import org.xamana.db.bd;

/**
 *
 * @author abryn
 */
public class FragmentationOperationsMySQL implements FragmentationOperations {

    AccesoDatos ACD;
    Long currentTuples;
    String database;
    String table;

    public FragmentationOperationsMySQL(bd bd) {
        this.table = bd.getTabla().getNombre();
        this.database = bd.getNombd();
        ACD = bd.getAccexternomysql();
    }

    @Override
    public void initialize() throws Exception {
        String query = "SELECT COUNT(*) FROM " + this.table;
        ArrayList arr = null;
        if (this.ACD.conectar()) {
            arr = this.ACD.ejecutarConsulta(query);
            if (arr != null) {
                currentTuples = ((Double) ((ArrayList) arr.get(0)).get(0)).longValue();
            }
        }
        this.ACD.desconectar();
    }

    public void getTuplesByPredicate(Map<String, Long> predicates) throws Exception {

        String query = "SELECT COUNT(*) FROM " + this.table + " WHERE ";
        ArrayList arr = null;
        Long tuples;
        if (this.ACD.conectar()) {
            for (Map.Entry<String, Long> item : predicates.entrySet()) {
                arr = this.ACD.ejecutarConsulta(query + item.getKey());
                if (arr != null) {
                    tuples = ((Double) ((ArrayList) arr.get(0)).get(0)).longValue();
                    if (tuples > 0) {
                        predicates.put(item.getKey(), tuples);
                    } else {
                        predicates.put(item.getKey(), 1L);
                    }
                }
            }

        }
        this.ACD.desconectar();
    }

    @Override
    public long getCurrentTuples() {
        return this.currentTuples;
    }

}
