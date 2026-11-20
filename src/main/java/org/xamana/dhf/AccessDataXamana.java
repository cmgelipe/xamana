/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.dhf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xamana.datos.AccesoDatos;
import org.xamana.db.bd;
import org.xamana.dhf.model.Fragment;
import org.xamana.dhf.model.MCRUD;

/**
 *
 * @author abryn
 */
public class AccessDataXamana {

    private AccesoDatos xamana;
    private bd bd;
    private long idDatabase;
    private long idTable;
    private Map<String, Long> sites;

    public AccessDataXamana(bd bd) throws Exception {
        this.bd = bd;
    }

    public void registerInfo() throws Exception {
        xamana = new AccesoDatos();
        ArrayList arr = null;
        String query = "INSERT INTO BD (NOMBRE,DIRECCION,GESTOR,USR,PSW,PUERTO)"
                + "VALUES('" + bd.getNombd() + "','" + bd.getDirbd() + "',"
                + "'" + bd.getTipoBD() + "','" + bd.getUsubd() + "','" + bd.getPassbd() + "','" + bd.getPortbd() + "')";
        if (xamana.conectar()) {
            xamana.ejecutarComando(query);
            arr = xamana.ejecutarConsulta("SELECT LAST_INSERT_ID()");
            this.idDatabase = ((Double) ((ArrayList) arr.get(0)).get(0)).longValue();

            query = "INSERT INTO TABLA (nombre,umbral_op,umbral_des,tipo_frag,id_BD) "
                    + "VALUES('" + this.bd.getTabla().getNombre() + "'," + this.bd.getTabla().getUmbralOP() + ","
                    + this.bd.getTabla().getUmbralCO() + ",'" + this.bd.getTabla().getTipoFragment() + "'," + this.idDatabase + ")";
            this.xamana.ejecutarComando(query);
            arr = xamana.ejecutarConsulta("SELECT LAST_INSERT_ID()");
            this.idTable = ((Double) ((ArrayList) arr.get(0)).get(0)).longValue();
            System.out.println(this.idTable);
        }
        xamana.desconectar();
    }

    public void registerSites(List<Fragment> schema) throws Exception {
        this.sites = new HashMap<>();
        xamana = new AccesoDatos();
        ArrayList arr = null;
        if (xamana.conectar()) {
            for (Fragment fragment : schema) {
                if (!this.sites.containsKey(fragment.getSite())) {
                    String query = "INSERT INTO SITIO (direccion)"
                            + "VALUES ('" + fragment.getSite() + "')";
                    int insert = xamana.ejecutarComando(query);
                    if (insert > 0) {
                        arr = xamana.ejecutarConsulta("SELECT LAST_INSERT_ID()");
                    }
                    if (arr != null) {
                        long idSite = ((Double) ((ArrayList) arr.get(0)).get(0)).longValue();
                        this.sites.put(fragment.getSite(), idSite);
                    }
                }
            }
        }
        xamana.desconectar();
    }

    public void registerFragments(Fragment fragment, MCRUD atribute, String type) throws Exception {
        xamana = new AccesoDatos();
        ArrayList arr = null;
        String query;
        long idFragment;
        long idPredicate;
        if (xamana.conectar()) {
            query = "INSERT INTO FRAGMENTO (nombre,id_tabla,id_sitio,noperaciones,cbir,ndesempenio)"
                    + "VALUES ('" + fragment.getName() + "'," + this.idTable + ","
                    + this.sites.get(fragment.getSite()) + "," + fragment.getApproximateOperations() + ",false," + fragment.getPerformance() + ")";
            int insert = xamana.ejecutarComando(query);
            arr = xamana.ejecutarConsulta("SELECT LAST_INSERT_ID()");
            idFragment = ((Double) ((ArrayList) arr.get(0)).get(0)).longValue();
            if (insert > 0) {
                String predicate = fragment.getPredicate();
                if (predicate.contains("'")) {
                    predicate = predicate.replaceAll("'", "");
                }

                query = "INSERT INTO PREDICADO (frecuencia, descripcion,ntuplas)"
                        + "VALUES (" + fragment.getApproximateOperations() + ",'" + predicate + "'," + fragment.getTuples() + ")";
                xamana.ejecutarComando(query);
                arr = xamana.ejecutarConsulta("SELECT LAST_INSERT_ID()");
                idPredicate = ((Double) ((ArrayList) arr.get(0)).get(0)).longValue();
                query = "INSERT INTO FRAGMENTO_PREDICADO (id_fragmento,id_predicado)"
                        + "VALUES(" + idFragment + "," + idPredicate + ")";
                xamana.ejecutarComando(query);

            }

        }
        xamana.desconectar();
    }

    public long getIdTable() {
        return this.idTable;
    }

}
