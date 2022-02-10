/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.dhf.schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.xamana.datos.AccesoDatos;
import org.xamana.db.bd;
import org.xamana.dhf.model.Fragment;
import org.xamana.dhf.model.MCRUD;

/**
 *
 * @author abryn
 */
public class BuilderFragmentsMySQL implements BuilderFraments, Serializable {

    List<String> operations;

    AccesoDatos ACD;
    String table;
    String address;
    String password;
    String user;
    String port;
    String nameDatabase;
    String header;
    String fragmentTable;
    String queryCreateTable;
    String queryTable;
    String typesTable;
    String createAtributes;
    String insertAttributes;
    MCRUD attribute;
    String primaryKey;

    public BuilderFragmentsMySQL(bd bd) {
        this.ACD = bd.getAccexternomysql();
        this.operations = new ArrayList<>();
        this.table = bd.getTabla().getNombre();
        this.address = bd.getDirbd();
        this.password = bd.getPassbd();
        this.user = bd.getUsubd();
        this.port = bd.getPortbd();
        this.nameDatabase = bd.getNombd();

    }

    @Override
    public List<Fragment> buildDesignSchema(List<MCRUD> mcrud, MCRUD attribute) throws Exception {
        int id = 1;
        Fragment fragment = null;
        List<Fragment> schema = new ArrayList<>();
        long tuples = 0L;
        for (MCRUD item : mcrud) {
            tuples = countQuery(item.getPredicate());
            if (tuples > 0) {
                fragment = new Fragment();
                fragment.setName(this.table + "_" + id);
                fragment.setPredicate(item.getPredicate());
                fragment.setSite(item.getSite());
                fragment.setTuples(tuples);
                fragment.setApproximateOperations(item.getFrequency());
                fragment.setPerformance(item.getPerformance());
                schema.add(fragment);
                id++;
            }
        }
        tuples = countQuery("");
        if (tuples > 0) {
            fragment = new Fragment();
            fragment.setName(this.table + "_" + id);
            fragment.setPredicate("Sin predicado");
            fragment.setSite(this.address);
            fragment.setTuples(tuples);
            fragment.setApproximateOperations(0L);
            schema.add(fragment);
        }
        this.operations.clear();
        return schema;
    }

    private long countQuery(String predicate) throws Exception {

        long count = 0L;
        ArrayList arr = null;
        String query = predicate.isEmpty() ? null
                : "SELECT COUNT(*) FROM " + table + " WHERE " + predicate;
        if (this.ACD.conectar()) {
            if (query != null && this.operations.isEmpty()) {
                arr = this.ACD.ejecutarConsulta(query);
            }
            if (query != null && this.operations.size() > 0) {
                query = query + " AND " + String.join(" AND ", this.operations);
                arr = this.ACD.ejecutarConsulta(query);
            }
            if (query == null) {
                query = "SELECT COUNT(*) FROM "
                        + table + " WHERE " + String.join(" AND ", this.operations);
                arr = this.ACD.ejecutarConsulta(query);
            }

        }
        if (!predicate.equals("")) {
            this.operations.add("NOT " + predicate);
        }
        if (arr != null) {
            count = ((Double) ((ArrayList) arr.get(0)).get(0)).longValue();
        }
        this.ACD.desconectar();
        return count;
    }

    @Override
    public void buildSchema(List<Fragment> schema, Register register) throws Exception {
        if (this.ACD.conectar()) {
            List<String> atributos = this.ACD.atributos(this.nameDatabase, this.table);
            this.primaryKey = "";
            String queryPK = "SELECT k.column_name "
                    + " FROM information_schema.table_constraints t"
                    + " JOIN information_schema.key_column_usage k"
                    + " USING(constraint_name,table_schema,table_name)"
                    + " WHERE t.constraint_type='PRIMARY KEY'"
                    + " AND t.table_schema='" + this.nameDatabase + "'"
                    + " AND t.table_name='" + this.table + "'";
            ArrayList arr = this.ACD.ejecutarConsulta(queryPK);
            if (arr != null) {
                this.primaryKey = "" + ((ArrayList) arr.get(0)).get(0);
            }
            this.buildQueryCreatetable(atributos);
            for (Fragment fragment : schema) {
                this.queryCreateTable = "CREATE TABLE " + fragment.getName() + createAtributes;
                AccesoDatos tempACD = new AccesoDatos("127.0.0.1:" + this.port, this.nameDatabase, this.user, this.password);
                List<String> data = getRows(fragment.getPredicate());
                if (tempACD.conectar()) {
                    tempACD.ejecutarComando(this.queryCreateTable);
                    for (String row : data) {
                        String tempQuery = "INSERT INTO " + fragment.getName() + "(" + this.queryTable + ")"
                                + " VALUES" + row;

                        tempACD.ejecutarComando(tempQuery);
                    }

                    register.fragment(fragment, attribute, table);

                }

            }

        }
    }

    private void buildQueryCreatetable(List<String> attributes) throws Exception {
        this.createAtributes = "(";
        this.queryTable = "";
        this.typesTable = "";
        for (String attribute : attributes) {
            String[] attributeValues = attribute.split(":");

            String value = BuildTypesMySQL.determineTypes(attributeValues[1], attributeValues[2]);
            if (attributeValues[0].contains(this.primaryKey)) {
                this.createAtributes += attributeValues[0] + " " + value + "PRIMARY KEY,";
            } else {
                this.createAtributes += attributeValues[0] + " " + value + ",";
            }

            this.typesTable += attributeValues[1] + ",";
            this.queryTable += attributeValues[0] + ",";
        }
        this.createAtributes = createAtributes.substring(0, createAtributes.length() - 1);
        this.queryTable = queryTable.substring(0, queryTable.length() - 1);
        this.typesTable = this.typesTable.substring(0, typesTable.length() - 1);
        this.createAtributes += ")";
    }

    private List<String> getRows(String predicate) throws Exception {
        ArrayList arr = null;
        List<String> result = new ArrayList<>();
        String query = predicate.contains("Sin predicado") ? null
                : "SELECT " + this.queryTable + " FROM " + table + " WHERE " + predicate;
        if (this.ACD.conectar()) {
            if (query != null && this.operations.isEmpty()) {
                arr = this.ACD.ejecutarConsulta(query);
            }
            if (query != null && this.operations.size() > 0) {
                query = query + " AND " + String.join(" AND ", this.operations);
                arr = this.ACD.ejecutarConsulta(query);
            }
            if (query == null) {
                query = "SELECT " + queryTable + " FROM "
                        + table + " WHERE " + String.join(" AND ", this.operations);
                arr = this.ACD.ejecutarConsulta(query);
            }
        }

        if (!predicate.equals("")) {
            this.operations.add("NOT " + predicate);
        }
        for (int i = 0; i < arr.size(); i++) {
            ArrayList row = (ArrayList) arr.get(i);
            String[] types = this.typesTable.split(",");
            String rowWithData = "(";
            for (int j = 0; j < row.size(); j++) {
                rowWithData += "" + BuildTypesMySQL.transformValue("" + row.get(j), types[j]) + ",";

            }
            rowWithData = rowWithData.substring(0, rowWithData.length() - 1);
            rowWithData += ")";
            result.add(rowWithData);
        }

        return result;
    }

}
