package org.xamana.vertical;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import org.xamana.db.Atributo;
import org.xamana.db.bd;
import org.xamana.verticalCBIR.model.TablaCosto;

public class VerticalBD {

    bd oBD;
    String archivo;
    ArrayList<Atributo> arrAtri;
    ArrayList<String> arrCad;
    ArrayList<Atributo> arrNoLog;
    ArrayList<String> arrAM;
    UploadedFiles files;
    private ArrayList<HashMap<String, Double>> costoAtributoxSitio;
    private ArrayList<String> sitios;
    private ArrayList<TablaCosto> esquemaVertical;

    public VerticalBD(bd oBD, UploadedFiles files, ArrayList<String> attM) {
        this.oBD = oBD;
        this.files = files;
        if (attM.isEmpty()) {
            this.arrAM = null;
        } else {
            this.arrAM = attM;
        }
    }

    public VerticalBD(bd oBD, UploadedFiles files) {
        this.oBD = oBD;
        this.files = files;
    }

    public ArrayList<String> getArrAM() {
        return arrAM;
    }

    public void setArrAM(ArrayList<String> arrAM) {
        this.arrAM = arrAM;
    }

    //PIPE
    public ArrayList<TablaCosto> nombrar(ArrayList<TablaCosto> ori) {
        for (int i = 0; i < ori.size(); i++) {
            ori.get(i).setNombre(this.oBD.getTabla().getNombre() + "_" + (i + 1));
        }
        return ori;
    }

    //PIPE
    public ArrayList<TablaCosto> obtenerFragmentosMultimedia() {
        ArrayList<TablaCosto> arr = new ArrayList<>();
        for (String str : this.arrAM) {
            TablaCosto tabla = new TablaCosto();
            ArrayList<Atributo> listaAtr = new ArrayList<>();
            String[] atributos = str.split(",");
            listaAtr.add(this.oBD.getTabla().getAtributoLlave());
            this.oBD.getTabla().getAtributos().get(this.oBD.getTabla().buscaIndiceAttxNombre(atributos[0].split(":")[0])).setDescriptor(true);
            this.oBD.getTabla().getAtributos().get(this.oBD.getTabla().buscaIndiceAttxNombre(atributos[0].split(":")[0])).setMultimedia(false);
            listaAtr.add(this.oBD.getTabla().getAtributos().get(this.oBD.getTabla().buscaIndiceAttxNombre(atributos[0].split(":")[0])));
            this.oBD.getTabla().getAtributos().get(this.oBD.getTabla().buscaIndiceAttxNombre(atributos[1].split(":")[0])).setMultimedia(true);
            this.oBD.getTabla().getAtributos().get(this.oBD.getTabla().buscaIndiceAttxNombre(atributos[1].split(":")[0])).setDescriptor(false);
            listaAtr.add(this.oBD.getTabla().getAtributos().get(this.oBD.getTabla().buscaIndiceAttxNombre(atributos[1].split(":")[0])));
            tabla.setArrAtri(listaAtr);
            //¿A dónde se debe asignar?
            //¿Cúal es el costo total?
            //Primera posición sitio, segunda costo
            ArrayList<String> sitioCosto = new ArrayList<>();
            if (this.oBD.getTipoBD().compareTo("a") == 0) {
                sitioCosto = costoxSitioMySQL(listaAtr);
            } else if (this.oBD.getTipoBD().compareTo("b") == 0 || this.oBD.getTipoBD().compareTo("c") == 0) {
                sitioCosto = costoxSitioPostgres(listaAtr);
            } else {
                sitioCosto = costoxSitioMongo(listaAtr);
            }
            tabla.setSitio(sitioCosto.get(0));
            tabla.setCosto(Double.parseDouble(sitioCosto.get(1)));
            tabla.setMultimedia(true);
            arr.add(tabla);
        }
        return arr;
    }

    //PIPE
    public TablaCosto obtenerOtroFragmento() {
        TablaCosto ret = new TablaCosto();
        ArrayList<Atributo> normales = new ArrayList<>();
        normales.add(this.oBD.getTabla().getAtributoLlave());
        for (Atributo a : this.oBD.getTabla().getAtributos()) {
            if (!a.isDescriptor() && !a.isMultimedia() && a.getNombre().compareTo(this.oBD.getTabla().getAtributoLlave().getNombre()) != 0) {
                normales.add(a);
            }
        }
        if (!this.oBD.isNullorEmpty(normales)) {
            ret.setArrAtri(normales);
            //Primera posición sitio, segunda costo
            ArrayList<String> sitioCosto = new ArrayList<>();
            if (this.oBD.getTipoBD().compareTo("a") == 0) {
                sitioCosto = costoxSitioMySQL(normales);
            } else if (this.oBD.getTipoBD().compareTo("b") == 0 || this.oBD.getTipoBD().compareTo("c") == 0) {
                sitioCosto = costoxSitioPostgres(normales);
            } else {
                sitioCosto = costoxSitioMongo(normales);
            }
            ret.setSitio(sitioCosto.get(0));
            ret.setCosto(Double.parseDouble(sitioCosto.get(1)));
            ret.setMultimedia(false);
        } else {
            ret = null;
        }
        return ret;
    }

    //PIPE
    private ArrayList<String> costoxSitioMySQL(ArrayList<Atributo> listaAtr) {
        //Posición 1 sitio, posición 2 costo
        ArrayList<String> ret = new ArrayList<>();
        HashMap<String, String> NombreIp = new HashMap<>();
        HashMap<String, Double> SitioCosto = new HashMap<>();
        double costoTotal = 0;
        System.out.println("-----------COSTOS PIPE-------------");
        for (UploadedFile f : this.files.getFiles()) {
            if (f != null && !"".equals(f.getFileName())) {
                try ( BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(f.getInputStream(), "UTF-8"))) {
                    String actual = "";
                    String ipLinea = "";
                    String nombre = "";
                    while ((actual = bufferedReader.readLine()) != null) {
                        if (actual.contains("Connect\t") && !actual.contains("denied")) {
                            String y[] = actual.split("\t");
                            ipLinea = y[2].substring(y[2].indexOf("@") + 1, y[2].indexOf(" ", y[2].indexOf("@") + 1));
                            nombre = y[1].substring(0, y[1].indexOf(" "));
                            if (ipLinea.toLowerCase().compareTo("localhost") == 0 || ipLinea.toLowerCase().compareTo("::1") == 0) {
                                ipLinea = "127.0.0.1";
                            }
                            NombreIp.put(nombre, ipLinea);
                        }
                        int ValorAsignadoOperacion = 0;
                        if (actual.toLowerCase().contains("select") && actual.toLowerCase().contains(this.oBD.getTabla().getNombre().toLowerCase())) {
                            ValorAsignadoOperacion = 1;
                        } else if ((actual.toLowerCase().contains("delete")
                                || actual.toLowerCase().contains("insert")) && actual.toLowerCase().contains(this.oBD.getTabla().getNombre().toLowerCase())) {
                            ValorAsignadoOperacion = 2;
                        } else if (actual.toLowerCase().contains("update") && actual.toLowerCase().contains(this.oBD.getTabla().getNombre().toLowerCase())) {
                            ValorAsignadoOperacion = 3;
                        }
                        if (ValorAsignadoOperacion != 0) {
                            String y[] = actual.split("\t");
                            String sujeto = y[1].substring(0, y[1].indexOf(" "));
                            String ipTraduccion = NombreIp.get(sujeto);
                            System.out.println(actual);
                            System.out.println("SITIO: " + ipTraduccion);
                            if (ipTraduccion.compareTo(this.oBD.getDirbd()) != 0) {
                                ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                            }
                            System.out.println("VAO: " + ValorAsignadoOperacion);
                            int nJoins = incidencias(actual.toLowerCase(), " join ");
                            System.out.println("CR: " + nJoins);
                            double costoOperacion = 0.0;
                            for (Atributo a : listaAtr) {
                                if (actual.toLowerCase().contains(a.getNombre().toLowerCase())) {
                                    System.out.println("\tTamaño de atributo " + a.getNombre() + ": " + Double.parseDouble(a.getTamanio()));
                                    costoOperacion += ValorAsignadoOperacion * nJoins * Double.parseDouble(a.getTamanio());
                                    costoTotal += costoOperacion;
                                    //COSTO POR SITIO
                                    if (SitioCosto.get(ipTraduccion) != null) {
                                        SitioCosto.put(ipTraduccion, SitioCosto.get(ipTraduccion) + costoOperacion);
                                    } else {
                                        SitioCosto.put(ipTraduccion, costoOperacion);
                                    }
                                    System.out.println("Costo de operación: " + costoOperacion);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //DETERMINAR SITIO MÁS COSTOSO PARA FRAGMENTO
        Double max = -1.0;
        try {
            max = Collections.max(SitioCosto.values());
        } catch (Exception e) {
            max = 0.0;
        }
        for (Entry<String, Double> entry : SitioCosto.entrySet()) {
            if (entry.getValue() >= max) {
                if (ret.size() > 0) {
                    ret.set(0, entry.getKey());
                } else {
                    ret.add(entry.getKey());
                }

            }
        }
        if (ret.size() > 1) {
            ret.set(1, "" + costoTotal);
        } else if (ret.size() == 1) {
            ret.add("" + costoTotal);
        } else if (ret.isEmpty()) {
            ret.add(this.oBD.getDirbd());
            ret.add("" + costoTotal);
        }
        return ret;
    }

    //PIPE
    private ArrayList<String> costoxSitioPostgres(ArrayList<Atributo> listaAtr) {
        //Posición 1 sitio, posición 2 costo
        ArrayList<String> ret = new ArrayList<>();
        HashMap<String, String> NombreIp = new HashMap<>();
        HashMap<String, Double> SitioCosto = new HashMap<>();
        double costoTotal = 0;
        System.out.println("-----------COSTOS PIPE-------------");
        for (UploadedFile f : this.files.getFiles()) {
            if (f != null && !"".equals(f.getFileName())) {
                try ( BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(f.getInputStream(), "UTF-8"))) {
                    String actual = "";
                    String ipLinea = "";
                    String nombre = "";
                    while ((actual = bufferedReader.readLine()) != null) {
                        if (actual.toLowerCase().contains("conexion autorizada") || actual.toLowerCase().contains("connection authorized")) {
                            ipLinea = actual.toLowerCase().substring(actual.toLowerCase().indexOf("-_-") + 3, actual.toLowerCase().indexOf("log"));
                            nombre = actual.substring(actual.indexOf("[") + 1, actual.indexOf("]"));
                            if (ipLinea.toLowerCase().compareTo("localhost") == 0 || ipLinea.toLowerCase().compareTo("::1") == 0) {
                                ipLinea = "127.0.0.1";
                            }
                            NombreIp.put(nombre, ipLinea);
                        }
                        int ValorAsignadoOperacion = 0;
                        if (actual.toLowerCase().contains("select") && actual.toLowerCase().contains(this.oBD.getTabla().getNombre().toLowerCase())) {
                            ValorAsignadoOperacion = 1;
                        } else if ((actual.toLowerCase().contains("delete")
                                || actual.toLowerCase().contains("insert")) && actual.toLowerCase().contains(this.oBD.getTabla().getNombre().toLowerCase())) {
                            ValorAsignadoOperacion = 2;
                        } else if (actual.toLowerCase().contains("update") && actual.toLowerCase().contains(this.oBD.getTabla().getNombre().toLowerCase())) {
                            ValorAsignadoOperacion = 3;
                        }
                        if (ValorAsignadoOperacion != 0) {
                            String sujeto = actual.substring(actual.indexOf("[") + 1, actual.indexOf("]"));
                            String ipTraduccion = NombreIp.get(sujeto);
                            System.out.println(actual);
                            System.out.println("SITIO: " + ipTraduccion);
                            if (ipTraduccion.compareTo(this.oBD.getDirbd()) != 0) {
                                ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                            }
                            System.out.println("VAO: " + ValorAsignadoOperacion);
                            int nJoins = incidencias(actual.toLowerCase(), " join ");
                            System.out.println("CR: " + nJoins);
                            double costoOperacion = 0.0;
                            for (Atributo a : listaAtr) {
                                if (actual.toLowerCase().contains(a.getNombre().toLowerCase())) {
                                    System.out.println("\tTamaño de atributo " + a.getNombre() + ": " + Double.parseDouble(a.getTamanio()));
                                    costoOperacion += ValorAsignadoOperacion * nJoins * Double.parseDouble(a.getTamanio());
                                    costoTotal += costoOperacion;
                                    //COSTO POR SITIO
                                    if (SitioCosto.get(ipTraduccion) != null) {
                                        SitioCosto.put(ipTraduccion, SitioCosto.get(ipTraduccion) + costoOperacion);
                                    } else {
                                        SitioCosto.put(ipTraduccion, costoOperacion);
                                    }
                                    System.out.println("Costo de operación: " + costoOperacion);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //DETERMINAR SITIO MÁS COSTOSO PARA FRAGMENTO
        Double max = -1.0;
        try {
            max = Collections.max(SitioCosto.values());
        } catch (Exception e) {
            max = 0.0;
        }
        for (Entry<String, Double> entry : SitioCosto.entrySet()) {
            if (entry.getValue() >= max) {
                if (ret.size() > 0) {
                    ret.set(0, entry.getKey());
                } else {
                    ret.add(entry.getKey());
                }

            }
        }
        if (ret.size() > 1) {
            ret.set(1, "" + costoTotal);
        } else if (ret.size() == 1) {
            ret.add("" + costoTotal);
        } else if (ret.isEmpty()) {
            ret.add(this.oBD.getDirbd());
            ret.add("" + costoTotal);
        }
        return ret;
    }

    //PIPE
    private ArrayList<String> costoxSitioMongo(ArrayList<Atributo> listaAtr) {
        ArrayList<String> ret = new ArrayList<>();
        HashMap<String, String> NombreIp = new HashMap<>();
        HashMap<String, Double> SitioCosto = new HashMap<>();
        double costoTotal = 0;
        System.out.println("-----------COSTOS PIPE-------------");
        for (UploadedFile f : this.files.getFiles()) {
            if (f != null && !"".equals(f.getFileName())) {
                try ( BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(f.getInputStream(), "UTF-8"))) {
                    String actual = "";
                    String ipLinea = "";
                    String nombre = "";
                    while ((actual = bufferedReader.readLine()) != null) {
                        if (actual.toLowerCase().contains("command") && actual.toLowerCase().contains(this.oBD.getTabla().getNombre().toLowerCase())) {
                            JSONObject j = new JSONObject(actual);
                            String c = j.get("c").toString().toLowerCase().trim();
                            JSONObject j2 = new JSONObject(j.get("attr").toString());
                            JSONObject j3 = new JSONObject(j2.get("command").toString());
                            String ns = "";
                            int ValorAsignadoOperacion = 0;
                            if (j3.has("find")) {
                                ValorAsignadoOperacion = 1;
                                ns = j3.get("find").toString().toLowerCase();
                            } else if (j3.has("delete")) {
                                ValorAsignadoOperacion = 2;
                                ns = j3.get("delete").toString().toLowerCase();
                            } else if (j3.has("insert")) {
                                ValorAsignadoOperacion = 2;
                                ns = j3.get("insert").toString().toLowerCase();
                            } else if (j3.has("update")) {
                                ValorAsignadoOperacion = 3;
                                ns = j3.get("update").toString().toLowerCase();
                            }
                            if (ValorAsignadoOperacion != 0 && c.compareTo("command") == 0 && ns.compareTo(this.oBD.getTabla().getNombre().toLowerCase()) == 0) {
                                j2 = new JSONObject(j.get("attr").toString());
                                String remote = j2.get("remote").toString().toLowerCase().trim();
                                ipLinea = remote.substring(0, remote.indexOf(":"));
                                System.out.println(actual);
                                System.out.println("SITIO: " + ipLinea);
                                if (this.oBD.getDirbd().compareTo(ipLinea) != 0) {
                                    ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                                }
                                System.out.println("VAO: " + ValorAsignadoOperacion);
                                int nJoins = incidencias(actual.toLowerCase(), "$lookup");
                                System.out.println("CR: " + nJoins);
                                double costoOperacion = 0.0;
                                for (Atributo a : listaAtr) {
                                    if (actual.toLowerCase().contains(a.getNombre().toLowerCase())) {
                                        System.out.println("\tTamaño de atributo " + a.getNombre() + ": " + Double.parseDouble(a.getTamanio()));
                                        costoOperacion += ValorAsignadoOperacion * nJoins * Double.parseDouble(a.getTamanio());
                                        costoTotal += costoOperacion;
                                        //COSTO POR SITIO
                                        if (SitioCosto.get(ipLinea) != null) {
                                            SitioCosto.put(ipLinea, SitioCosto.get(ipLinea) + costoOperacion);
                                        } else {
                                            SitioCosto.put(ipLinea, costoOperacion);
                                        }
                                        System.out.println("Costo de operación: " + costoOperacion);
                                    }
                                }
                            }
                        }
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        //DETERMINAR SITIO MÁS COSTOSO PARA FRAGMENTO
        Double max = -1.0;
        try {
            max = Collections.max(SitioCosto.values());
        } catch (Exception e) {
            max = 0.0;
        }
        for (Entry<String, Double> entry : SitioCosto.entrySet()) {
            if (entry.getValue() >= max) {
                if (ret.size() > 0) {
                    ret.set(0, entry.getKey());
                } else {
                    ret.add(entry.getKey());
                }

            }
        }
        if (ret.size() > 1) {
            ret.set(1, "" + costoTotal);
        } else if (ret.size() == 1) {
            ret.add("" + costoTotal);
        } else if (ret.isEmpty()) {
            ret.add(this.oBD.getDirbd());
            ret.add("" + costoTotal);
        }
        return ret;
    }

    //PIPE
    private int incidencias(String origen, String palabra) {
        //costo de reuniones=n+1 
        int i = 1;
        while (origen.indexOf(palabra) > -1) {
            origen = origen.substring(origen.indexOf(palabra) + palabra.length(), origen.length());
            i++;
        }
        return i;
    }

    /**
     * @return the costoAtributoxSitio
     */
    public ArrayList<HashMap<String, Double>> getCostoAtributoxSitio() {
        return costoAtributoxSitio;
    }

    /**
     * @param costoAtributoxSitio the costoAtributoxSitio to set
     */
    public void setCostoAtributoxSitio(ArrayList<HashMap<String, Double>> costoAtributoxSitio) {
        this.costoAtributoxSitio = costoAtributoxSitio;
    }

    /**
     * @return the sitios
     */
    public ArrayList<String> getSitios() {
        return sitios;
    }

    /**
     * @param sitios the sitios to set
     */
    public void setSitios(ArrayList<String> sitios) {
        this.sitios = sitios;
    }

    public void esquemaVerticalMySQL() {

    }

    //PIPE
    public void esquemaVerticalPostgres() {
        this.oBD.setNombreIp(new HashMap<>());
        costoAtributoxSitio = new ArrayList<>();
        for (Atributo l : this.oBD.getTabla().getAtributos()) {
            costoAtributoxSitio.add(new HashMap<String, Double>());
        }
        HashMap<String, Double> costoxSitio = new HashMap<>();
        System.out.println("-----------COSTOS ATRIBUTOS PIPE-------------");
        for (UploadedFile f : this.files.getFiles()) {
            if (f != null && !"".equals(f.getFileName())) {
                try ( BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(f.getInputStream(), "UTF-8"))) {
                    String actual = "";
                    String ipLinea = "";
                    String nombre = "";
                    while ((actual = bufferedReader.readLine()) != null) {
                        if (actual.toLowerCase().contains("conexion autorizada") || actual.toLowerCase().contains("connection authorized")) {
                            ipLinea = actual.toLowerCase().substring(actual.toLowerCase().indexOf("-_-") + 3, actual.toLowerCase().indexOf("log"));
                            nombre = actual.substring(actual.indexOf("[") + 1, actual.indexOf("]"));
                            if (ipLinea.toLowerCase().compareTo("localhost") == 0 || ipLinea.toLowerCase().compareTo("::1") == 0) {
                                ipLinea = "127.0.0.1";
                            }
                            this.oBD.getNombreIp().put(nombre, ipLinea);
                        }
                        int ValorAsignadoOperacion = 0;
                        if (actual.toLowerCase().contains("select") && actual.toLowerCase().contains(this.oBD.getTabla().getNombre().toLowerCase())) {
                            ValorAsignadoOperacion = 1;
                        } else if ((actual.toLowerCase().contains("delete")
                                || actual.toLowerCase().contains("insert")) && actual.toLowerCase().contains(this.oBD.getTabla().getNombre().toLowerCase())) {
                            ValorAsignadoOperacion = 2;
                        } else if (actual.toLowerCase().contains("update") && actual.toLowerCase().contains(this.oBD.getTabla().getNombre().toLowerCase())) {
                            ValorAsignadoOperacion = 3;
                        }
                        if (ValorAsignadoOperacion != 0) {
                            String sujeto = actual.substring(actual.indexOf("[") + 1, actual.indexOf("]"));
                            String ipTraduccion = this.oBD.getNombreIp().get(sujeto);
                            System.out.println(actual);
                            System.out.println("SITIO: " + ipTraduccion);
                            if (ipTraduccion.compareTo(this.oBD.getDirbd()) != 0) {
                                ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                            }
                            System.out.println("VAO: " + ValorAsignadoOperacion);
                            int nJoins = incidencias(actual.toLowerCase(), " join ");
                            System.out.println("CR: " + nJoins);
                            for (int o = 0; o < this.oBD.getTabla().getAtributos().size(); o++) {
                                Atributo a = this.oBD.getTabla().getAtributos().get(o);
                                System.out.println("Atributo: " + a.getNombre() + " indice: " + o);
                                if (actual.toLowerCase().contains(a.getNombre().toLowerCase())) {
                                    HashMap<String, Double> atributoSitios = new HashMap<>();
                                    if (costoAtributoxSitio.get(o) != null) {
                                        atributoSitios = costoAtributoxSitio.get(o);
                                    }
                                    double valor = 0.0;
                                    if (atributoSitios.containsKey(ipTraduccion)) {
                                        valor = atributoSitios.get(ipTraduccion);
                                    }
                                    valor += ValorAsignadoOperacion * nJoins * Double.parseDouble(a.getTamanio());
                                    System.out.println("Valor actual: " + valor);
                                    System.out.println("\tTamaño de atributo " + a.getNombre() + ": " + Double.parseDouble(a.getTamanio()));
                                    atributoSitios.put(ipTraduccion, valor);
                                        costoAtributoxSitio.set(o, atributoSitios);
                                    //Costo por sitio
                                    Double costoSitio = 0.0;
                                    if (costoxSitio.containsKey(ipTraduccion)) {
                                        costoSitio = costoxSitio.get(ipTraduccion);
                                    }
                                    costoSitio += valor;
                                    costoxSitio.put(ipTraduccion, costoSitio);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        this.sitios = deHashaArraySitios();
        this.esquemaVertical = new ArrayList<>();
        for(int p=0;p<costoAtributoxSitio.size();p++){
            for(String s:sitios){
                if(costoAtributoxSitio.get(p).get(s)==null || costoAtributoxSitio.get(p).get(s)==Double.NaN){
                   costoAtributoxSitio.get(p).put(s, 0.0);
                }
            }
        }
        //DETERMINAR FRAGMENTOS
        ArrayList<Atributo> attsSinCosto = new ArrayList<>();
        for (String clave : costoxSitio.keySet()) {
            if (costoxSitio.get(clave) > 0) {
                ArrayList<Atributo> atts = new ArrayList<>();
                TablaCosto tc = new TablaCosto();
                tc.setSitio(clave);
                //llenar atributos
                double costoFragmento = 0.0;
                for (int o = 0; o < this.oBD.getTabla().getAtributos().size(); o++) {
                    Atributo a = this.oBD.getTabla().getAtributos().get(o);
                    HashMap<String, Double> lineaAtt = costoAtributoxSitio.get(o);
                    try {
                        if (this.getMaxEntry(lineaAtt).getKey().compareTo(clave) == 0) {
                            costoFragmento += lineaAtt.get(clave);
                            atts.add(a);
                        }
                    } catch (Exception e) {
                        //OBTUBIERON CERO EN COSTO, NO ESTAN EN LOG
                        if (!attsSinCosto.contains(a)) {
                            attsSinCosto.add(a);
                        }
                    }
                }
                tc.setCosto(costoFragmento);
                tc.setArrAtri(atts);
                this.esquemaVertical.add(tc);
            }
        }
        if (attsSinCosto.size() > 0) {
            esquemaVertical.get(0).getArrAtri().addAll(attsSinCosto);
        }
        esquemaVertical = this.nombrar(esquemaVertical);
    }

    public void esquemaVerticalMongo() {

    }

    //PIPE
    public ArrayList<String> deHashaArraySitios() {
        ArrayList<String> ret = new ArrayList<>();
        for (String clave : this.oBD.getNombreIp().keySet()) {
            String valor = this.oBD.getNombreIp().get(clave);
            System.out.println(valor);
            ret.add(valor);
        }
        return ret;
    }

    /**
     * @return the esquemaVertical
     */
    public ArrayList<TablaCosto> getEsquemaVertical() {
        return esquemaVertical;
    }

    /**
     * @param esquemaVertical the esquemaVertical to set
     */
    public void setEsquemaVertical(ArrayList<TablaCosto> esquemaVertical) {
        this.esquemaVertical = esquemaVertical;
    }

    //PIPE
    public Entry<String, Double> getMaxEntry(Map<String, Double> map) {
        Entry<String, Double> maxEntry = null;
        Double max = -1.0;
        try {
            max = Collections.max(map.values());
        } catch (Exception e) {
            max = 0.0;
        }
        for (Entry<String, Double> entry : map.entrySet()) {
            Double value = entry.getValue();
            if (null != value && value > max) {
                maxEntry = entry;
                max = maxEntry.getValue();
            }
        }
        return maxEntry;
    }
}
