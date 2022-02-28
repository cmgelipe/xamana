package org.xamana.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Accumulators.sum;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.xamana.datos.AccesoDatos;
import org.xamana.datos.AccesoDatosMG;
import org.xamana.datos.AccesoDatosPG;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.json.JSONObject;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import org.xamana.crearfrag.BuildTypes;
import org.xamana.verticalCBIR.log.archivos;
import org.xamana.verticalCBIR.model.Query;
import org.xamana.verticalCBIR.model.TablaCosto;
import org.xamana.verticalCBIR.model.atributo;

/**
 *
 * @author pipe
 */
public class bd implements Serializable {

    AccesoDatos accexternomysql;
    private AccesoDatosMG accexternomongo;
    AccesoDatosPG accexternopost;
    private String dirbd;
    private String portbd;
    private String nombd;
    private String usubd;
    private String passbd;
    private String TipoBD = "";
    private Tabla tabla = new Tabla();
    private Integer progress1;
    private List<SelectItem> tablas;
    private HashMap<String, String> NombreIp;

    public boolean conexionexterna() {
        boolean res = false;

        if (TipoBD.compareTo("a") == 0) {
            try {
                accexternomysql = new AccesoDatos(dirbd + ":" + this.portbd, nombd, usubd, passbd);

                if (accexternomysql.conectar()) {
                    res = true;
                    accexternomysql.desconectar();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (TipoBD.compareTo("b") == 0 || TipoBD.compareTo("c") == 0) {
            try {
                accexternopost = new AccesoDatosPG(dirbd + ":" + this.portbd, nombd, usubd, passbd);
                if (accexternopost.conectar()) {
                    res = true;
                    accexternopost.desconectar();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (TipoBD.compareTo("d") == 0) {
            try {
                setAccexternomongo(new AccesoDatosMG(dirbd + ":" + this.portbd, nombd, this.tabla.getNombre(), usubd, passbd));
                if (getAccexternomongo().conectar()) {
                    res = true;
                } else {
                    System.out.println("CONEXION RECHAZADA");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return res;
    }

    /**
     * @return the dirbd
     */
    public String getDirbd() {
        return dirbd;
    }

    /**
     * @param dirbd the dirbd to set
     */
    public void setDirbd(String dirbd) {
        this.dirbd = dirbd;
    }

    /**
     * @return the usubd
     */
    public String getUsubd() {
        return usubd;
    }

    /**
     * @param usubd the usubd to set
     */
    public void setUsubd(String usubd) {
        this.usubd = usubd;
    }

    /**
     * @return the passbd
     */
    public String getPassbd() {
        return passbd;
    }

    /**
     * @param passbd the passbd to set
     */
    public void setPassbd(String passbd) {
        this.passbd = passbd;
    }

    /**
     * @return the TipoBD
     */
    public String getTipoBD() {
        return TipoBD;
    }

    /**
     * @param TipoBD the TipoBD to set
     */
    public void setTipoBD(String TipoBD) {
        this.TipoBD = TipoBD;
    }

    /**
     * @return the tabla
     */
    public Tabla getTabla() {
        return tabla;
    }

    /**
     * @param tabla the tabla to set
     */
    public void setTabla(Tabla tabla) {
        this.tabla = tabla;
    }

    /**
     * @return the nombd
     */
    public String getNombd() {
        return nombd;
    }

    /**
     * @param nombd the nombd to set
     */
    public void setNombd(String nombd) {
        this.nombd = nombd;
    }

    public List<SelectItem> listaAtributosMySQL() throws Exception {
        ArrayList<String> arr;
        this.tabla.setAtributos(new ArrayList<>());
        List<SelectItem> res = new ArrayList<>();
        //accexternomysql = new AccesoDatos(dirbd, nombd, usubd, passbd);
        if (accexternomysql.conectar()) {
            arr = accexternomysql.atributos(nombd, this.getTabla().getNombre());
            accexternomysql.desconectar();
            if (arr != null) {
                arr.forEach((g) -> {
                    res.add(new SelectItem(g, g));
                    String[] t = g.split(":");
                    Atributo att = new Atributo();
                    att.setNombre(t[0]);
                    att.setTipo(t[1]);
                    att.setTamanio(t[2]);
                    this.tabla.getAtributos().add(att);
                });
            }
        } else {
            System.out.println("Conexión incorrecta listaatributosmysql");
        }
        return res;
    }

    public List<SelectItem> listaAtributosMongoDB() throws Exception {
        ArrayList<String> arr;
        this.tabla.setAtributos(new ArrayList<>());
        this.tabla.setAtributos(new ArrayList<>());
        List<SelectItem> res = new ArrayList<>();
        //this.accexternomongo = new AccesoDatosMG(dirbd, nombd, this.tabla.getNombre(), usubd, passbd);
        if (this.getAccexternomongo().conectar()) {
            arr = this.getAccexternomongo().atributos(nombd, this.getTabla().getNombre());
            arr.forEach((h) -> {
                res.add(new SelectItem(h, h));
                String[] t = h.split(":");
                Atributo att = new Atributo();
                att.setNombre(t[0]);
                att.setTipo(t[1]);
                this.tabla.getAtributos().add(att);
            });
        }
        return res;
    }

    public List<SelectItem> listaAtributosPostgreSQL() throws Exception {
        ArrayList<String> arr;
        this.tabla.setAtributos(new ArrayList<>());
        List<SelectItem> res = new ArrayList<>();
        //this.accexternopost=new AccesoDatosPG(dirbd, nombd, usubd, passbd);
        if (this.accexternopost.conectar()) {
            arr = this.accexternopost.atributos(nombd, this.getTabla().getNombre());
            this.accexternopost.desconectar();
            arr.forEach((h) -> {
                res.add(new SelectItem(h, h));
                String[] t = h.split(":");
                Atributo att = new Atributo();
                att.setNombre(t[0]);
                att.setTipo(t[1]);
                att.setTamanio(t[2]);
                this.tabla.getAtributos().add(att);
            });
        }
        return res;
    }

    public ArrayList<esquemaHorMultimedia> valoresDistintosColMultimedia(UploadedFiles files) throws Exception {
        ArrayList<esquemaHorMultimedia> res = null;
        ArrayList arr = null;
        String nombreColumna = this.getTabla().getAtributoMultimedia().split(":")[0];
        String queryRelacional = "select " + nombreColumna + ", count(" + nombreColumna + ") as num from " + this.tabla.getNombre() + " group by " + nombreColumna;
        double nTuplasTabla = this.nTuplasTabla();
        DecimalFormat formateador = new DecimalFormat("##########.##");
        if (this.TipoBD.compareTo("a") == 0) {
            if (this.accexternomysql.conectar()) {
                arr = this.accexternomysql.ejecutarConsulta(queryRelacional);
                this.accexternomysql.desconectar();
            }
        } else if (this.TipoBD.compareTo("b") == 0 || this.TipoBD.compareTo("c") == 0) {
            if (this.accexternopost.conectar()) {
                arr = this.accexternopost.ejecutarConsulta(queryRelacional);
                this.accexternopost.desconectar();
            }
        } else {
            if (this.getAccexternomongo().conectar()) {
                MongoDatabase db = this.getAccexternomongo().getMongoClient().getDatabase(nombd);
                MongoCollection<Document> colec = db.getCollection(this.tabla.getNombre());
                Bson group = group("$" + nombreColumna, sum("num", 1L));
                List<Document> r = colec.aggregate(Arrays.asList(group)).into(new ArrayList<>());
                if (!r.isEmpty()) {
                    res = new ArrayList<>();
                    int t = 0;
                    for (Document d : r) {
                        t++;
                        vOperacionDesempenio valoresOPDES = this.nOperacionesLog(files, "" + (new JSONObject(d.toJson())).get("_id"));
                        esquemaHorMultimedia tupla = new esquemaHorMultimedia();
                        tupla.setNombre(this.tabla.getNombre() + "_f" + (t));
                        tupla.setValor("" + (new JSONObject(d.toJson())).get("_id"));
                        tupla.setNTuplas("" + (new JSONObject(d.toJson())).get("num"));
                        tupla.setPorcentaje(formateador.format((Double.parseDouble(tupla.getNTuplas()) * 100) / nTuplasTabla));
                        tupla.setNOperacionesAprox("" + valoresOPDES.getOperaciones());
                        tupla.setNdesempenio("" + valoresOPDES.getDesempenio());
                        res.add(tupla);
                    }
                }
            }
        }
        if (this.TipoBD.compareTo("a") == 0 || this.TipoBD.compareTo("b") == 0 || this.TipoBD.compareTo("c") == 0) {
            if (arr != null) {
                res = new ArrayList<>();
                for (int t = 0; t < arr.size(); t++) {
                    esquemaHorMultimedia tupla = new esquemaHorMultimedia();
                    vOperacionDesempenio valoresOPDES = this.nOperacionesLog(files, "" + ((ArrayList) arr.get(t)).get(0));
                    tupla.setNombre(this.tabla.getNombre() + "_f" + (t + 1));
                    tupla.setValor("" + ((ArrayList) arr.get(t)).get(0));
                    tupla.setNTuplas("" + ((ArrayList) arr.get(t)).get(1));
                    tupla.setPorcentaje(formateador.format(((Double) ((Double.parseDouble(tupla.getNTuplas()) * 100) / nTuplasTabla)).intValue()));
                    tupla.setNOperacionesAprox(valoresOPDES.getOperaciones() + "");
                    tupla.setNdesempenio(valoresOPDES.getDesempenio() + "");
                    res.add(tupla);
                }
            }
        }
        return res;
    }

    public boolean isHorizontalCBIR() {
        return this.tabla.isCBIR() && this.tabla.getTipoFragment().compareTo("a") == 0;
    }

    public boolean isRelacional() {
        return this.TipoBD.compareTo("a") == 0 || this.TipoBD.compareTo("b") == 0 || this.TipoBD.compareTo("c") == 0;
    }

    public void guardarDatos() {
        String id_bd = "";
        String id_tabla = "";
        String query = "";
        try {
            AccesoDatos acc = new AccesoDatos();
            if (acc.conectar()) {

                if (this.usubd.isEmpty()) {
                    query = "insert into bd (nombre, direccion, puerto, gestor, usr, psw) values('" + this.getNombd() + "','" + this.dirbd + "','" + this.portbd + "','" + this.TipoBD + "',null,null)";
                } else if (!this.usubd.isEmpty() && this.passbd.isEmpty()) {
                    query = "insert into bd (nombre, direccion, puerto, gestor, usr, psw) values('" + this.getNombd() + "','" + this.dirbd + "','" + this.portbd + "','" + this.TipoBD + "','" + this.usubd + "',null)";
                } else {
                    query = "insert into bd (nombre, direccion, puerto, gestor, usr, psw) values('" + this.getNombd() + "','" + this.dirbd + "','" + this.portbd + "','" + this.TipoBD + "','" + this.usubd + "','" + this.passbd + "')";
                }
                acc.ejecutarComando(query);
                ArrayList res = acc.ejecutarConsulta("select LAST_INSERT_ID()");
                id_bd = "" + ((ArrayList) res.get(0)).get(0);
                acc.ejecutarComando("insert into tabla (nombre,umbral_op,umbral_des,tipo_frag,id_BD) values ('" + this.getTabla().getNombre() + "'," + this.getTabla().getUmbralOP() + "," + this.getTabla().getUmbralCO() + ",'" + this.tabla.getTipoFragment() + "'," + id_bd + ")");
                res = acc.ejecutarConsulta("select LAST_INSERT_ID()");
                id_tabla = "" + ((ArrayList) res.get(0)).get(0);
                acc.desconectar();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.tabla.setIdTabla(id_tabla);
    }

    public void fragmentarHorCBIRMySQL(ArrayList<esquemaHorMultimedia> e) {
        try {
            this.guardarDatos();
            String qSitio = "";
            String idSitio = "";
            String[] u = this.getTabla().getAtributoMultimedia().split(":");
            accexternomysql = new AccesoDatos(dirbd + ":" + this.portbd, nombd, usubd, passbd);
            this.getTabla().setAtributoLlave(this.llaveMySQL(accexternomysql, nombd, this.getTabla().getNombre()));
            Atributo attMultimedia = new Atributo();
            Atributo tamanio = new Atributo();
            attMultimedia.setNombre(u[0]);
            attMultimedia.setTipo(u[1]);
            attMultimedia.setTamanio(u[2]);
            AccesoDatos acc = new AccesoDatos();
            if (acc.conectar()) {
                qSitio = "insert into sitio (direccion) values('" + this.getDirbd() + "')";
                acc.ejecutarComando(qSitio);
                ArrayList res = acc.ejecutarConsulta("select LAST_INSERT_ID()");
                idSitio = "" + ((ArrayList) res.get(0)).get(0);
            }
            String att_tipo = "";
            for (Atributo r : this.getTabla().getAtributos()) {
                if (this.transTipoDato(r.getTipo()) != 0) {
                    att_tipo += r.getNombre() + " " + r.getTipo() + ", ";
                } else {
                    if (r.getTipo().compareTo("BIT") != 0) {
                        att_tipo += r.getNombre() + " " + r.getTipo() + "(" + r.getTamanio() + "), ";
                    } else {
                        att_tipo += r.getNombre() + " TINYINT, ";
                    }
                }
            }
            att_tipo = att_tipo.substring(0, att_tipo.length() - 2);
            this.progress1 = 10;
            int t = 100 / e.size();
            for (esquemaHorMultimedia tupla : e) {
                accexternomysql = new AccesoDatos(dirbd + ":" + this.portbd, nombd, usubd, passbd);
                ArrayList<String> idTuplas = null;
                if (this.accexternomysql.conectar()) {
                    ArrayList resultado = null;
                    String query = "create table " + tupla.getNombre() + " (" + att_tipo + ", PRIMARY KEY (" + this.getTabla().getAtributoLlave().getNombre() + "));";
                    this.accexternomysql.ejecutarComando(query);
                    query = "select * from " + this.tabla.getNombre() + " where " + attMultimedia.getNombre() + "='" + tupla.getValor() + "'";
                    resultado = this.accexternomysql.ejecutarConsulta(query);
                    if (resultado != null) {
                        if (!resultado.isEmpty()) {
                            idTuplas = new ArrayList<>();
                            for (int i = 0; i < resultado.size(); i++) {
                                String valores = "";
                                String id = "";
                                for (int j = 0; j < ((ArrayList) resultado.get(i)).size(); j++) {
                                    if (this.getTabla().getAtributos().get(j).getNombre().compareTo(this.getTabla().getAtributoLlave().getNombre()) == 0) {
                                        id = "" + ((ArrayList) resultado.get(i)).get(j);
                                    }
                                    if (this.transTipoDato(this.getTabla().getAtributos().get(j).getTipo()) == 0 || this.transTipoDato(this.getTabla().getAtributos().get(j).getTipo()) == 2) {
                                        valores += "\"" + ((ArrayList) resultado.get(i)).get(j) + "\", ";
                                    } else {
                                        valores += ((ArrayList) resultado.get(i)).get(j) + ", ";
                                    }
                                }
                                valores = valores.substring(0, valores.length() - 2);
                                String q = "INSERT INTO " + tupla.getNombre() + " VALUES (" + valores + ");";
                                this.accexternomysql.ejecutarComando(q);
                                idTuplas.add(id);
                            }
                        }
                    }
                    this.accexternomysql.desconectar();
                }
                acc = new AccesoDatos();
                if (acc.conectar()) {
                    String un = "0";
                    String dos = "0";
                    un = (tupla.getNOperacionesAprox().compareTo("0") == 0) ? "1" : tupla.getNOperacionesAprox();
                    dos = (tupla.getNdesempenio().compareTo("0") == 0) ? "1" : tupla.getNdesempenio();
                    String q = "insert into fragmento (nombre, id_tabla, id_sitio, noperaciones, ndesempenio) values('" + tupla.getNombre() + "'," + this.tabla.getIdTabla() + "," + idSitio + "," + un + "," + dos + ")";
                    acc.ejecutarComando(q);
                    ArrayList res = acc.ejecutarConsulta("select LAST_INSERT_ID()");
                    String idFragmento = "" + ((ArrayList) res.get(0)).get(0);
                    for (int i = 0; i < idTuplas.size(); i++) {
                        String qTupla = "insert into tupla (id_remoto, id_fragmento) values('" + idTuplas.get(i) + "'," + idFragmento + ")";
                        acc.ejecutarComando(qTupla);
                    }
                    acc.desconectar();
                }
                this.progress1 = this.progress1 + t;
                if (this.progress1 > 100) {
                    this.progress1 = 100;
                }
            }
            if (this.progress1 < 100) {
                this.progress1 = 100;
            }
        } catch (Exception y) {
            y.printStackTrace();
        }

    }

    public void fragmentarHorCBIRPostgres(ArrayList<esquemaHorMultimedia> e) {
        try {
            this.guardarDatos();
            String qSitio = "";
            String idSitio = "";
            String[] u = this.getTabla().getAtributoMultimedia().split(":");
            this.getTabla().setAtributoLlave(this.llavePostgres(accexternopost, nombd, this.getTabla().getNombre()));
            Atributo attMultimedia = new Atributo();
            attMultimedia.setNombre(u[0]);
            attMultimedia.setTipo(u[1]);
            attMultimedia.setTamanio(u[2]);
            AccesoDatos acc = new AccesoDatos();
            if (acc.conectar()) {
                qSitio = "insert into sitio (direccion) values('" + this.getDirbd() + "')";
                acc.ejecutarComando(qSitio);
                ArrayList res = acc.ejecutarConsulta("select LAST_INSERT_ID()");
                idSitio = "" + ((ArrayList) res.get(0)).get(0);
            }
            String att_tipo = "";
            for (Atributo r : this.getTabla().getAtributos()) {
                if (this.transTipoDato(r.getTipo()) != 0) {
                    if (r.getNombre().compareTo(this.getTabla().getAtributoLlave().getNombre()) == 0) {
                        att_tipo += r.getNombre() + " " + r.getTipo() + " PRIMARY KEY, ";
                    } else {
                        att_tipo += r.getNombre() + " " + r.getTipo() + ", ";
                    }

                } else {
                    if (r.getTipo().compareTo("BIT") != 0) {
                        if (r.getNombre().compareTo(this.getTabla().getAtributoLlave().getNombre()) == 0) {
                            att_tipo += r.getNombre() + " " + r.getTipo() + "(" + r.getTamanio() + ") PRIMARY KEY, ";
                        } else {
                            att_tipo += r.getNombre() + " " + r.getTipo() + "(" + r.getTamanio() + "), ";
                        }
                    } else {
                        if (r.getNombre().compareTo(this.getTabla().getAtributoLlave().getNombre()) == 0) {
                            att_tipo += r.getNombre() + " TINYINT PRIMARY KEY, ";
                        } else {
                            att_tipo += r.getNombre() + " TINYINT, ";
                        }
                    }
                }
            }
            this.progress1 = 10;
            att_tipo = att_tipo.substring(0, att_tipo.length() - 2);
            int t = 100 / e.size();
            for (esquemaHorMultimedia tupla : e) {
                ArrayList<String> idTuplas = null;
                if (this.accexternopost.conectar()) {
                    ArrayList resultado = null;
                    String query = "create table " + tupla.getNombre() + " (" + att_tipo + ");";//, PRIMARY KEY (" + this.getTabla().getAtributoLlave().getNombre() + 
                    System.out.println(query);
                    this.accexternopost.ejecutarComando(query);
                    query = "select * from " + this.tabla.getNombre() + " where " + attMultimedia.getNombre() + "='" + tupla.getValor() + "'";
                    resultado = this.accexternopost.ejecutarConsulta(query);
                    if (resultado != null) {
                        if (!resultado.isEmpty()) {
                            idTuplas = new ArrayList<>();
                            for (int i = 0; i < resultado.size(); i++) {
                                String valores = "";
                                String id = "";
                                for (int j = 0; j < ((ArrayList) resultado.get(i)).size(); j++) {
                                    if (this.getTabla().getAtributos().get(j).getNombre().compareTo(this.getTabla().getAtributoLlave().getNombre()) == 0) {
                                        id = "" + ((ArrayList) resultado.get(i)).get(j);
                                    }
                                    if (this.transTipoDato(this.getTabla().getAtributos().get(j).getTipo()) == 0 || this.transTipoDato(this.getTabla().getAtributos().get(j).getTipo()) == 2) {
                                        valores += "'" + ((ArrayList) resultado.get(i)).get(j) + "', ";
                                    } else {
                                        valores += ((ArrayList) resultado.get(i)).get(j) + ", ";
                                    }
                                }
                                valores = valores.substring(0, valores.length() - 2);
                                String q = "INSERT INTO " + tupla.getNombre() + " VALUES (" + valores + ");";
                                this.accexternopost.ejecutarComando(q);
                                idTuplas.add(id);
                            }
                        }
                    }
                    this.accexternopost.desconectar();
                }
                if (acc.conectar()) {
                    String un = "0";
                    String dos = "0";
                    un = (tupla.getNOperacionesAprox().compareTo("0") == 0) ? "1" : tupla.getNOperacionesAprox();
                    dos = (tupla.getNdesempenio().compareTo("0") == 0) ? "1" : tupla.getNdesempenio();
                    System.out.println("undos:" + un + " - " + dos);
                    String q = "insert into fragmento (nombre, id_tabla, id_sitio, noperaciones, ndesempenio) values('" + tupla.getNombre() + "'," + this.tabla.getIdTabla() + "," + idSitio + "," + un + "," + dos + ")";
                    acc.ejecutarComando(q);
                    ArrayList res = acc.ejecutarConsulta("select LAST_INSERT_ID()");
                    String idFragmento = "" + ((ArrayList) res.get(0)).get(0);
                    for (int i = 0; i < idTuplas.size(); i++) {
                        String qTupla = "insert into tupla (id_remoto, id_fragmento) values('" + idTuplas.get(i) + "'," + idFragmento + ")";
                        acc.ejecutarComando(qTupla);
                    }
                    acc.desconectar();
                }
                this.progress1 = this.progress1 + t;
                if (this.progress1 > 100) {
                    this.progress1 = 100;
                }
            }
            if (this.progress1 < 100) {
                this.progress1 = 100;
            }
        } catch (Exception y) {
            y.printStackTrace();
        }

    }

    public void fragmentarHorCBIRMongoDB(ArrayList<esquemaHorMultimedia> e) {
        try {
            String qSitio = "";
            String idSitio = "";
            this.guardarDatos();
            String[] u = this.getTabla().getAtributoMultimedia().split(":");
            Atributo attMultimedia = new Atributo();
            attMultimedia.setNombre(u[0]);
            attMultimedia.setTipo(u[1]);
            AccesoDatos acc = new AccesoDatos();
            if (acc.conectar()) {
                qSitio = "insert into sitio (direccion) values('" + this.getDirbd() + "')";
                acc.ejecutarComando(qSitio);
                ArrayList res = acc.ejecutarConsulta("select LAST_INSERT_ID()");
                idSitio = "" + ((ArrayList) res.get(0)).get(0);
                acc.desconectar();
            }
            if (this.getAccexternomongo().conectar()) {
                this.progress1 = 10;
                int t = 100 / e.size();
                for (esquemaHorMultimedia tupla : e) {
                    ArrayList<String> idTuplas = null;
                    ArrayList<String> tamanioBytes = null;
                    MongoDatabase db = this.getAccexternomongo().getMongoClient().getDatabase(nombd);
                    MongoCollection<Document> doc = db.getCollection(this.getTabla().getNombre());
                    db.createCollection(tupla.getNombre());
                    MongoCollection<Document> nuevoDoc = db.getCollection(tupla.getNombre());
                    Document where = null;
                    if (attMultimedia.getTipo().compareTo("double") == 0 || attMultimedia.getTipo().compareTo("decimal") == 0 || attMultimedia.getTipo().compareTo("int") == 0 || attMultimedia.getTipo().compareTo("long") == 0) {
                        where = new Document(attMultimedia.getNombre(), Double.parseDouble(tupla.getValor()));
                    } else {

                        where = new Document(attMultimedia.getNombre(), tupla.getValor());
                    }

                    MongoCursor<Document> resultado = doc.find(where).iterator();

                    idTuplas = new ArrayList<>();
                    while (resultado.hasNext()) {
                        Document Nuevatupla = resultado.next();
                        idTuplas.add(Nuevatupla.get("_id").toString());
                        nuevoDoc.insertOne(Nuevatupla);
                    }
                    if (acc.conectar()) {
                        String un = "0";
                        String dos = "0";
                        un = (tupla.getNOperacionesAprox().compareTo("0") == 0) ? "1" : tupla.getNOperacionesAprox();
                        dos = (tupla.getNdesempenio().compareTo("0") == 0) ? "1" : tupla.getNdesempenio();
                        String q = "insert into fragmento (nombre, id_tabla, id_sitio, noperaciones, ndesempenio) values('" + tupla.getNombre() + "'," + this.tabla.getIdTabla() + "," + idSitio + "," + un + "," + dos + ")";
                        acc.ejecutarComando(q);
                        ArrayList res = acc.ejecutarConsulta("select LAST_INSERT_ID()");
                        String idFragmento = "" + ((ArrayList) res.get(0)).get(0);
                        for (int i = 0; i < idTuplas.size(); i++) {
                            String qTupla = "insert into tupla (id_remoto, id_fragmento) values('" + idTuplas.get(i) + "'," + idFragmento + ")";
                            acc.ejecutarComando(qTupla);
                        }
                        acc.desconectar();
                    }
                    this.progress1 = this.progress1 + t;
                    if (this.progress1 > 100) {
                        this.progress1 = 100;
                    }

                }
            }
            if (this.progress1 < 100) {
                this.progress1 = 100;
            }
        } catch (Exception y) {
            y.printStackTrace();
        }
    }

    public vOperacionDesempenio nOperacionesLog(UploadedFiles files, String conjunto) {
        vOperacionDesempenio ret = new vOperacionDesempenio();
        double op = 0;
        double des = 0;
        setNombreIp(new HashMap<>());
        for (UploadedFile f : files.getFiles()) {
            if (f != null && !"".equals(f.getFileName())) {
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(f.getInputStream(), "UTF-8"))) {
                    String actual = "";

                    if (this.TipoBD.compareTo("a") == 0) {
                        //generalLogFile activado
                        String ip = "127.0.0.1";
                        String nombre = "";
                        while ((actual = bufferedReader.readLine()) != null) {

                            //System.out.println(actual.contains("Connect\t")+" "+actual+" ");
                            if (actual.contains("Connect\t") && !actual.contains("denied")) {
                                String y[] = actual.split("\t");
                                ip = y[2].substring(y[2].indexOf("@") + 1, y[2].indexOf(" ", y[2].indexOf("@") + 1));
                                nombre = y[1].substring(0, y[1].indexOf(" "));
                                if (ip.toLowerCase().compareTo("localhost") == 0 || ip.toLowerCase().compareTo("::1") == 0) {
                                    ip = "127.0.0.1";
                                }
                                getNombreIp().put(nombre, ip);
                            }
                            int ValorAsignadoOperacion = 0;
                            if (actual.toLowerCase().contains("select") && actual.toLowerCase().contains(this.tabla.getNombre().toLowerCase())) {
                                ValorAsignadoOperacion = 1;
                            } else if ((actual.toLowerCase().contains("delete")
                                    || actual.toLowerCase().contains("insert")) && actual.toLowerCase().contains(this.tabla.getNombre().toLowerCase())) {
                                ValorAsignadoOperacion = 2;
                            } else if (actual.toLowerCase().contains("update") && actual.toLowerCase().contains(this.tabla.getNombre().toLowerCase())) {
                                ValorAsignadoOperacion = 3;
                            }
                            if (ValorAsignadoOperacion != 0) {
                                String y[] = actual.split("\t");
                                String sujeto = y[1].substring(0, y[1].indexOf(" "));
                                String ipTraduccion = getNombreIp().get(sujeto);
                                if (ipTraduccion.compareTo(this.dirbd) != 0) {
                                    double nTuplas = 1.0;
                                    if ((actual.toLowerCase().contains("select") || actual.toLowerCase().contains("delete")
                                            || actual.toLowerCase().contains("update")) && actual.toLowerCase().contains(this.tabla.getNombre().toLowerCase())) {//excluir joins
                                        String QueryNTuplas = "";
                                        boolean perteneceConjunto = false;
                                        if (y[2].toLowerCase().contains("where")) {
                                            String predicado = y[2].substring(y[2].toLowerCase().indexOf("where"));
                                            QueryNTuplas = "select count(*) from " + this.tabla.getNombre().toLowerCase() + " " + predicado;
                                            String nombreColumna = this.getTabla().getAtributoMultimedia().split(":")[0];
                                            ArrayList subConsulta = null;
                                            if (this.accexternomysql.conectar()) {
                                                subConsulta = this.accexternomysql.ejecutarConsulta("select count(*) from (select tipo from multimedia_records " + predicado + ") as t1 where " + nombreColumna + "='" + conjunto + "';");
                                                this.accexternomysql.desconectar();
                                            }
                                            if (!this.isNullorEmpty(subConsulta)) {
                                                perteneceConjunto = (Double.parseDouble("" + ((ArrayList) subConsulta.get(0)).get(0)) > 0) ? true : false;
                                            }
                                        } else {
                                            perteneceConjunto = true;
                                            QueryNTuplas = "select count(*) from " + this.tabla.getNombre().toLowerCase();
                                        }
                                        if (perteneceConjunto) {
                                            ArrayList rs = null;
                                            System.out.println(conjunto + " remoto " + (ValorAsignadoOperacion * 2) + ipTraduccion);
                                            if (this.accexternomysql.conectar()) {
                                                rs = this.accexternomysql.ejecutarConsulta(QueryNTuplas);
                                                this.accexternomysql.desconectar();
                                            }
                                            if (!this.isNullorEmpty(rs)) {
                                                nTuplas = Double.parseDouble("" + ((ArrayList) rs.get(0)).get(0));
                                            }
                                            des = des + (ValorAsignadoOperacion * 2) * nTuplas;
                                            op++;
                                        }

                                    } else if (actual.toLowerCase().contains("insert") && actual.toLowerCase().contains(this.tabla.getNombre().toLowerCase()) && actual.toLowerCase().contains(conjunto)) {
                                        //si es insert nTuplas se queda con 1

                                        des = des + (ValorAsignadoOperacion * 2) * nTuplas;
                                        op++;
                                    }
                                } else {
                                    if ((actual.toLowerCase().contains("select") || actual.toLowerCase().contains("delete") || actual.toLowerCase().contains("update") || actual.toLowerCase().contains("insert"))
                                            && actual.toLowerCase().contains(this.tabla.getNombre().toLowerCase()) && actual.toLowerCase().contains(conjunto)) {
                                        des = des + ValorAsignadoOperacion;
                                        op++;
                                        System.out.println(conjunto + " local " + ValorAsignadoOperacion + ipTraduccion);
                                    }
                                }

                            }
                        }
                    } else if (this.TipoBD.compareTo("b") == 0 || this.TipoBD.compareTo("c") == 0) {
                        //log_line_prefix = '[%p]-_-%h'
                        //log_connections = on
                        //log_disconnections = on
                        //logging_collector = on
                        //log_statement = 'all'
                        //%h debe ser IP!
                        //conexión: 
                        //[2168]-_-::1LOG:  conexión autorizada: usuario=postgres base de datos=Prueba application_name=psql
                        //Desconexión:
                        //[2168]-_-::1LOG:  desconexiÃ³n: duraciÃ³n de sesiÃ³n: 0:01:31.066 usuario=postgres base=Prueba host=::1 port=51996
                        //127.0.0.1 en IPv4 y ::1 en IPv6
                        String ipLinea = "";
                        String nombre = "";
                        while ((actual = bufferedReader.readLine()) != null) {
                            System.out.println(actual);
                            if (actual.toLowerCase().contains("conexion autorizada") || actual.toLowerCase().contains("connection authorized")) {
                                ipLinea = actual.toLowerCase().substring(actual.toLowerCase().indexOf("-_-") + 3, actual.toLowerCase().indexOf("log"));
                                System.out.println(ipLinea);
                                nombre = actual.substring(actual.indexOf("[") + 1, actual.indexOf("]"));

                                if (ipLinea.toLowerCase().compareTo("localhost") == 0 || ipLinea.toLowerCase().compareTo("::1") == 0) {
                                    ipLinea = "127.0.0.1";
                                }
                                getNombreIp().put(nombre, ipLinea);
                            }
                            int ValorAsignadoOperacion = 0;
                            if (actual.toLowerCase().contains("select") && actual.toLowerCase().contains(this.tabla.getNombre().toLowerCase())) {
                                ValorAsignadoOperacion = 1;
                            } else if ((actual.toLowerCase().contains("delete")
                                    || actual.toLowerCase().contains("insert")) && actual.toLowerCase().contains(this.tabla.getNombre().toLowerCase())) {
                                ValorAsignadoOperacion = 2;
                            } else if (actual.toLowerCase().contains("update") && actual.toLowerCase().contains(this.tabla.getNombre().toLowerCase())) {
                                ValorAsignadoOperacion = 3;
                            }
                            if (ValorAsignadoOperacion != 0) {
                                String sujeto = actual.substring(actual.indexOf("[") + 1, actual.indexOf("]"));
                                String ipTraduccion = getNombreIp().get(sujeto);
                                if (ipTraduccion.compareTo(this.dirbd) == 0) {
                                    if ((actual.toLowerCase().contains("select") || actual.toLowerCase().contains("delete") || actual.toLowerCase().contains("update") || actual.toLowerCase().contains("insert"))
                                            && actual.toLowerCase().contains(this.tabla.getNombre().toLowerCase()) && actual.toLowerCase().contains(conjunto)) {
                                        des = des + ValorAsignadoOperacion;
                                        op++;
                                    }
                                } else {
                                    double nTuplas = 1.0;
                                    if ((actual.toLowerCase().contains("select") || actual.toLowerCase().contains("delete") || actual.toLowerCase().contains("update"))
                                            && actual.toLowerCase().contains(this.tabla.getNombre().toLowerCase()) && actual.toLowerCase().contains(conjunto)) {//excluir joins
                                        String QueryNTuplas = "";
                                        if (actual.toLowerCase().contains("where")) {
                                            String predicado = actual.substring(actual.toLowerCase().indexOf("where"));
                                            QueryNTuplas = "select count(*) from " + this.tabla.getNombre().toLowerCase() + " " + predicado;
                                        } else {
                                            QueryNTuplas = "select count(*) from " + this.tabla.getNombre().toLowerCase();
                                        }
                                        ArrayList rs = null;
                                        System.out.println(QueryNTuplas);
                                        if (this.accexternopost.conectar()) {
                                            rs = this.accexternopost.ejecutarConsulta(QueryNTuplas);
                                            this.accexternopost.desconectar();
                                        }
                                        if (!this.isNullorEmpty(rs)) {
                                            nTuplas = Double.parseDouble("" + ((ArrayList) rs.get(0)).get(0));
                                        }
                                        des = des + (ValorAsignadoOperacion * 2) * nTuplas;
                                        op++;
                                    } else if (actual.toLowerCase().contains("insert") && actual.toLowerCase().contains(this.tabla.getNombre().toLowerCase()) && actual.toLowerCase().contains(conjunto)) {
                                        //si es insert nTuplas se queda con 1
                                        des = des + (ValorAsignadoOperacion * 2) * nTuplas;
                                        op++;
                                    }
                                }
                            }
                        }
                    } else if (this.TipoBD.compareTo("d") == 0) {
                        //setProfiling=1 o superior
                        while ((actual = bufferedReader.readLine()) != null) {
                            if (actual.toLowerCase().contains("command") && actual.toLowerCase().contains(this.tabla.getNombre().toLowerCase())) {
                                String ipLinea = "";
                                String nombre = "";
                                try {
                                    JSONObject j = new JSONObject(actual);
                                    String c = j.get("c").toString().toLowerCase().trim();
                                    JSONObject j2 = new JSONObject(j.get("attr").toString());
                                    JSONObject j3 = new JSONObject(j2.get("command").toString());
                                    String ns = "";
                                    String filter = "";
                                    if (j3.has("find")) {
                                        filter = j3.get("filter").toString().toLowerCase();
                                        ns = j3.get("find").toString().toLowerCase();
                                    } else if (j3.has("delete")) {
                                        JSONObject j4 = new JSONObject(j3.getJSONArray("deletes").get(0).toString());
                                        filter = j4.get("q").toString();
                                        ns = j3.get("delete").toString().toLowerCase();
                                    } else if (j3.has("insert")) {
                                        filter = j3.getJSONArray("documents").get(0).toString();
                                        ns = j3.get("insert").toString().toLowerCase();
                                    } else if (j3.has("update")) {
                                        JSONObject j4 = new JSONObject(j3.getJSONArray("updates").get(0).toString());
                                        filter = j4.get("q").toString();
                                        ns = j3.get("update").toString().toLowerCase();
                                    }

                                    //posprocesar "$oid" a "ObjectId"
                                    JSONObject f1 = new JSONObject(filter);
                                    if (f1.has("_id")) {
                                        try {
                                            JSONObject f2 = new JSONObject(f1.get("_id").toString());
                                            if (f2.has("$oid")) {
                                                String oid = f2.getString("$oid");
                                                String head = filter.substring(0, filter.indexOf("{\"$oid\""));
                                                String tail = filter.substring(filter.indexOf(oid) + oid.length() + 2);
                                                filter = head + "ObjectId(\"" + oid + "\")" + tail;
                                            }
                                        } catch (Exception e) {
                                            //e.printStackTrace();
                                            //no hacer nada, no tiene oid, solo valor
                                        }
                                    }

                                    if (c.compareTo("command") == 0 && ns.compareTo(this.tabla.getNombre().toLowerCase()) == 0) {
                                        boolean perteneceConjunto = false;

                                        if (filter.toLowerCase().compareTo("{}") == 0) {
                                            perteneceConjunto = true;
                                        } else {
                                            if (this.accexternomongo.conectar()) {
                                                MongoDatabase db = this.getAccexternomongo().getMongoClient().getDatabase(nombd);
                                                MongoCollection<Document> doc = db.getCollection(this.getTabla().getNombre());
                                                String nombreColumna = this.getTabla().getAtributoMultimedia().split(":")[0];
                                                Document where = Document.parse(filter);
                                                long coincidencias = doc.countDocuments(and((where), eq(nombreColumna, conjunto)));

                                                perteneceConjunto = (coincidencias > 0) ? true : false;
                                            }
                                        }
                                        if (perteneceConjunto) {
                                            op++;
                                            try {
                                                int ValorAsignadoOperacion = 0;
                                                if (j3.has("find")) {
                                                    ValorAsignadoOperacion = 1;
                                                } else if (j3.has("insert") || j3.has("delete")) {
                                                    ValorAsignadoOperacion = 2;
                                                } else if (j3.has("update")) {
                                                    ValorAsignadoOperacion = 3;
                                                }

                                                if (ValorAsignadoOperacion != 0) {
                                                    j2 = new JSONObject(j.get("attr").toString());
                                                    String remote = j2.get("remote").toString().toLowerCase().trim();
                                                    ipLinea = remote.substring(0, remote.indexOf(":"));
                                                    if (!this.isNullorEmpty(ns)) {
                                                        getNombreIp().put(ipLinea, ipLinea);
                                                        if (this.dirbd.compareTo(ipLinea) == 0) {
                                                            System.out.println(ValorAsignadoOperacion + " local " + conjunto);
                                                            des = des + ValorAsignadoOperacion;
                                                        } else {
                                                            long ntuplas = 1;
                                                            if (j3.has("find")) {
                                                                ntuplas = Long.parseLong(j2.get("nreturned").toString());
                                                            } else if (j3.has("delete") || j3.has("update")) {
                                                                if (this.accexternomongo.conectar()) {
                                                                    MongoDatabase db = this.getAccexternomongo().getMongoClient().getDatabase(nombd);
                                                                    MongoCollection<Document> doc = db.getCollection(this.getTabla().getNombre());
                                                                    Document where = Document.parse(filter);
                                                                    ntuplas = doc.countDocuments(where);
                                                                }
                                                            } else if (j3.has("insert")) {
                                                                ntuplas = Long.parseLong(j2.get("ninserted").toString());
                                                            }
                                                            System.out.println(ValorAsignadoOperacion + " " + ntuplas + " remoto " + conjunto);
                                                            des = des + (ValorAsignadoOperacion * 2) * ntuplas;
                                                        }
                                                    } else {
                                                        System.out.println("ns esta vacio");
                                                    }
                                                } else {
                                                    System.out.println("valor asignado de operación es 0");
                                                }

                                            } catch (JSONException ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                    }

                                } catch (JSONException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("op:" + op + " des: " + des);
        ret.setOperaciones(op);
        ret.setDesempenio(des);
        return ret;
    }

    protected String getIP() {
        String ipLocal = "";
        try {
            ipLocal = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
        return ipLocal;
    }

    public double nTuplasTabla() {
        double res = 0;
        ArrayList arr = null;
        try {
            if (this.TipoBD.compareTo("a") == 0) {
                if (this.accexternomysql.conectar()) {
                    arr = this.accexternomysql.ejecutarConsulta("select count(*) from " + this.tabla.getNombre());
                    this.accexternomysql.desconectar();
                }
                if (arr != null) {
                    res = (double) ((ArrayList) arr.get(0)).get(0);
                }
            } else if (this.TipoBD.compareTo("b") == 0 || this.TipoBD.compareTo("c") == 0) {
                if (this.accexternopost.conectar()) {
                    arr = this.accexternopost.ejecutarConsulta("select count(*) from " + this.tabla.getNombre());
                    this.accexternopost.desconectar();
                }
                if (arr != null) {
                    res = (double) ((ArrayList) arr.get(0)).get(0);
                }
            } else if (this.TipoBD.compareTo("d") == 0) {
                if (this.getAccexternomongo().conectar()) {
                    MongoDatabase db = this.getAccexternomongo().getMongoClient().getDatabase(nombd);
                    MongoCollection<Document> colec = db.getCollection(this.tabla.getNombre());
                    res = (double) colec.countDocuments();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public int transTipoDato(String tipo) {
        tipo = tipo.toUpperCase();
        switch (tipo) {
            case "CHAR":
            case "VARCHAR":
            case "BIT":
            case "TINYINT":
                return 0;
            case "INTEGER":
            case "SMALLINT":
            case "BIGINT":
            case "NUMERIC":
            case "DECIMAL":
            case "FLOAT":
            case "DOUBLE":
                return 1;
            case "DATE":
            case "TIME":
            case "TIMESTAMP":
            case "DATETIME":
                return 2;
            default:
                return 3;
        }

    }

    /**
     * @return the progress1
     */
    public Integer getProgress1() {
        return progress1;
    }

    /**
     * @param progress1 the progress1 to set
     */
    public void setProgress1(Integer progress1) {
        this.progress1 = progress1;
    }

    protected Atributo llaveMySQL(AccesoDatos acc, String nombreBase, String nombreTabla) {

        Atributo att = null;
        ArrayList res = null;
        try {
            if (acc.conectar()) {
                res = acc.ejecutarConsulta("SELECT COLUMN_NAME, DATA_TYPE, COLUMN_KEY FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + nombreBase + "' AND TABLE_NAME = '" + nombreTabla + "' and COLUMN_KEY IN('PRI', 'UNI')");
                acc.desconectar();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (res != null) {
            att = new Atributo();
            att.setNombre("" + ((ArrayList) res.get(0)).get(0));
            att.setTipo("" + ((ArrayList) res.get(0)).get(1));
        }
        return att;
    }

    public Atributo llavePostgres(AccesoDatosPG acc, String nombreBase, String nombreTabla) {

        Atributo att = null;
        ArrayList res = null;
        try {
            if (acc.conectar()) {
                String q = "select nombre, data_type "
                        + "from (SELECT column_name as nombre "
                        + "FROM information_schema.key_column_usage "
                        + "WHERE TABLE_NAME='" + nombreTabla + "' "
                        + "AND TABLE_CATALOG='" + nombreBase + "') as a "
                        + "join INFORMATION_SCHEMA.COLUMNS "
                        + "on INFORMATION_SCHEMA.COLUMNS.COLUMN_NAME=nombre "
                        + "where INFORMATION_SCHEMA.COLUMNS.TABLE_NAME='" + nombreTabla + "' "
                        + "and INFORMATION_SCHEMA.COLUMNS.TABLE_CATALOG='" + nombreBase + "';";
                res = acc.ejecutarConsulta(q);
                acc.desconectar();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (res != null) {
            att = new Atributo();
            att.setNombre("" + ((ArrayList) res.get(0)).get(0));
            att.setTipo("" + ((ArrayList) res.get(0)).get(1));
        }
        return att;
    }

    public void onTablas() {
        try {
            if (conexionexterna()) {
                if (TipoBD.compareTo("a") == 0) {
                    ArrayList<String> t = null;
                    if (this.accexternomysql.conectar()) {
                        t = this.accexternomysql.tablas(this.nombd);
                    }
                    if (!this.isNullorEmpty(t)) {
                        this.tablas = new ArrayList<>();
                        for (String item : t) {
                            SelectItem si = new SelectItem(item, item);
                            this.tablas.add(si);
                        }
                    }
                } else if (TipoBD.compareTo("b") == 0 || TipoBD.compareTo("c") == 0) {
                    ArrayList<String> t = null;
                    if (this.accexternopost.conectar()) {
                        t = this.accexternopost.tablas(this.nombd);
                    }
                    if (!this.isNullorEmpty(t)) {
                        this.tablas = new ArrayList<>();
                        for (String item : t) {
                            SelectItem si = new SelectItem(item, item);
                            this.tablas.add(si);
                        }
                    }
                } else {
                    ArrayList<String> t = null;
                    if (this.getAccexternomongo().conectar()) {
                        t = this.getAccexternomongo().tablas(this.nombd);
                    }
                    if (!this.isNullorEmpty(t)) {
                        this.tablas = new ArrayList<>();
                        for (String item : t) {
                            SelectItem si = new SelectItem(item, item);
                            this.tablas.add(si);
                        }
                    }
                }
            } else {
                System.out.println("Sin conexión");
                this.tablas = new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println("Sin conexión");
            this.tablas = new ArrayList<>();
        }
    }

    /**
     * @return the tablas
     */
    public List<SelectItem> getTablas() {
        return tablas;
    }

    /**
     * @param tablas the tablas to set
     */
    public void setTablas(List<SelectItem> tablas) {
        this.tablas = tablas;
    }

    public boolean isNullorEmpty(ArrayList arr) {
        boolean res = false;
        try {
            if (arr == null || arr.isEmpty()) {
                res = true;
            } else {
                res = false;
            }
        } catch (Exception e) {
            res = true;
        }
        return res;
    }

    public boolean isNullorEmpty(String s) {
        return (s != null) ? ((!s.isEmpty()) ? false : true) : true;
    }

    /**
     * @return the portbd
     */
    public String getPortbd() {
        return portbd;
    }

    /**
     * @param portbd the portbd to set
     */
    public void setPortbd(String portbd) {
        this.portbd = portbd;
    }

    /**
     * @return the accexternomongo
     */
    public AccesoDatosMG getAccexternomongo() {
        return accexternomongo;
    }

    /**
     * @param accexternomongo the accexternomongo to set
     */
    public void setAccexternomongo(AccesoDatosMG accexternomongo) {
        this.accexternomongo = accexternomongo;
    }

    /**
     * @return the NombreIp
     */
    public HashMap<String, String> getNombreIp() {
        return NombreIp;
    }

    /**
     * @param NombreIp the NombreIp to set
     */
    public void setNombreIp(HashMap<String, String> NombreIp) {
        this.NombreIp = NombreIp;
    }

    public ArrayList<TablaCosto> esquemaVertical(UploadedFiles files) throws IOException {
        ArrayList<TablaCosto> ta = new ArrayList();

        archivos a = new archivos(this.passbd, this.nombd, this.tabla.getNombre(), this.TipoBD, files, this.dirbd);
        //ta.add(a.noLog(a.obtenerTablaCosto()));
        ta.addAll(a.obtenerTablaCosto());

        ta = a.nombrar(a.obtenerTablaCosto());

        return ta;

    }

    public ArrayList<TablaCosto> esquemaVerticalCBIRPostgres(ArrayList<String> attM, UploadedFiles files) throws IOException {
        archivos a = new archivos(this.passbd, this.nombd, this.tabla.getNombre(), this.TipoBD, files, this.dirbd, attM);
        ArrayList<TablaCosto> juntos = new ArrayList();
        juntos.addAll(a.obtenerFragMul());
        juntos.addAll(a.obtenerTablaCosto());
        ArrayList<TablaCosto> todos = new ArrayList();

        todos.addAll(juntos);
        if (a.noLog(juntos) != null) {
            todos.add(a.noLog(juntos));
        }

        return a.nombrar(a.obtenerTablaCosto());
    }

    public ArrayList<Query> ConsultasVerLog(UploadedFiles files) throws IOException {
        archivos a = new archivos(this.passbd, this.nombd, this.tabla.getNombre(), this.TipoBD, files, this.dirbd);

        return a.arrQuerys(a.getArrCad());
    }

    public ArrayList<TablaCosto> AsignacionSitio(UploadedFiles files) throws IOException {
        archivos a = new archivos(this.passbd, this.nombd, this.tabla.getNombre(), this.TipoBD, files, this.dirbd);
        ArrayList<TablaCosto> ta = new ArrayList<>();
        ArrayList<TablaCosto> to = new ArrayList<>();
        ta.addAll(a.mismoSitio(a.obtenerTablaCosto()));
        if (a.noLog(a.obtenerTablaCosto()) != null) {
            ta.add(a.noLog(a.obtenerTablaCosto()));
        }

        return a.nombrar(ta);
    }

    public ArrayList<TablaCosto> AsignacionSitio1(ArrayList<String> attsM, UploadedFiles files) throws IOException {
        archivos a = new archivos(this.passbd, this.nombd, this.tabla.getNombre(), this.TipoBD, files, this.dirbd, attsM);
        ArrayList<TablaCosto> ta = new ArrayList<>();
        ArrayList<TablaCosto> to = new ArrayList<>();
        ta.addAll(a.obtenerFragMul());
        ta.addAll(a.mismoSitio(a.obtenerTablaCosto()));
        to.addAll(ta);
        if (a.noLog(ta) != null) {
            to.add(a.noLog(ta));
        }

        return a.nombrar(to);
    }

    public boolean isVerticalCBIR() {
        return this.tabla.getTipoFragment().compareTo("b") == 0 && this.tabla.isCBIR();
    }

    public void fragmentarVerCBIRMySQL(ArrayList<String> esquemaVerticalCBIR) {

    }

    public void fragmentarVerCBIRPostgres(ArrayList<TablaCosto> es) {
        this.progress1 = 1;
        int percent = 100 / es.size();

        try {
            for (TablaCosto ta : es) {
                crearTabla(ta);
                this.progress1 += percent;
            }
            this.progress1 = 100;
        } catch (Exception e) {
            System.out.println("Sin conexión");
        }
    }

    private void crearTabla(TablaCosto frag) {

        try {
            String sql;

            AccesoDatosPG acc = new AccesoDatosPG(frag.getSitio().getIp() + ":" + "5432", this.nombd, this.usubd, this.passbd);
            if (acc.conectar()) {

                sql = "drop table if exists " + frag.getNombre() + " ; CREATE TABLE " + frag.getNombre() + " ( " + atributosTipos(frag.getArrAtri()) + " );";

                acc.ejecutarComando(sql);
                acc.desconectar();
            }

            ArrayList atributos = null;
            ArrayList n = null;
            AccesoDatosPG acc2 = new AccesoDatosPG(this.dirbd + ":" + "5432", this.nombd, this.usubd, this.passbd);
            if (acc2.conectar()) {
                String numero = "Select count (*) from " + this.getTabla().getNombre() + ";";
                n = acc2.ejecutarConsulta(numero);
                acc2.desconectar();
            }
            int num = ((Double) Double.parseDouble("" + ((ArrayList) n.get(0)).get(0))).intValue();
            int nLote = num / 50;
            System.out.println("Lotes*********************" + frag.getNombre());
            for (int i = 0; i < 50; i++) {
                acc2 = new AccesoDatosPG(this.dirbd + ":" + "5432", this.nombd, this.usubd, this.passbd);
                if (acc2.conectar()) {
                    if (i == 0) {
                        sql = "Select " + atri(frag.getArrAtri()) + " from " + this.getTabla().getNombre() + " Limit " + nLote + " offset " + (i + 1);
                    } else {

                        sql = "Select " + atri(frag.getArrAtri()) + " from " + this.getTabla().getNombre() + " Limit " + nLote + " offset " + nLote * (i + 1);
                    }
                    atributos = acc2.ejecutarConsulta(sql);
                    acc2.desconectar();
                }
                if (atributos != null) {
                    acc = new AccesoDatosPG(frag.getSitio().getIp() + ":" + "5432", this.nombd, this.usubd, this.passbd);
                    if (acc.conectar()) {
                        for (int k = 0; k < atributos.size(); k++) {
                            String valor = "";
                            ArrayList tupla = (ArrayList) atributos.get(k);
                            for (int j = 0; j < tupla.size(); j++) {
                                valor += "'" + tupla.get(j) + "',";
                            }
                            valor = valor.substring(0, valor.length() - 1);
                            sql = "insert into " + frag.getNombre() + "(" + frag.getAtributos() + ") values (" + valor + ");";

                            acc.ejecutarComando(sql);

                        }
                        acc.desconectar();
                    }

                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String atri(ArrayList<atributo> arrAtri) {
        String separador = ", ";
        @SuppressWarnings("unchecked")
        ArrayList<String> cad = new ArrayList();
        arrAtri.forEach((att) -> {
            cad.add(att.getNombre());
        });
        return cad.stream().map(String::toLowerCase).collect(Collectors.joining(separador));
    }

    private String atributosTipos(ArrayList<atributo> arrAtri) {

        @SuppressWarnings("unchecked")
        ArrayList<String> cad = new ArrayList();
        String separador = ", ";
        arrAtri.forEach((att) -> {
            cad.add(att.getNombre() + " " + BuildTypes.determineTypes(att.getTipo(), att.getTamanio()));
        });
        return cad.stream().map(String::toLowerCase).collect(Collectors.joining(separador));
    }

    public void fragmentarVerCBIRMongoDB(ArrayList<String> esquemaVerticalCBIR) {

    }

    public double nTuplasOperacionRelacional(String operacion) {

        return 0.0;
    }

    public double nTuplasRegistroNoSQL(String registro) {

        return 0.0;
    }

    public AccesoDatos getAccexternomysql() {
        return accexternomysql;
    }

    public AccesoDatosPG getAccexternopost() {
        return accexternopost;
    }
}
