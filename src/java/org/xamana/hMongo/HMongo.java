/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.xamana.hMongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import org.xamana.datos.AccesoDatos;
import org.xamana.datos.AccesoDatosMG;
import org.xamana.db.Atributo;
import org.xamana.db.bd;
import org.xamana.dhf.model.Fragment;
import org.xamana.verticalCBIR.model.TablaCosto;

/**
 *
 * @author pipe
 */
public class HMongo {

    private List<Fragment> esquema;
    UploadedFiles files;
    bd oBD;

    public HMongo(bd aThis, UploadedFiles files) {
        this.oBD = aThis;
        this.files = files;
        esquema = new ArrayList<>();
    }

    public void crearEsquema() {
        //MCRUD
        HashMap<String, costoSitio> predicadoCosto = new HashMap<>();
        for (UploadedFile f : files.getFiles()) {
            if (f != null && !"".equals(f.getFileName())) {
                try ( BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(f.getInputStream(), "UTF-8"))) {
                    String actual = "";
                    String ipLinea = "";
                    while ((actual = bufferedReader.readLine()) != null) {
                        if (actual.toLowerCase().contains("command") && actual.toLowerCase().contains(this.oBD.getTabla().getNombre().toLowerCase())) {
                            JSONObject j = new JSONObject(actual);
                            String c = j.get("c").toString().toLowerCase().trim();
                            JSONObject j2 = new JSONObject(j.get("attr").toString());
                            JSONObject j3 = new JSONObject(j2.get("command").toString());
                            String ns = "";
                            int ValorAsignadoOperacion = this.VAO(j2.get("command").toString());
                            if (ValorAsignadoOperacion == 2) {
                                if (j3.has("delete")) {
                                    ns = j3.get("delete").toString().toLowerCase();
                                } else if (j3.has("insert")) {
                                    ns = j3.get("insert").toString().toLowerCase();
                                }
                            } else {
                                ns = (ValorAsignadoOperacion == 1) ? j3.get("find").toString().toLowerCase() : j3.get("update").toString().toLowerCase();
                            }
                            if (ValorAsignadoOperacion != 0 && c.compareTo("command") == 0 && ns.compareTo(this.oBD.getTabla().getNombre().toLowerCase()) == 0) {
                                j2 = new JSONObject(j.get("attr").toString());
                                String remote = j2.get("remote").toString().toLowerCase().trim();
                                ipLinea = remote.substring(0, remote.indexOf(":"));
                                System.out.println(actual);
                                System.out.println("SITIO: " + ipLinea);
                                String where = this.where_statement(actual);
                                for (Atributo a : this.oBD.getTabla().getAtributos()) {
                                    if (where.contains(a.getNombre())) {
                                        Pattern pattern = Pattern.compile(a.getNombre());
                                        Matcher matcher = pattern.matcher(where);
                                        while (matcher.find()) {
                                            String r = where.substring(matcher.start(), where.indexOf("}", matcher.start()));
                                            if (r.contains("$eq") || r.contains("$gt") || r.contains("$gte")
                                                    || r.contains("$in") || r.contains("$lt")
                                                    || r.contains("$lte") || r.contains("$ne")
                                                    || r.contains("$nin")) {
                                                String p = "{\"" + r + "}}";
                                                double tuplas = this.nTuplas(p);
                                                double costoOpe = (ipLinea.compareTo(this.oBD.getDirbd()) == 0)
                                                        ? ValorAsignadoOperacion * 1 * tuplas : ValorAsignadoOperacion * 2 * tuplas;
                                                if (predicadoCosto.containsKey(p)) {
                                                    if (predicadoCosto.get(p).getCostoSitio().containsKey(ipLinea)) {
                                                        predicadoCosto.get(p).getCostoSitio().put(ipLinea, predicadoCosto.get(p).getCostoSitio().get(ipLinea) + costoOpe);
                                                    } else {
                                                        predicadoCosto.get(p).getCostoSitio().put(ipLinea, costoOpe);
                                                    }
                                                } else {
                                                    costoSitio cs = new costoSitio();
                                                    cs.getCostoSitio().put(ipLinea, costoOpe);
                                                    predicadoCosto.put(p, cs);
                                                }
                                            } else {
                                                String p = "{\"" + r + "}";
                                                double tuplas = this.nTuplas(p);
                                                double costoOpe = (ipLinea.compareTo(this.oBD.getDirbd()) == 0)
                                                        ? ValorAsignadoOperacion * 1 * tuplas : ValorAsignadoOperacion * 2 * tuplas;
                                                if (predicadoCosto.containsKey(p)) {
                                                    if (predicadoCosto.get(p).getCostoSitio().containsKey(ipLinea)) {
                                                        predicadoCosto.get(p).getCostoSitio().put(ipLinea, predicadoCosto.get(p).getCostoSitio().get(ipLinea) + costoOpe);
                                                    } else {
                                                        predicadoCosto.get(p).getCostoSitio().put(ipLinea, costoOpe);
                                                    }
                                                } else {
                                                    costoSitio cs = new costoSitio();
                                                    cs.getCostoSitio().put(ipLinea, costoOpe);
                                                    predicadoCosto.put(p, cs);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        HashMap<String, Double> atributoValor = new HashMap<>();
        //ALP
        if (!predicadoCosto.isEmpty()) {

            for (Atributo a : this.oBD.getTabla().getAtributos()) {
                for (String clave : predicadoCosto.keySet()) {
                    if (clave.contains(a.getNombre())) {
                        //sumar valores de sitios por predicado
                        double costoPredicadoSitio = 0.0;
                        for (String sitio : predicadoCosto.get(clave).getCostoSitio().keySet()) {
                            costoPredicadoSitio += predicadoCosto.get(clave).getCostoSitio().get(sitio);
                        }
                        if (atributoValor.containsKey(a.getNombre())) {
                            atributoValor.put(a.getNombre(), atributoValor.get(a.getNombre()) + costoPredicadoSitio);
                        } else {
                            atributoValor.put(a.getNombre(), costoPredicadoSitio);
                        }
                    }
                }
            }
        }
        String atributo = "";
        if (!atributoValor.isEmpty()) {
            double valor = 0.0;
            for (String clave : atributoValor.keySet()) {
                if (atributoValor.get(clave) >= valor) {
                    atributo = clave;
                    valor = atributoValor.get(clave);
                }
            }
        }
        System.out.println("Atributo que define: " + atributo);
        //Tabla final
        HashMap<String, costoSitio> predicadoCostoFiltrado = new HashMap<>();
        if (!atributo.isEmpty()) {
            for (String clave : predicadoCosto.keySet()) {
                if (clave.contains(atributo)) {
                    predicadoCostoFiltrado.put(clave, predicadoCosto.get(clave));
                }
            }
        }
        if (!predicadoCostoFiltrado.isEmpty()) {
            for (String clave : predicadoCostoFiltrado.keySet()) {
                Fragment f = new Fragment();
                f.setPredicate(clave);
                //donde asignar
                double sitioMasCostoso = 0.0;
                double costoFragmento = 0.0;
                String sitioAsignacion = "";
                for (String s : predicadoCostoFiltrado.get(clave).getCostoSitio().keySet()) {
                    if (predicadoCostoFiltrado.get(clave).getCostoSitio().get(s) >= sitioMasCostoso) {
                        sitioMasCostoso = predicadoCostoFiltrado.get(clave).getCostoSitio().get(s);
                        sitioAsignacion = s;
                    }
                    costoFragmento += predicadoCostoFiltrado.get(clave).getCostoSitio().get(s);
                }
                f.setPerformance(costoFragmento);
                f.setSite(sitioAsignacion);
                this.esquema.add(f);
            }
        }
        this.nombrar();
        //traslape, filtrado y fragmento sin predicado
        this.traslape();
        this.setEsquema(this.filtrado());
        System.out.println("ESQUEMA HORIZONTAL");
        this.esquema.forEach(x -> System.out.println(x.getPredicate() + " " + x.getPredicadoTraslape()));
    }

    int VAO(String linea) {
        int ret = 0;
        JSONObject j3 = null;
        try {
            j3 = new JSONObject(linea);
            if (j3.has("find")) {
                ret = 1;
            } else if (j3.has("delete") || j3.has("insert")) {
                ret = 2;
            } else if (j3.has("update")) {
                ret = 3;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    private String where_statement(String actual) {
        String ret = "";
        //mongo
        JSONObject j;
        try {
            j = new JSONObject(actual);
            String c = j.get("c").toString().toLowerCase().trim();
            JSONObject j2 = new JSONObject(j.get("attr").toString());
            JSONObject j3 = new JSONObject(j2.get("command").toString());
            if (j3.has("find")) {
                if (j3.has("filter")) {
                    JSONObject j4 = new JSONObject(j3.get("filter").toString());
                    ret = j4.toString();
                }
            } else if (j3.has("delete")) {
                if (j3.has("deletes")) {
                    JSONArray j4 = j3.getJSONArray("deletes");
                    if (j4.length() >= 0) {
                        JSONObject j5 = new JSONObject(j4.get(0) + "");
                        if (j5.has("q")) {
                            ret = j5.get("q").toString();
                        }
                    }
                }
            } else if (j3.has("insert")) {
                //insert one
                if (j3.has("documents")) {
                    JSONArray arr = j3.getJSONArray("documents");
                    if (arr.length() >= 0) {
                        ret = arr.get(0).toString();
                    }
                }
            } else if (j3.has("update")) {
                if (j3.has("updates")) {
                    JSONArray j4 = j3.getJSONArray("updates");
                    if (j4.length() >= 0) {
                        JSONObject j5 = new JSONObject(j4.get(0) + "");
                        if (j5.has("q")) {
                            ret = j5.get("q").toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private double nTuplas(String predicado) {
        System.out.println("Contar tuplas de: " + predicado);
        double count1 = 0.0;
        try {
            //mongo
            if (this.oBD.getAccexternomongo().conectar()) {
                MongoCollection<Document> doc = this.oBD.getAccexternomongo().getMongoClient().getDatabase(this.oBD.getNombd()).getCollection(this.oBD.getTabla().getNombre());
                Document d = Document.parse((predicado.isEmpty()) ? "{}" : predicado);
                count1 = doc.countDocuments(d);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return count1;
    }

    /**
     * @return the esquema
     */
    public List<Fragment> getEsquema() {
        return esquema;
    }

    /**
     * @param esquema the esquema to set
     */
    public void setEsquema(List<Fragment> esquema) {
        this.esquema = esquema;
    }

    private void nombrar() {
        for (int i = 0; i < this.getEsquema().size(); i++) {
            this.getEsquema().get(i).setName(this.oBD.getTabla().getNombre() + "_f" + i);
        }
    }

    private List<Fragment> filtrado() {
        ArrayList<Fragment> arr = new ArrayList<>();
        //eliminar fragmentos que no contienen tuplas
        for (Fragment f : this.esquema) {
            String predicado = f.getPredicadoTraslape();
            if (this.nTuplas(predicado) > 0) {
                arr.add(f);
            }
        }
        return arr;
    }

    private void traslape() {
        Collections.sort(this.esquema, (Fragment f1, Fragment f2) -> new Double(f2.getPerformance()).compareTo(new Double(f1.getPerformance())));
        //producir predicados con traslape
        String negado = "";
        for (Fragment f : this.esquema) {
            String campo = f.getPredicate().substring(1, f.getPredicate().indexOf(":"));
            String valor = f.getPredicate().substring(f.getPredicate().indexOf(":") + 1, f.getPredicate().length() - 1);
            if (negado.isEmpty()) {
                if (!valor.contains("$eq") && !valor.contains("$gt") && !valor.contains("$gte")
                        && !valor.contains("$in") && !valor.contains("$lt")
                        && !valor.contains("$lte") && !valor.contains("$ne")
                        && !valor.contains("$nin")) {
                    valor = "{$eq:" + valor + "}";
                    f.setPredicadoTraslape("{" + campo + ":" + valor + "}");
                } else {
                    f.setPredicadoTraslape("{" + campo + ":" + valor + "}");
                }
            } else {
                f.setPredicadoTraslape(negado + f.getPredicate());
            }
            if (!valor.contains("$eq") && !valor.contains("$gt") && !valor.contains("$gte")
                    && !valor.contains("$in") && !valor.contains("$lt")
                    && !valor.contains("$lte") && !valor.contains("$ne")
                    && !valor.contains("$nin")) {
                valor = "{$eq:" + valor + "}";
            }
            negado = "{" + campo + ":{$not:" + valor + "}}," + negado;
        }
        for (Fragment f2 : this.esquema) {
            if (f2.getPredicadoTraslape().length() >= 1 && f2.getPredicadoTraslape().charAt(f2.getPredicadoTraslape().length() - 1) == ',') {
                f2.setPredicadoTraslape(f2.getPredicadoTraslape().substring(0, f2.getPredicadoTraslape().length() - 1).toLowerCase());
                f2.setTuples((long) this.nTuplas(f2.getPredicadoTraslape()));
            }
            f2.setPredicadoTraslape("{$and:[" + f2.getPredicadoTraslape() + "]}");
            f2.setTuples((long) this.nTuplas(f2.getPredicadoTraslape()));
        }
        if (negado.length() >= 1 && negado.charAt(negado.length() - 1) == ',') {
            negado = negado.substring(0, negado.length() - 1).toLowerCase();
        }
        //frag Sin predicado
        String and = "{$and:[" + negado + "]}";
        double tuplas = this.nTuplas(and);
        if (tuplas > 0) {
            Fragment fsp = new Fragment();
            fsp.setName("Sin predicado");
            fsp.setPerformance(0);
            fsp.setPredicate(negado);
            fsp.setPredicadoTraslape(negado);
            fsp.setTuples((long) tuplas);
            this.getEsquema().add(fsp);
        }

    }

    public void aplicarEsquema() {
        try {
            this.oBD.setProgress1(1);
            int percent = 100 / this.esquema.size();
            this.oBD.guardarDatos();
            String sql = "";
            for (Fragment ta : this.esquema) {
                AccesoDatosMG accDestino = new AccesoDatosMG(ta.getSite() + ":" + this.oBD.getPortbd(), this.oBD.getNombd(), this.oBD.getTabla().getNombre(), this.oBD.getUsubd(), this.oBD.getPassbd());
                AccesoDatosMG accOrigen = new AccesoDatosMG(this.oBD.getDirbd() + ":" + this.oBD.getPortbd(), this.oBD.getPortbd(), this.oBD.getTabla().getNombre(), this.oBD.getUsubd(), this.oBD.getPassbd());
                if (accDestino.conectar() && accOrigen.conectar()) {
                    //Destino
                    MongoDatabase dbDestino = accDestino.getMongoClient().getDatabase(this.oBD.getNombd());
                    MongoCollection<Document> docDestino = dbDestino.getCollection(ta.getName());
                    dbDestino.createCollection(ta.getName());

                    //Origen
                    MongoDatabase dbOrigen = accOrigen.getMongoClient().getDatabase(this.oBD.getNombd());
                    MongoCollection<Document> docOrigen = dbOrigen.getCollection(this.oBD.getTabla().getNombre());

                    Document d = Document.parse((ta.getPredicadoTraslape().isEmpty()) ? "{}" : ta.getPredicadoTraslape());
                    MongoCursor<Document> resultado = docOrigen.find(d).iterator();
                    while (resultado.hasNext()) {
                        docDestino.insertOne(resultado.next());
                    }
                }
                //Insertar a XAMANA
                //SITIO
                String id_sitio = "";
                String idFragmento = "";

                AccesoDatos accXamana = new AccesoDatos();
                if (accXamana.conectar()) {
                    sql = "insert into Sitio (direccion) "
                            + "select * from (select '" + ta.getSite() + "' as direccion ) as tmp "
                            + "where not exists (select direccion from Sitio where direccion='" + ta.getSite() + "') limit 1;";
                    accXamana.ejecutarComando(sql);
                    sql = "select id_sitio from Sitio where direccion='" + ta.getSite() + "'";

                    id_sitio = "" + ((ArrayList) accXamana.ejecutarConsulta(sql).get(0)).get(0);

                    //FRAGMENTO
                    accXamana.ejecutarComando("insert into Fragmento(nombre, id_tabla, id_sitio, noperaciones, ndesempenio, isCBIR) value ('"
                            + ta.getName() + "', " + this.oBD.getTabla().getIdTabla()
                            + ", " + id_sitio + ",0," + ta.getPerformance() + ",0)");
                    idFragmento = "" + ((ArrayList) accXamana.ejecutarConsulta("Select LAST_INSERT_ID()").get(0)).get(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
