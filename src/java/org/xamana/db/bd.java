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
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Arrays;
import org.json.JSONObject;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;

/**
 *
 * @author pipe
 */
public class bd implements Serializable {

    AccesoDatos accexternomysql;
    AccesoDatosMG accexternomongo;
    AccesoDatosPG accexternopost;
    private String dirbd;
    private String nombd;
    private String usubd;
    private String passbd;
    private String TipoBD = "";
    private Tabla tabla = new Tabla();
    private Integer progress1;

    public boolean conexionexterna() {
        boolean res = false;

        if (TipoBD.compareTo("a") == 0) {
            try {
                accexternomysql = new AccesoDatos(dirbd, nombd, usubd, passbd);

                if (accexternomysql.conectar()) {
                    res = true;
                    accexternomysql.desconectar();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (TipoBD.compareTo("b") == 0 || TipoBD.compareTo("c") == 0) {
            try {
                accexternopost = new AccesoDatosPG(dirbd, nombd, usubd, passbd);
                if (accexternopost.conectar()) {
                    res = true;
                    accexternopost.desconectar();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (TipoBD.compareTo("d") == 0) {
            try {
                accexternomongo = new AccesoDatosMG(dirbd, nombd, this.tabla.getNombre(), usubd, passbd);
                if (accexternomongo.conectar()) {
                    res = true;
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
        if (this.accexternomongo.conectar()) {
            arr = this.accexternomongo.atributos(nombd, this.getTabla().getNombre());
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
        double nOperacionesLog = this.nOperacionesLog(files);
        double nTuplas = this.nTuplas();
        double nOperacionesTabla = nOperacionesLog / nTuplas;
        if (this.TipoBD.compareTo("a") == 0) {
            if (this.accexternomysql.conectar()) {
                arr = this.accexternomysql.ejecutarConsulta(queryRelacional);
                this.accexternomysql.desconectar();
            }
            if (arr != null) {
                res = new ArrayList<>();
                for (int t = 0; t < arr.size(); t++) {
                    esquemaHorMultimedia tupla = new esquemaHorMultimedia();
                    tupla.setNombre(this.tabla.getNombre() + "_f" + (t + 1));
                    tupla.setValor("" + ((ArrayList) arr.get(t)).get(0));
                    tupla.setNTuplas("" + ((ArrayList) arr.get(t)).get(1));
                    tupla.setPorcentaje("" + (((Double) ((Double.parseDouble(tupla.getNTuplas()) * 100) / nTuplas)).intValue()));
                    tupla.setNOperacionesAprox("" + (((Double) ((Double.parseDouble(tupla.getPorcentaje()) * nOperacionesTabla) / 100)).intValue()));
                    res.add(tupla);
                }
            }
        } else if (this.TipoBD.compareTo("b") == 0 || this.TipoBD.compareTo("c") == 0) {
            if (this.accexternopost.conectar()) {
                arr = this.accexternopost.ejecutarConsulta(queryRelacional);
                this.accexternopost.desconectar();
            }
            if (arr != null) {
                res = new ArrayList<>();
                for (int t = 0; t < arr.size(); t++) {
                    esquemaHorMultimedia tupla = new esquemaHorMultimedia();
                    tupla.setNombre(this.tabla.getNombre() + "_f" + (t + 1));
                    tupla.setValor("" + ((ArrayList) arr.get(t)).get(0));
                    tupla.setNTuplas("" + ((ArrayList) arr.get(t)).get(1));
                    tupla.setPorcentaje("" + (((Double) ((Double.parseDouble(tupla.getNTuplas()) * 100) / nTuplas)).intValue()));
                    tupla.setNOperacionesAprox("" + (((Double) ((Double.parseDouble(tupla.getPorcentaje()) * nOperacionesTabla) / 100)).intValue()));
                    res.add(tupla);
                }
            }
        } else {
            if (this.accexternomongo.conectar()) {
                MongoDatabase db = this.accexternomongo.getMongoClient().getDatabase(nombd);
                MongoCollection<Document> colec = db.getCollection(this.tabla.getNombre());
                Bson group = group("$" + nombreColumna, sum("num", 1L));
                List<Document> r = colec.aggregate(Arrays.asList(group)).into(new ArrayList<>());
                if (!r.isEmpty()) {
                    res = new ArrayList<>();
                    int t = 0;
                    for (Document d : r) {
                        t++;
                        esquemaHorMultimedia tupla = new esquemaHorMultimedia();
                        tupla.setNombre(this.tabla.getNombre() + "_f" + (t));
                        tupla.setValor("" + (new JSONObject(d.toJson())).get("_id"));
                        tupla.setNTuplas("" + (new JSONObject(d.toJson())).get("num"));
                        tupla.setPorcentaje("" + (((Double) ((Double.parseDouble(tupla.getNTuplas()) * 100) / nTuplas)).intValue()));
                        tupla.setNOperacionesAprox("" + (((Double) ((Double.parseDouble(tupla.getPorcentaje()) * nOperacionesTabla) / 100)).intValue()));
                        res.add(tupla);
                    }
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

    public String guardarDatos() {
        String id_bd = "";
        String id_tabla = "";
        String query = "";
        try {
            AccesoDatos acc = new AccesoDatos();
            if (acc.conectar()) {

                if (this.usubd.isEmpty()) {
                    query = "insert into BD (direccion, gestor, usr, psw) values('" + this.dirbd + "','" + this.TipoBD + "',null,null)";
                } else if (!this.usubd.isEmpty() && this.passbd.isEmpty()) {
                    query = "insert into BD (direccion, gestor, usr, psw) values('" + this.dirbd + "','" + this.TipoBD + "','" + this.usubd + "',null)";
                } else {
                    query = "insert into BD (direccion, gestor, usr, psw) values('" + this.dirbd + "','" + this.TipoBD + "','" + this.usubd + "','" + this.passbd + "')";
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
        return id_tabla;
    }

    public void fragmentarHorCBIRMySQL(ArrayList<esquemaHorMultimedia> e) {
        try {
            String idTabla=this.guardarDatos();
            String[] u = this.getTabla().getAtributoMultimedia().split(":");
            Atributo attMultimedia = new Atributo();
            attMultimedia.setNombre(u[0]);
            attMultimedia.setTipo(u[1]);
            attMultimedia.setTamanio(u[2]);
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
            for (esquemaHorMultimedia tupla : e) {
                accexternomysql = new AccesoDatos(dirbd, nombd, usubd, passbd);
                ArrayList<String> idTuplas=null;
                if (this.accexternomysql.conectar()) {
                    ArrayList resultado = null;
                    String query = "create table " + tupla.getNombre() + " (" + att_tipo + ");";
                    this.accexternomysql.ejecutarComando(query);
                    query = "select * from " + this.tabla.getNombre() + " where " + attMultimedia.getNombre() + "='" + tupla.getValor() + "'";
                    resultado = this.accexternomysql.ejecutarConsulta(query);
                    if (resultado != null) {
                        if (!resultado.isEmpty()) {
                            idTuplas=new ArrayList<>();
                            for (int i = 0; i < resultado.size(); i++) {
                                String valores = "";
                                for (int j = 0; j < ((ArrayList) resultado.get(i)).size(); j++) {
                                    if (this.transTipoDato(this.getTabla().getAtributos().get(j).getTipo()) == 0 || this.transTipoDato(this.getTabla().getAtributos().get(j).getTipo()) == 2) {
                                        valores += "\"" + ((ArrayList) resultado.get(i)).get(j) + "\", ";
                                    } else {
                                        valores += ((ArrayList) resultado.get(i)).get(j) + ", ";
                                    }
                                }
                                valores = valores.substring(0, valores.length() - 2);
                                String q = "INSERT INTO " + tupla.getNombre() + " VALUES (" + valores + ");";
                                this.accexternomysql.ejecutarComando(q);
                                ArrayList res = this.accexternomysql.ejecutarConsulta("select LAST_INSERT_ID()");
                                idTuplas.add(""+((ArrayList)res.get(0)).get(0));
                            }
                        }
                    }
                    this.accexternomysql.desconectar();
                }
                AccesoDatos acc = new AccesoDatos();
                if (acc.conectar()) {
                    String qSitio="insert into sitio (direccion) values('"+this.getDirbd()+"')";
                    acc.ejecutarComando(qSitio);
                    ArrayList res = acc.ejecutarConsulta("select LAST_INSERT_ID()");
                    String idSitio=""+((ArrayList)res.get(0)).get(0);
                    String q="insert into fragmento (nombre, id_tabla, id_sitio) values('"+tupla.getNombre()+"',"+idTabla+","+idSitio+")";
                    acc.ejecutarComando(q);
                    res = acc.ejecutarConsulta("select LAST_INSERT_ID()");
                    String idFragmento=""+((ArrayList)res.get(0)).get(0);
                    for(String id:idTuplas){
                        String qTupla="insert into tupla (id_remoto, freq_acceso, tamanioBytes, id_fragmento) values('"+id+"',0,0,"+idFragmento+")";
                        acc.ejecutarComando(qTupla);
                    }
                    acc.desconectar();
                }
            }
        } catch (Exception y) {
            y.printStackTrace();
        }
    }

    public void fragmentarHorCBIRPostgres(ArrayList<esquemaHorMultimedia> e) {
        try {
            if (this.accexternopost.conectar()) {
                String[] u = this.getTabla().getAtributoMultimedia().split(":");
                Atributo attMultimedia = new Atributo();
                attMultimedia.setNombre(u[0]);
                attMultimedia.setTipo(u[1]);
                attMultimedia.setTamanio(u[2]);
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
                for (esquemaHorMultimedia tupla : e) {
                    ArrayList resultado = null;
                    String query = "create table " + tupla.getNombre() + " (" + att_tipo + ");";
                    this.accexternopost.ejecutarComando(query);
                    query = "select * from " + this.tabla.getNombre() + " where " + attMultimedia.getNombre() + "='" + tupla.getValor() + "'";
                    resultado = this.accexternopost.ejecutarConsulta(query);
                    if (resultado != null) {
                        if (!resultado.isEmpty()) {
                            for (int i = 0; i < resultado.size(); i++) {
                                String valores = "";
                                for (int j = 0; j < ((ArrayList) resultado.get(i)).size(); j++) {

                                    if (this.transTipoDato(this.getTabla().getAtributos().get(j).getTipo()) == 0 || this.transTipoDato(this.getTabla().getAtributos().get(j).getTipo()) == 2) {
                                        valores += "'" + ((ArrayList) resultado.get(i)).get(j) + "', ";
                                        System.out.println("pasa");
                                    } else {
                                        valores += ((ArrayList) resultado.get(i)).get(j) + ", ";
                                    }
                                }
                                valores = valores.substring(0, valores.length() - 2);

                                String q = "INSERT INTO " + tupla.getNombre() + " VALUES (" + valores + ");";
                                this.accexternopost.ejecutarComando(q);
                            }
                        }
                    }
                }
                this.accexternopost.desconectar();
            }
        } catch (Exception y) {
            y.printStackTrace();
        }
    }

    public void fragmentarHorCBIRMongoDB(ArrayList<esquemaHorMultimedia> e) {
        try {
            if (this.accexternomongo.conectar()) {
                this.progress1 = 10;
                for (esquemaHorMultimedia tupla : e) {
                    String[] u = this.getTabla().getAtributoMultimedia().split(":");
                    Atributo attMultimedia = new Atributo();
                    attMultimedia.setNombre(u[0]);
                    attMultimedia.setTipo(u[1]);
                    MongoDatabase db = this.accexternomongo.getMongoClient().getDatabase(nombd);
                    this.progress1 = 20;
                    MongoCollection<Document> doc = db.getCollection(this.getTabla().getNombre());
                    db.createCollection(tupla.getNombre());
                    this.progress1 = 30;
                    MongoCollection<Document> nuevoDoc = db.getCollection(tupla.getNombre());
                    Document where = null;
                    if (attMultimedia.getTipo().compareTo("double") == 0 || attMultimedia.getTipo().compareTo("decimal") == 0 || attMultimedia.getTipo().compareTo("int") == 0 || attMultimedia.getTipo().compareTo("long") == 0) {
                        where = new Document(attMultimedia.getNombre(), Double.parseDouble(tupla.getValor()));
                    } else {
                        where = new Document(attMultimedia.getNombre(), tupla.getValor());
                    }
                    MongoCursor<Document> resultado = doc.find(where).iterator();
                    this.progress1 = 50;
                    while (resultado.hasNext()) {
                        this.progress1++;
                        nuevoDoc.insertOne(resultado.next());
                        if (this.progress1 > 100) {
                            this.progress1 = 100;
                        }
                    }
                    if (this.progress1 < 100) {
                        this.progress1 = 100;
                    }
                }
            }
        } catch (Exception y) {
            y.printStackTrace();
        }
    }

    public double nOperacionesLog(UploadedFiles files) {
        double res = 0;
        for (UploadedFile f : files.getFiles()) {
            if (f != null && !"".equals(f.getFileName())) {
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(f.getInputStream(), "UTF-8"))) {
                    String actual = "";
                    while ((actual = bufferedReader.readLine()) != null) {
                        if (this.isRelacional()) {
                            if (actual.toLowerCase().contains("select") && actual.toLowerCase().contains(this.tabla.getNombre().toLowerCase())) {
                                res++;
                            }
                        } else {
                            if (actual.toLowerCase().contains("find") && actual.toLowerCase().contains(this.tabla.getNombre().toLowerCase())) {
                                res++;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return res;
    }

    public double nTuplas() {
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
                if (this.accexternomongo.conectar()) {
                    MongoDatabase db = this.accexternomongo.getMongoClient().getDatabase(nombd);
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
}
