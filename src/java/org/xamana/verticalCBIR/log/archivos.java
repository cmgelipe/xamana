package org.xamana.verticalCBIR.log;

import org.xamana.datos.AccesoDatosPG;
import java.io.*;
import static java.lang.Integer.parseInt;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.primefaces.model.file.UploadedFiles;
import org.xamana.crearfrag.BuildTypes;
import org.xamana.db.Atributo;
import org.xamana.db.bd;
import org.xamana.verticalCBIR.model.Query;
import org.xamana.verticalCBIR.model.TablaCosto;
import org.xamana.verticalCBIR.model.sitio;

public class archivos {

    bd oBD;
    String archivo;
    ArrayList<Atributo> arrAtri;
    ArrayList<String> arrCad;
    ArrayList<Atributo> arrNoLog;
    ArrayList<String> arrAM;

    public ArrayList<Atributo> getArrNoLog() {
        return arrNoLog;
    }

    public void setArrNoLog(ArrayList<Atributo> arrNoLog) {
        this.arrNoLog = arrNoLog;
    }

    public ArrayList<String> getArrAM() {
        return arrAM;
    }

    public void setArrAM(ArrayList<String> arrAM) {
        this.arrAM = arrAM;
    }

    public archivos(bd oBD, UploadedFiles files, ArrayList<String> attM) throws IOException {
        this.oBD = oBD;
        this.arrCad = leerTxt(files);
        this.arrAtri = listaAtributos();
        if (attM.isEmpty()) {
            this.arrAM = null;
        } else {
            this.arrAM = attM;
        }
    }

    public archivos(bd oBD, UploadedFiles files) throws IOException {
        this.oBD = oBD;
        this.arrCad = leerTxt(files);
        this.arrAtri = listaAtributos();

    }

    public String getArchivo() {
        return archivo;
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }

    public ArrayList<String> getArrCad() {
        return arrCad;
    }

    public void setArrCad(ArrayList<String> arrCad) {
        this.arrCad = arrCad;
    }

    public ArrayList<Atributo> getArrAtri() {
        return arrAtri;
    }

    public void setArrAtri(ArrayList<Atributo> arrAtri) {
        this.arrAtri = arrAtri;
    }

    public void imprimeQuery(Query q) {

        System.out.println("------Imprime Query-------");
        System.out.println("Tipo de operacion: " + q.getTipoOpera());
        System.out.println("Sitio: " + q.getSitio().getNombre());
        System.out.println("Atributos:***** ");

        q.getAtr().forEach(att -> {
            System.out.println("Atributo: " + att.getNombre());
        });
        System.out.println("Frecuencia: " + q.getFrecuencia());
    }

    public ArrayList<String> leerTxt(UploadedFiles cad) throws FileNotFoundException, IOException {
        ArrayList<String> lineaArchivo = new ArrayList();

        cad.getFiles().stream().filter((f) -> (f != null && !"".equals(f.getFileName()))).forEachOrdered((f) -> {
            try ( BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(f.getInputStream(), "UTF-8"))) {
                String actual = "";

                while ((actual = bufferedReader.readLine()) != null) {
                    lineaArchivo.add(actual);
                }

            } catch (Exception e) {
            }
        });

        return lineaArchivo;
    }

    public ArrayList<Atributo> listaAtributos() {
        @SuppressWarnings("unchecked")
        ArrayList<Atributo> atts = new ArrayList();
        ArrayList<String> arr;

        try {
            AccesoDatosPG acc = new AccesoDatosPG(this.oBD.getDirbd() + ":" + this.oBD.getPortbd(), this.oBD.getNombd(), this.oBD.getUsubd(), this.oBD.getPassbd());
            if (acc.conectar()) {
                arr = acc.atributos(this.oBD.getNombd(), this.oBD.getTabla().getNombre());
                acc.desconectar();
                for (int i = 0; i < arr.size(); i++) {
                    String[] t = arr.get(i).split(":");
                    Atributo att = new Atributo();
                    att.setNombre(t[0]);
                    att.setTipo(t[1]);
                    att.setTamanio(t[2]);
                    atts.add(att);
                }
            } else {
                System.out.print("no hay conexion");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return atts;
    }

    public Query obtenerQuery(ArrayList<String> log, String cad) {
        String tipoOperacion;
        tipoOperacion = "";
        sitio s = obtenerSitio(cad);
        ArrayList<Atributo> att = obtenerAttParticipantes(this.arrAtri, cad);
        tipoOperacion = obtenerTipo(cad);
        Query obj = new Query();

        obj.setAtr(att);
        obj.setSitio(s);
        obj.setTipoOpera(tipoOperacion);
        obj.setFrecuencia(obtenerFrecuencia(log, s, tipoOperacion, att));

        return obj;
    }

    public ArrayList<Atributo> obtenerAttParticipantes(ArrayList<Atributo> atributos, String consulta) {
        @SuppressWarnings("unchecked")
        ArrayList<Atributo> listaAt = new ArrayList();
        atributos.forEach(c -> {
            if (consulta.contains(c.getNombre()) && c.getNombre().compareTo(this.oBD.getTabla().getAtributoLlave().getNombre())!=0) {
                listaAt.add(c);
            }
        });
        return listaAt;
    }

    public ArrayList<String> obtenerConsultasIp(sitio s, ArrayList<String> cad) {
        @SuppressWarnings("unchecked")
        ArrayList<String> con = new ArrayList();
        ArrayList<String> consu = obtenerConsulta(cad);

        consu.forEach(c -> {
            if (c.contains(s.getNombre())) {
                con.add(c);
            }
        });
        return con;
    }

    public double obtenerFrecuencia(ArrayList<String> log, sitio s, String tipoOperacion, ArrayList<Atributo> att) {
        double f = 0;
        ArrayList<String> cons = obtenerConsulta(log);
        ArrayList<Atributo> atributos = listaAtributos();
        ArrayList<String> consultalimpia = obtenerConTipo(obtenerConsultasIp(s, cons), tipoOperacion);

        for (int j = 0; j < consultalimpia.size(); j++) {
            ArrayList<Atributo> attParticipan = obtenerAttParticipantes(atributos, consultalimpia.get(j));
            if (attParticipan.size() == att.size()) {
                for (int i = 0; i < att.size(); i++) {
                    if (att.get(i).getNombre().equals(attParticipan.get(i).getNombre())) {
                        f = f + 1;
                    }
                }
            }
        }

        return (f / att.size());
    }

    public ArrayList<TablaCosto> sumar(ArrayList<TablaCosto> original) {
        @SuppressWarnings("unchecked")
        ArrayList<TablaCosto> tabla = new ArrayList();
        for (int i = 0; i < original.size(); i++) {
            boolean flag = false;
            for (TablaCosto u : tabla) {
                if (u.getSitio().getNombre().compareTo(original.get(i).getSitio().getNombre()) == 0 && u.getAtributo().getNombre().compareTo(original.get(i).getAtributo().getNombre()) == 0) {
                    u.setCosto(u.getCosto() + original.get(i).getCosto());
                    flag = true;
                }
            }
            if (!flag) {
                tabla.add(original.get(i));
            }
        }

        return asignar(tabla);
        //return tabla;
    }

    public ArrayList<TablaCosto> asignar(ArrayList<TablaCosto> original) {
        @SuppressWarnings("unchecked")
        ArrayList<TablaCosto> tabla = new ArrayList();
        TablaCosto aux = new TablaCosto();

        for (TablaCosto t : original) {

            aux = t;

            for (int i = 0; i < original.size(); i++) {
                if (aux.getAtributo().getNombre().compareTo(original.get(i).getAtributo().getNombre()) == 0) {
                    if (aux.getCosto() < original.get(i).getCosto()) {
                        aux = original.get(i);
                    }
                }

            }
            tabla.add(aux);
        }

        //return mismoSitio(repetidos(tabla));
        return repetidos(tabla);
    }

    public ArrayList<TablaCosto> repetidos(ArrayList<TablaCosto> original) {
        @SuppressWarnings("unchecked")
        ArrayList<TablaCosto> list = new ArrayList();
        for (int i = 0; i < original.size(); i++) {
            if (list.isEmpty()) {
                list.add(original.get(i));
            } else {
                if (!existe2(list, original.get(i))) {
                    list.add(original.get(i));
                }
            }

        }

        return nombrar(list);
    }

    private boolean existe2(ArrayList<TablaCosto> list, TablaCosto get) {
        boolean r = false;

        for (TablaCosto k : list) {
            if (get.getAtributo().getNombre().compareTo(k.getAtributo().getNombre()) == 0) {
                r = true;
            }
        }

        return r;
    }

    public ArrayList<TablaCosto> nombrar(ArrayList<TablaCosto> ori) {

        for (int i = 0; i < ori.size(); i++) {
            ori.get(i).setNombre(this.oBD.getTabla().getNombre() + "_" + (i + 1));
        }
        return ori;
    }

    public ArrayList<TablaCosto> mismoSitio(ArrayList<TablaCosto> ta) {

        ArrayList<TablaCosto> t = new ArrayList<>();
        @SuppressWarnings("unchecked")
        ArrayList<sitio> si = new ArrayList();
        for (TablaCosto t1 : ta) {
            if (si.isEmpty()) {
                si.add(t1.getSitio());
            } else if (!existeEnArrSitio(t1, si)) {
                si.add(t1.getSitio());
            }
        }
        for (sitio s : si) {
            @SuppressWarnings("unchecked")
            ArrayList<Atributo> arraux = new ArrayList();
            TablaCosto aux = new TablaCosto();
            aux.setNombre("");
            aux.setSitio(s);
            aux.setCosto(0.0);
            arraux.add(obteneratributoid());

            for (TablaCosto t2 : ta) {

                if (t2.getSitio().getIp().compareTo(s.getIp()) == 0) {
                    arraux.add(t2.getAtributo());
                }
            }
            aux.setArrAtri(arraux);
            // aux.getArrAtri().forEach(at->System.out.println(at.getNombre()));
            t.add(aux);
        }

        return t;
    }

    private boolean existeEnArrSitio(TablaCosto t1, ArrayList<sitio> si) {
        boolean r = false;
        for (sitio s : si) {
            if (s.getNombre().compareTo(t1.getSitio().getNombre()) == 0) {
                r = true;
            }

        }
        return r;
    }

    public TablaCosto noLog(ArrayList<TablaCosto> t) {
        ArrayList<Atributo> aa = new ArrayList();
        ArrayList<Atributo> attTa = new ArrayList();
        ArrayList<String> log = new ArrayList();
        ArrayList<String> atris = new ArrayList();
        aa.add(obteneratributoid());
        for (TablaCosto ta : t) {
            attTa.addAll(ta.getArrAtri());
        }
        attTa.forEach(atr -> log.add(atr.getNombre()));
        this.arrAtri.forEach(att -> atris.add(att.getNombre()));
        atris.removeAll(log);
        this.arrAtri.forEach(atrib -> {
            if (atrib.getNombre().compareTo(this.oBD.getTabla().getAtributoLlave().getNombre())!=0) {
                atris.forEach(j -> {
                    if (j.toLowerCase().compareTo(atrib.getNombre().toLowerCase()) == 0) {
                        aa.add(atrib);
                    }
                });
            }
        });
        TablaCosto nueva = new TablaCosto();
        if (aa.size() != 1) {

            nueva.setArrAtri(aa);
            nueva.setCosto(0);
            nueva.setSitio(new sitio("localhost", this.oBD.getDirbd()));
        } else {
            nueva = null;
        }
        return nueva;
    }

    public ArrayList<TablaCosto> obtenerFragMul() {
        ArrayList<TablaCosto> arr = new ArrayList<>();
        ArrayList<Atributo> listaAtr;
        for (String str : this.arrAM) {
            listaAtr = new ArrayList<>();
            TablaCosto tabla = new TablaCosto();
            tabla.setSitio(new sitio("localhost", this.oBD.getDirbd()));
            listaAtr.add(obteneratributoid());
            for (Atributo atr : this.arrAtri) {
                if (str.contains(atr.getNombre())) {
                    listaAtr.add(atr);
                }
            }
            tabla.setArrAtri(listaAtr);
            tabla.setCosto(0);
            arr.add(tabla);
        }
        return arr;
    }

    private Atributo obteneratributoid() {
        Atributo atri = new Atributo();
        for (int i = 0; i < this.arrAtri.size(); i++) {
            if (this.arrAtri.get(i).getNombre().compareTo(this.oBD.getTabla().getAtributoLlave().getNombre())==0) {
                atri = this.arrAtri.get(i);
            }
        }

        return atri;
    }

    public ArrayList<String> obtenerConTipo(ArrayList<String> cad, String tipo) {
        @SuppressWarnings("unchecked")
        ArrayList<String> con = new ArrayList();
        for (int i = 0; i < cad.size(); i++) {
            if (cad.get(i).contains(tipo)) {
                con.add(cad.get(i));
            }

        }

        return con;
    }

    public ArrayList<TablaCosto> obtenerTablaCosto() {
        ArrayList<String> cad = this.arrCad;
        ArrayList<Query> tablafrecuencia = arrQuerys(cad);
        @SuppressWarnings("unchecked")
        ArrayList<TablaCosto> tabla = new ArrayList();
        tablafrecuencia.forEach(new Consumer<Query>() {
            public void accept(Query t) {
                for (int i = 0; i < t.getAtr().size(); i++) {
                    int vo = 0;
                    double f = 0;
                    int ta = 0;
                    Atributo a = t.getAtr().get(i);
                    Atributo id = obteneratributoid();
                    ArrayList<Atributo> arrT = new ArrayList<>();
                    arrT.add(id);
                    TablaCosto costo = new TablaCosto();

                    if (t.getTipoOpera().contains("select")) {
                        vo = 1;
                    }
                    if (t.getTipoOpera().contains("create")) {
                        vo = 2;
                    }
                    if (t.getTipoOpera().contains("delete")) {
                        vo = 2;
                    }
                    if (t.getTipoOpera().contains("update")) {
                        vo = 3;
                    }
                    f = t.getFrecuencia();
                    ta = parseInt(a.getTamanio());
                    costo.setAtributo(a);
                    arrT.add(a);
                    costo.setArrAtri(arrT);
                    costo.setSitio(t.getSitio());
                    costo.setCosto((vo * f * ta));
                    tabla.add(costo);
                }
            }
        });
        return sumar(tabla);
        // return tabla;
    }

    public ArrayList<String> obtenerConsulta(ArrayList<String> cad) {
        @SuppressWarnings("unchecked")
        ArrayList<String> tipoC = new ArrayList();
        for (int i = 0; i < cad.size(); i++) {
            if (cad.get(i).toLowerCase().contains("sentencia:")
                    && cad.get(i).toLowerCase().contains(this.oBD.getTabla().getNombre())) {
                String tipo = cad.get(i);
                tipoC.add(tipo);
            }
        }
        return tipoC;
    }

    public void imprimeQ(Query q) {
        System.out.println(" ===Query ===");
        System.out.println("Tipo de operacion " + q.getTipoOpera());
        System.out.println(" Sitio " + q.getSitio().getIp());
        System.out.println(" Atributos involucrados " + q.getAtributos());
        System.out.println("Frecuencia " + q.getFrecuencia());
        System.out.println("==========");
        System.out.println(" ");

    }

    public sitio obtenerSitio(String cad) {

        String nom = cad.toLowerCase().substring(cad.toLowerCase().indexOf("[") + 1, cad.toLowerCase().indexOf("]"));
        String ip = cad.toLowerCase().substring(cad.toLowerCase().indexOf("-_-") + 3, cad.toLowerCase().indexOf("l"));

        sitio s = new sitio(nom, ip);

        return s;
    }

    public String obtenerTipo(String cad) {
        String tipo = "";

        if (cad.toLowerCase().contains("select")) {
            tipo = "select";
        }

        if (cad.toLowerCase().contains("update")) {
            tipo = "update";
        }

        if (cad.toLowerCase().contains("delete")) {
            tipo = "delete";
        }
        if (cad.toLowerCase().contains("create")) {
            tipo = "create";
        }

        return tipo;

    }

    public ArrayList<Query> arrQuerys(ArrayList<String> cad) {

        @SuppressWarnings("unchecked")
        ArrayList<Query> frec = new ArrayList();
        ArrayList<Query> repetidos = new ArrayList();
        ArrayList<Query> unicos = new ArrayList();
        obtenerConsulta(cad).forEach(t -> {
            Query q = obtenerQuery(cad, t.toLowerCase());

            if (q.getFrecuencia() == 1) {
                frec.add(q);
            }
            if (q.getFrecuencia() > 1) {
                repetidos.add(q);
            }
        });
        unicos.addAll(frec);
        unicos.addAll(limpiar(repetidos));
        return unicos;
    }

///////////// aqui lo debo quitar cosas
    public ArrayList<Query> limpiar(ArrayList<Query> cad) {

        @SuppressWarnings("unchecked")
        ArrayList<Query> list = new ArrayList();

        for (int i = 0; i < cad.size(); i++) {
            if (list.isEmpty()) {
                list.add(cad.get(i));
            }
            if (!existe(list, cad.get(i))) {
                list.add(cad.get(i));
            }
        }
        return list;
    }

    public boolean existe(ArrayList<Query> cad, Query q) {
        boolean r = false;

        for (int i = 0; i < cad.size(); i++) {
            if (q.getFrecuencia() == cad.get(i).getFrecuencia()) {
                if (q.getSitio().getIp().compareTo(cad.get(i).getSitio().getIp()) == 0) {
                    if (q.getAtributos().compareTo(cad.get(i).getAtributos()) == 0) {
                        r = true;
                    } else {
                        r = false;
                    }
                } else {
                    r = false;
                }
            } else {
                r = false;
            }
        }
        return r;
    }
    ////

    public void fragmentarVerCBIRPostgres(ArrayList<TablaCosto> esquemaVertical) {
        try {
//            this.guardarDatos();
            for (TablaCosto frag : esquemaVertical) {
                crearTabla(frag);
            }
        } catch (Exception e) {
            System.out.println("Sin conexión");
        }
    }

    private void crearTabla(TablaCosto frag) {

        try {
            String sql;
            ArrayList atributos = null;
            AccesoDatosPG acc2 = new AccesoDatosPG(this.oBD.getDirbd() + ":" + this.oBD.getPortbd(), this.oBD.getNombd(), this.oBD.getUsubd(), this.oBD.getPassbd());
            if (acc2.conectar()) {
                sql = "Select " + atri(frag.getArrAtri()) + " from " + this.oBD.getTabla().getNombre() + " ;";
                System.out.println(sql);
                atributos = acc2.ejecutarConsulta(sql);
                //sql="alter table "+frag.getNombre()+" ADD PRIMARY KEY ("+atributos.get(0)+");";
            }
            AccesoDatosPG acc = new AccesoDatosPG(frag.getSitio().getIp() + ":" + this.oBD.getPortbd(), this.oBD.getNombd(), this.oBD.getUsubd(), this.oBD.getPassbd());
            //AccesoDatosPG acc= new AccesoDatosPG(this.direccion,this.nomDB,"postgres",this.pass);
            if (acc.conectar()) {
                sql = "drop table if exists " + frag.getNombre() + " ; CREATE TABLE " + frag.getNombre() + " ( " + atributosTipos(frag.getArrAtri()) + " );";
                System.out.println(sql);
                acc.ejecutarComando(sql);
                if (atributos != null) {
                    for (int i = 0; i < atributos.size(); i++) {
                        String valor = "";
                        ArrayList tupla = (ArrayList) atributos.get(i);
                        for (int j = 0; j < tupla.size(); j++) {
                            valor += "'" + tupla.get(j) + "',";
                        }
                        valor = valor.substring(0, valor.length() - 1);
                        sql = "insert into " + frag.getNombre() + "(" + frag.getAtributos() + ") values (" + valor + ");";
                        System.out.println(sql);
                        acc.ejecutarComando(sql);
                    }
                }

                acc2.desconectar();
                acc.desconectar();
            }
        } catch (Exception ex) {
        }

    }

    private String atri(ArrayList<Atributo> arrAtri) {
        String separador = ", ";
        @SuppressWarnings("unchecked")
        ArrayList<String> cad = new ArrayList();
        arrAtri.forEach((att) -> {
            cad.add(att.getNombre());
        });
        return cad.stream().map(String::toLowerCase).collect(Collectors.joining(separador));
    }

    private String atributosTipos(ArrayList<Atributo> arrAtri) {
        @SuppressWarnings("unchecked")
        ArrayList<String> cad = new ArrayList();
        String separador = ", ";
        arrAtri.forEach((att) -> {
            cad.add(att.getNombre() + " " + BuildTypes.determineTypes(att.getTipo(), att.getTamanio()));
        });
        return cad.stream().map(String::toLowerCase).collect(Collectors.joining(separador));
    }

}
