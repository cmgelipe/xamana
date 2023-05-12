package org.xamana.vertical;

import java.io.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.json.JSONArray;
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
    private HashMap<ArrayList<Atributo>, HashMap<String, Double>> atributosSitioCostos = new HashMap<>();//afinidad

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
            tabla.setMultimedia(true);
            //¿A dónde se debe asignar?
            //¿Cúal es el costo total?
            //Primera posición sitio, segunda costo
            /*ArrayList<String> sitioCosto = new ArrayList<>();
            if (this.oBD.getTipoBD().compareTo("a") == 0) {
                sitioCosto = costoxSitioMySQL(listaAtr);
            } else if (this.oBD.getTipoBD().compareTo("b") == 0 || this.oBD.getTipoBD().compareTo("c") == 0) {
                sitioCosto = costoxSitioPostgres(listaAtr);
            } else {
                sitioCosto = costoxSitioMongo(listaAtr);
            }
            tabla.setSitio(sitioCosto.get(0));
            tabla.setCosto(Double.parseDouble(sitioCosto.get(1)));*/
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
            ret.setMultimedia(false);

            //Primera posición sitio, segunda costo
            /*ArrayList<String> sitioCosto = new ArrayList<>();
            if (this.oBD.getTipoBD().compareTo("a") == 0) {
                sitioCosto = costoxSitioMySQL(normales);
            } else if (this.oBD.getTipoBD().compareTo("b") == 0 || this.oBD.getTipoBD().compareTo("c") == 0) {
                sitioCosto = costoxSitioPostgres(normales);
            } else {
                sitioCosto = costoxSitioMongo(normales);
            }
            ret.setSitio(sitioCosto.get(0));
            ret.setCosto(Double.parseDouble(sitioCosto.get(1)));*/
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
    public ArrayList<TablaCosto> costoxSitioMongoV2(ArrayList<TablaCosto> preEsquema) {
        ArrayList<TablaCosto> ret = new ArrayList<>();
        int contador = 0;
        double costoTotal = 0.0;
        System.out.println("-----------COSTOS PIPE-------------");
        for (UploadedFile f : this.files.getFiles()) {
            if (f != null && !"".equals(f.getFileName())) {
                try ( BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(f.getInputStream(), "UTF-8"))) {
                    String actual = "";
                    String ipLinea = "";
                    while ((actual = bufferedReader.readLine()) != null) {

                        if (actual.toLowerCase().contains("command")
                                && actual.toLowerCase().contains(this.oBD.getTabla().getNombre().toLowerCase())) {

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
                            if (ValorAsignadoOperacion != 0 && c.compareTo("command") == 0
                                    && ns.compareTo(this.oBD.getTabla().getNombre().toLowerCase()) == 0) {
                                contador++;
                                System.out.println("OPERACIÓN: " + contador);
                                j2 = new JSONObject(j.get("attr").toString());
                                String remote = j2.get("remote").toString().toLowerCase().trim();
                                ipLinea = remote.substring(0, remote.indexOf(":"));

                                System.out.println("SITIO: " + ipLinea);
                                int nJoins = 1; //centralizada
                                System.out.println("CR: " + nJoins);
                                if (this.oBD.getDirbd().compareTo(ipLinea) != 0) {
                                    ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                                }
                                System.out.println("VAO: " + ValorAsignadoOperacion);

                                for (int o = 0; o < this.oBD.getTabla().getAtributos().size(); o++) {
                                    if (actual.toLowerCase().contains(this.oBD.getTabla().getAtributos().get(o).getNombre().toLowerCase())) {
                                        System.out.println("\tTamaño de atributo " + this.oBD.getTabla().getAtributos().get(o).getNombre() + ": " + Double.parseDouble(this.oBD.getTabla().getAtributos().get(o).getTamanio()));
                                        double costoOperacion = 0.0;
                                        if (this.oBD.getTabla().getAtributos().get(o).getCostoAtributoxSitio().containsKey(ipLinea)) {
                                            costoOperacion = this.oBD.getTabla().getAtributos().get(o).getCostoAtributoxSitio().get(ipLinea);
                                        }
                                        //System.out.println("PASA");
                                        double valorActual = ValorAsignadoOperacion * nJoins * Double.parseDouble(this.oBD.getTabla().getAtributos().get(o).getTamanio());
                                        System.out.println("Costo de operación: " + valorActual);
                                        costoTotal += valorActual;
                                        costoOperacion += valorActual;

                                        this.oBD.getTabla().getAtributos().get(o).getCostoAtributoxSitio().put(ipLinea, costoOperacion);
                                    }
                                }

                            } else {
                                System.out.println("algo pasa");
                            }
                        } else {
                            System.out.println("algo pasa");
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        //SITIOS Y COSTOS MULTIMEDIA
        for (TablaCosto tc : preEsquema) {
            if (tc.isMultimedia()) {
                Entry<String, Double> entryTemp = new AbstractMap.SimpleEntry<>(this.oBD.getDirbd(), 0.0);
                double costoFragmento = 0.0;
                for (int o = 0; o < tc.getArrAtri().size(); o++) {
                    for (String clave : tc.getArrAtri().get(o).getCostoAtributoxSitio().keySet()) {
                        costoFragmento += tc.getArrAtri().get(o).getCostoAtributoxSitio().get(clave);
                    }
                    try {
                        Entry<String, Double> entry = this.getMaxEntry(tc.getArrAtri().get(o).getCostoAtributoxSitio());
                        if (entry.getValue() > entryTemp.getValue()) {
                            entryTemp = entry;
                        }
                    } catch (Exception e) {
                        System.out.println("Atributo sin costo en log " + tc.getArrAtri().get(o));
                    }
                }
                tc.setCosto(costoFragmento);
                tc.setSitio(entryTemp.getKey());
            }
        }

        //FRAGMENTACIÓN, SITIOS Y COSTOS NO MULTIMEDIA
        ArrayList<TablaCosto> nuevosFragmentos = new ArrayList<>();
        for (TablaCosto tc : preEsquema) {
            if (!tc.isMultimedia()) {
                double costoFragmento = 0.0;
                for (int o = 0; o < tc.getArrAtri().size(); o++) {
                    if (tc.getArrAtri().get(o).getNombre().compareTo(this.oBD.getTabla().getAtributoLlave().getNombre()) != 0) {
                        try {
                            String sitio = this.getMaxEntry(tc.getArrAtri().get(o).getCostoAtributoxSitio()).getKey();
                            boolean existe = false;
                            for (TablaCosto nuevo : nuevosFragmentos) {
                                if (nuevo.getSitio().compareTo(sitio) == 0) {
                                    nuevo.getArrAtri().add(tc.getArrAtri().get(o));
                                    existe = true;
                                }
                            }
                            if (!existe) {
                                TablaCosto nuevo = new TablaCosto();
                                nuevo.setArrAtri(new ArrayList<>());
                                nuevo.getArrAtri().add(this.oBD.getTabla().getAtributoLlave());
                                nuevo.getArrAtri().add(tc.getArrAtri().get(o));
                                nuevo.setSitio(sitio);
                                nuevo.setMultimedia(false);
                                nuevosFragmentos.add(nuevo);
                            }
                        } catch (Exception e) {
                            System.out.println("Atributo sin costo en log " + tc.getArrAtri().get(o) + " asignación a este sitio");
                            boolean existe = false;
                            for (TablaCosto nuevo : nuevosFragmentos) {
                                if (nuevo.getSitio().compareTo(this.oBD.getDirbd()) == 0) {
                                    nuevo.getArrAtri().add(tc.getArrAtri().get(o));
                                    existe = true;
                                }
                            }
                            if (!existe) {
                                TablaCosto nuevo = new TablaCosto();
                                nuevo.setArrAtri(new ArrayList<>());
                                nuevo.getArrAtri().add(this.oBD.getTabla().getAtributoLlave());
                                nuevo.getArrAtri().add(tc.getArrAtri().get(o));
                                nuevo.setSitio(this.oBD.getDirbd());
                                nuevo.setMultimedia(false);
                                nuevosFragmentos.add(nuevo);
                            }
                        }
                    }
                }
            }
        }
        preEsquema.removeIf(x -> !x.isMultimedia());
        ret.addAll(preEsquema);
        ret.addAll(nuevosFragmentos);

        for (TablaCosto tc : ret) {
            if (!tc.isMultimedia()) {
                double costoFragmento = 0.0;
                for (int o = 0; o < tc.getArrAtri().size(); o++) {
                    for (String clave : tc.getArrAtri().get(o).getCostoAtributoxSitio().keySet()) {
                        costoFragmento += tc.getArrAtri().get(o).getCostoAtributoxSitio().get(clave);
                    }
                }
                tc.setCosto(costoFragmento);
            }
        }
        System.out.println("::::::::::COSTO ACUMULATIVO: " + costoTotal);
        return ret;
    }

    //PIPE
    public ArrayList<TablaCosto> costoxSitioMongoV3(ArrayList<TablaCosto> preEsquema) {
        ArrayList<TablaCosto> ret = new ArrayList<>();
        int contador = 0;
        double costoTotal = 0.0;
        System.out.println("-----------COSTOS PIPE-------------");
        for (UploadedFile f : this.files.getFiles()) {
            if (f != null && !"".equals(f.getFileName())) {
                try ( BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(f.getInputStream(), "UTF-8"))) {
                    String actual = "";
                    String ipLinea = "";
                    while ((actual = bufferedReader.readLine()) != null) {

                        if (actual.toLowerCase().contains("command")
                                && actual.toLowerCase().contains(this.oBD.getTabla().getNombre().toLowerCase())) {

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
                            if (ValorAsignadoOperacion != 0 && c.compareTo("command") == 0
                                    && ns.compareTo(this.oBD.getTabla().getNombre().toLowerCase()) == 0) {
                                contador++;
                                System.out.println("OPERACIÓN: " + contador);
                                j2 = new JSONObject(j.get("attr").toString());
                                String remote = j2.get("remote").toString().toLowerCase().trim();
                                ipLinea = remote.substring(0, remote.indexOf(":"));

                                System.out.println("SITIO: " + ipLinea);
                                int nJoins = 1; //centralizada
                                System.out.println("CR: " + nJoins);
                                if (this.oBD.getDirbd().compareTo(ipLinea) != 0) {
                                    ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                                }
                                System.out.println("VAO: " + ValorAsignadoOperacion);
                                ArrayList<Atributo> desordenados = new ArrayList<>();
                                double costoOperacionReal = 0.0;
                                for (int o = 0; o < this.oBD.getTabla().getAtributos().size(); o++) {
                                    if (actual.toLowerCase().contains(this.oBD.getTabla().getAtributos().get(o).getNombre().toLowerCase())) {
                                        desordenados.add(this.oBD.getTabla().getAtributos().get(o));
                                        System.out.println("\tTamaño de atributo " + this.oBD.getTabla().getAtributos().get(o).getNombre() + ": " + Double.parseDouble(this.oBD.getTabla().getAtributos().get(o).getTamanio()));
                                        double costoOperacion = 0.0;
                                        if (this.oBD.getTabla().getAtributos().get(o).getCostoAtributoxSitio().containsKey(ipLinea)) {
                                            costoOperacion = this.oBD.getTabla().getAtributos().get(o).getCostoAtributoxSitio().get(ipLinea);
                                        }
                                        //System.out.println("PASA");
                                        double valorActual = ValorAsignadoOperacion * nJoins * Double.parseDouble(this.oBD.getTabla().getAtributos().get(o).getTamanio());
                                        System.out.println("Costo de operación: " + valorActual);
                                        costoTotal += valorActual;
                                        costoOperacion += valorActual;
                                        costoOperacionReal += valorActual;
                                        this.oBD.getTabla().getAtributos().get(o).getCostoAtributoxSitio().put(ipLinea, costoOperacion);
                                    }
                                }
                                this.matrizAfinidad(desordenados, ipLinea, costoOperacionReal);

                            } else {
                                System.out.println("algo pasa");
                            }
                        } else {
                            System.out.println("algo pasa");
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        //AFINIDAD
        //1.- ELIMINAR CONJUNTOS MULTIMEDIA, TODO AQUELLO QUE TENGA ATRIBUTOS MULTIMEDIA
        //1.1.- Eliminar el conjunto con la llave primaria sola, si es que es existe
        
        //LOS COSTOS RELACIONADOS A LOS CONJUNTOS ELIMINADOS NO SON RELEVANTES, YA QUE NINGUN
        //COSTO DE LA MATRIZ DE AFINIDAD ES EL COSTO FINAL DEL FRAGMENTO, LOS COSTOS REALES
        //SE TOMAN DE getCostoAtributoxSitio DE CADA ATRIBUTO INCLUIDO EN EL CONJUNTO
        
        ArrayList<ArrayList<Atributo>> aEliminar = new ArrayList<>();
        
        for (ArrayList<Atributo> arr : this.atributosSitioCostos.keySet()) {
            boolean hayMultimedia = false;
            boolean esSoloPK = false;
            for (int i = 0; i < arr.size() && !hayMultimedia; i++) {
                if (arr.get(i).isMultimedia() || arr.get(i).isDescriptor()) {
                    hayMultimedia = true;
                }
                if(arr.get(i).isPk()){
                    esSoloPK=true;
                }
            }
            if (hayMultimedia || (esSoloPK && arr.size()==1)) {
                aEliminar.add(arr);
            }
        }
        for (ArrayList<Atributo> arr : aEliminar) {
            this.atributosSitioCostos.remove(arr);
        }
        
        
        //2.- SUMAR COSTOS DE CONJUNTOS EN CADA SITIO (UN COSTO POR CONJUNTO)
        HashMap<ArrayList<Atributo>, Double> atributosSuma = new HashMap<>();
        for (ArrayList<Atributo> arr : this.atributosSitioCostos.keySet()) {
            Double sum = 0.0;
            for (String sitios : this.atributosSitioCostos.get(arr).keySet()) {
                sum += this.atributosSitioCostos.get(arr).get(sitios);
            }
            atributosSuma.put(arr, sum);
        }

        //3.- ORDENAR CONJUNTOS POR COSTO
        ArrayList atributosSumaOrden = (ArrayList) atributosSuma.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toList());

        HashMap<ArrayList<Atributo>, Double> filtrados = new HashMap<>();
        //4.- SELECCIONAR Y ELIMINAR CONJUNTOS SUBSECUENTES QUE CONTENGAN ATRIBUTOS YA PRESENTES EN LAS SELECCIONES ANTERIORES
        //ATENDER LLAVE PRIMARIA (no tomarla en cuenta, sino, siempre habría un solo conjunto, porque todos los conjuntos
        //tienen llave primaria)
        for (Object o : atributosSumaOrden) {
            Entry e = (Entry) o;
            ArrayList<Atributo> llave = (ArrayList<Atributo>) e.getKey();
            Double valor = (Double) e.getValue();
            boolean yaLoContiene = false;
            for (int j = 0; j < llave.size() && !yaLoContiene; j++) {
                if (!llave.get(j).isPk()) {
                    for (ArrayList<Atributo> i : filtrados.keySet()) {
                        for (int u = 0; u < i.size() && !yaLoContiene; u++) {
                            if (llave.get(j).getNombre().compareTo(i.get(u).getNombre()) == 0) {
                                yaLoContiene = true;
                            }
                        }
                    }
                }
            }
            if (!yaLoContiene) {
                filtrados.put(llave, valor);
            }
        }

        //5.- AGREGAR ATRIBUTOS FALTANTES (ATRIBUTOS SIN CONJUNTOS NI COSTO) A SITIO ORIGINAL
        //no agregar atts multimedia
        ArrayList<Atributo> faltantes = new ArrayList<>();
        for (Atributo t : this.oBD.getTabla().getAtributos()) {
            boolean yaLoContiene = false;
            for (ArrayList<Atributo> l : filtrados.keySet()) {
                for (Atributo i : l) {
                    if (i.getNombre().compareTo(t.getNombre()) == 0) {
                        yaLoContiene = true;
                    }
                }
            }
            if (!yaLoContiene && !t.isMultimedia() && !t.isDescriptor()) {
                faltantes.add(t);
            }
        }
        if (!faltantes.isEmpty()) {
            faltantes.add(this.oBD.getTabla().getAtributoLlave());
            filtrados.put(faltantes, 0.0);
        }

        //Agregar a preesquema los nuevos fragmentos
        for (ArrayList<Atributo> listaAtt : filtrados.keySet()) {
            TablaCosto tc = new TablaCosto();
            tc.setArrAtri(listaAtt);
            tc.setCosto(filtrados.get(listaAtt));
            tc.setMultimedia(false);
            preEsquema.add(tc);
        }

        //SITIOS Y COSTOS MULTIMEDIA
        for (TablaCosto tc : preEsquema) {
            double costoFragmento = 0.0;
            for (int o = 0; o < tc.getArrAtri().size(); o++) {
                //buscar en OBD estos atributos y sumar su costo en cada sitio
                for (Atributo att : this.oBD.getTabla().getAtributos()) {
                    if (att.getNombre().compareTo(tc.getArrAtri().get(o).getNombre()) == 0) {
                        costoFragmento += att.getCostoAtributoxSitio().values().stream().mapToDouble(Double::doubleValue).sum();
                    }
                }
            }
            tc.setCosto(costoFragmento);
        }
        //1.- sumar por sitio el costo de cada atributo en el conjunto sacado de obd
        HashMap<ArrayList<Atributo>, HashMap<String, Double>> sumaXSitio = new HashMap<>();
        for (TablaCosto tc : preEsquema) {
            sumaXSitio.put(tc.getArrAtri(), new HashMap<>());
            for (int o = 0; o < tc.getArrAtri().size(); o++) {
                for (Atributo att : this.oBD.getTabla().getAtributos()) {
                    if (att.getNombre().compareTo(tc.getArrAtri().get(o).getNombre()) == 0) {
                        for (String sitio : att.getCostoAtributoxSitio().keySet()) {
                            if (sumaXSitio.get(tc.getArrAtri()).containsKey(sitio)) {
                                sumaXSitio.get(tc.getArrAtri()).put(sitio, sumaXSitio.get(tc.getArrAtri()).get(sitio) + att.getCostoAtributoxSitio().get(sitio));
                            } else {
                                sumaXSitio.get(tc.getArrAtri()).put(sitio, att.getCostoAtributoxSitio().get(sitio));
                            }
                        }
                    }
                }
            }
        }
        //2.- hacer un maxentry del nuevo hashmap y ese será el sitio a donde asignar
        for (TablaCosto tc : preEsquema) {
            Entry<String, Double> entry = this.getMaxEntry(sumaXSitio.get(tc.getArrAtri()));
            tc.setSitio(entry.getKey());
        }
        //3.- Agrupar fragmentos no multimedia que esten presentes en el mismo sitio
        
        ret.addAll(preEsquema);
        System.out.println("::::::::::COSTO ACUMULATIVO: " + costoTotal);
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
                        if (actual.contains("Connect\t") && !actual.contains("denied")) {
                            String y[] = actual.split("\t");
                            ipLinea = y[2].substring(y[2].indexOf("@") + 1, y[2].indexOf(" ", y[2].indexOf("@") + 1));
                            nombre = y[1].substring(0, y[1].indexOf("C")).trim();
                            if (ipLinea.toLowerCase().compareTo("localhost") == 0 || ipLinea.toLowerCase().compareTo("::1") == 0) {
                                ipLinea = "127.0.0.1";
                            }
                            System.out.println("Agregado: " + nombre + " " + ipLinea);
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
                            String y[] = actual.split("\t");
                            String sujeto = y[1].trim().split(" ")[0];
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
                                if (actual.toLowerCase().contains(a.getNombre().toLowerCase()) || actual.contains(" * ")) {
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
        for (int p = 0; p < costoAtributoxSitio.size(); p++) {
            for (String s : sitios) {
                if (costoAtributoxSitio.get(p).get(s) == null || costoAtributoxSitio.get(p).get(s) == Double.NaN) {
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
                            if (a.getNombre().compareTo(this.oBD.getTabla().getAtributoLlave().getNombre()) != 0) {
                                atts.add(a);
                            }
                        }
                    } catch (Exception e) {
                        //OBTUBIERON CERO EN COSTO, NO ESTAN EN LOG
                        e.printStackTrace();
                        if (!attsSinCosto.contains(a)) {
                            attsSinCosto.add(a);
                        }
                    }
                }
                tc.setCosto(costoFragmento);
                atts.add(this.oBD.getTabla().getAtributoLlave());
                tc.setArrAtri(atts);
                this.esquemaVertical.add(tc);
            }
        }
        if (attsSinCosto.size() > 0) {
            esquemaVertical.get(0).getArrAtri().addAll(attsSinCosto);
        }
        esquemaVertical = this.nombrar(esquemaVertical);
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
                                if (actual.toLowerCase().contains(a.getNombre().toLowerCase()) || actual.contains(" * ")) {
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
        for (int p = 0; p < costoAtributoxSitio.size(); p++) {
            for (String s : sitios) {
                if (costoAtributoxSitio.get(p).get(s) == null || costoAtributoxSitio.get(p).get(s) == Double.NaN) {
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
                            if (a.getNombre().compareTo(this.oBD.getTabla().getAtributoLlave().getNombre()) != 0) {
                                atts.add(a);
                            }
                        }
                    } catch (Exception e) {
                        //OBTUBIERON CERO EN COSTO, NO ESTAN EN LOG
                        e.printStackTrace();
                        if (!attsSinCosto.contains(a)) {
                            attsSinCosto.add(a);
                        }
                    }
                }
                tc.setCosto(costoFragmento);
                atts.add(this.oBD.getTabla().getAtributoLlave());
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
                                this.oBD.getNombreIp().put(ipLinea, ipLinea);
                                if (this.oBD.getDirbd().compareTo(ipLinea) != 0) {
                                    ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                                }
                                System.out.println("VAO: " + ValorAsignadoOperacion);
                                int nJoins = incidencias(actual.toLowerCase(), "$lookup");
                                System.out.println("CR: " + nJoins);
                                double costoOperacion = 0.0;
                                for (int o = 0; o < this.oBD.getTabla().getAtributos().size(); o++) {
                                    Atributo a = this.oBD.getTabla().getAtributos().get(o);
                                    System.out.println("Atributo: " + a.getNombre() + a.getTamanio() + " indice: " + o);
                                    if (actual.toLowerCase().contains(a.getNombre().toLowerCase()) || !actual.contains("projection")) {
                                        HashMap<String, Double> atributoSitios = new HashMap<>();
                                        if (costoAtributoxSitio.get(o) != null) {
                                            atributoSitios = costoAtributoxSitio.get(o);
                                        }
                                        double valor = 0.0;
                                        if (atributoSitios.containsKey(ipLinea)) {
                                            valor = atributoSitios.get(ipLinea);
                                        }
                                        valor += ValorAsignadoOperacion * nJoins * Double.parseDouble(a.getTamanio());
                                        System.out.println("Valor actual: " + valor);
                                        System.out.println("\tTamaño de atributo " + a.getNombre() + ": " + Double.parseDouble(a.getTamanio()));
                                        atributoSitios.put(ipLinea, valor);
                                        costoAtributoxSitio.set(o, atributoSitios);
                                        //Costo por sitio
                                        Double costoSitio = 0.0;
                                        if (costoxSitio.containsKey(ipLinea)) {
                                            costoSitio = costoxSitio.get(ipLinea);
                                        }
                                        costoSitio += valor;
                                        costoxSitio.put(ipLinea, costoSitio);
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
        this.sitios = deHashaArraySitios();
        this.esquemaVertical = new ArrayList<>();
        for (int p = 0; p < costoAtributoxSitio.size(); p++) {
            for (String s : sitios) {
                if (costoAtributoxSitio.get(p).get(s) == null || costoAtributoxSitio.get(p).get(s) == Double.NaN) {
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
                            if (a.getNombre().compareTo(this.oBD.getTabla().getAtributoLlave().getNombre()) != 0) {
                                atts.add(a);
                            }
                        }
                    } catch (Exception e) {
                        //OBTUBIERON CERO EN COSTO, NO ESTAN EN LOG
                        e.printStackTrace();
                        if (!attsSinCosto.contains(a)) {
                            attsSinCosto.add(a);
                        }
                    }
                }
                tc.setCosto(costoFragmento);
                atts.add(this.oBD.getTabla().getAtributoLlave());
                tc.setArrAtri(atts);
                this.esquemaVertical.add(tc);
            }
        }
        if (attsSinCosto.size() > 0) {
            esquemaVertical.get(0).getArrAtri().addAll(attsSinCosto);
        }
        esquemaVertical.removeIf(a -> a.getArrAtri().size() == 1 && a.getArrAtri().get(0).getNombre().compareTo("_id") == 0 && a.getCosto() == 0.0);
        esquemaVertical = this.nombrar(esquemaVertical);
        System.out.println("<<<<<COSTO DE ESQUEMA TOTAL: " + costoEsquemaVerticalMongoDB() + ">>>>>");
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
        for (Entry<String, Double> entry : map.entrySet()) {
            if (maxEntry == null) {
                maxEntry = entry;
            } else {
                Double value = entry.getValue();
                if (null != value && value > maxEntry.getValue()) {
                    maxEntry = entry;
                }
            }
        }

        return maxEntry;
    }

    public double costoEsquemaVerticalMongoDB() {
        this.oBD.setNombreIp(new HashMap<>());
        double costoEsquema = 0.0;
        int contador = 0;
        System.out.println("COSTO DE ESQUEMA UNA VEZ APLICADO");
        for (UploadedFile f : this.files.getFiles()) {
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
                            if (ValorAsignadoOperacion != 0
                                    && c.compareTo("command") == 0
                                    && ns.compareTo(this.oBD.getTabla().getNombre().toLowerCase()) == 0) {

                                j2 = new JSONObject(j.get("attr").toString());
                                String remote = j2.get("remote").toString().toLowerCase().trim();
                                ipLinea = remote.substring(0, remote.indexOf(":"));
                                //System.out.println("IP: "+ipLinea);
                                this.oBD.getNombreIp().put(ipLinea, ipLinea);
                                //calcular el número de sitios que necesita visitar para llevar
                                //a cabo la operación (será el número de reuniones necesarias)
                                int nJoins = 0;
                                ArrayList<String> atributosOpe = buscarAtributos(actual, this.oBD.getTabla().getAtributos());
                                ArrayList<TablaCosto> fResuelvenAtributos = new ArrayList<>();
                                for (TablaCosto tc : this.esquemaVertical) {
                                    boolean seUsa = false;
                                    for (Atributo l : tc.getArrAtri()) {
                                        if (actual.toLowerCase().contains(l.getNombre().toLowerCase()) || (!actual.contains("projection") && j3.has("find"))) {
                                            seUsa = true;
                                        }
                                    }
                                    if (seUsa) {
                                        fResuelvenAtributos.add(tc);
                                    }
                                }
                                nJoins = (fResuelvenAtributos.size() < 1) ? 1 : fResuelvenAtributos.size();

                                //System.out.println("nJoins: " + nJoins);                                
                                //buscar en esquema si hay algun atributo (por lo menos uno) remoto ,
                                //si lo hay entonces considerar operación remota
                                //de otro modo es local
                                boolean siHay = false;
                                for (TablaCosto tc : fResuelvenAtributos) {
                                    if (tc.getSitio().compareTo(ipLinea) != 0) {
                                        siHay = true;
                                    }
                                }
                                if (siHay) {
                                    ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                                }
                                //System.out.println("VAO: " + ValorAsignadoOperacion);
                                contador++;
                                System.out.println("OPE " + contador);
                                for (int o = 0; o < this.oBD.getTabla().getAtributos().size(); o++) {
                                    Atributo a = this.oBD.getTabla().getAtributos().get(o);

                                    if (actual.toLowerCase().contains(a.getNombre().toLowerCase()) || (!actual.contains("projection") && j3.has("find"))) {
                                        //System.out.println("Atributo "+this.oBD.getTabla().getAtributos().get(o).getNombre()+" : "+this.oBD.getTabla().getAtributos().get(o).getTamanio());
                                        costoEsquema += ValorAsignadoOperacion * nJoins * Double.parseDouble(a.getTamanio());
                                    }
                                }
                                //System.out.println("::::COSTO ACUMULADO: "+costoEsquema);
                            } else {
                                System.out.println("OPERACIÓN NO ANALIZADA (TAMAÑO DE MUESTRA)");
                            }
                        } else {
                            System.out.println("ALGO PASA");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return costoEsquema;
    }

    private ArrayList<String> buscarAtributos(String actual, ArrayList<Atributo> atts) {
        JSONObject j;
        ArrayList<String> ret = new ArrayList<>();
        //QUE ATRIBUTOS SON LOS DEL ESQUEMA?
        try {
            j = new JSONObject(actual);
            String c = j.get("c").toString().toLowerCase().trim();
            JSONObject j2 = new JSONObject(j.get("attr").toString());
            JSONObject j3 = new JSONObject(j2.get("command").toString());
            if (j3.has("find")) {
                if (j3.has("projection")) {
                    JSONObject j4 = new JSONObject(j3.get("projection").toString());
                    for (Atributo a : atts) {
                        if (j4.has(a.getNombre())) {
                            ret.add(a.getNombre().toLowerCase());
                        }
                    }
                } else {
                    for (Atributo a : atts) {
                        ret.add(a.getNombre().toLowerCase());
                    }
                }
            } else if (j3.has("delete")) {
                for (Atributo a : atts) {
                    ret.add(a.getNombre().toLowerCase());
                }
            } else if (j3.has("insert")) {
                //insert many
                if (j3.has("documents")) {
                    JSONArray arr = j3.getJSONArray("documents");
                    for (int i = 0; i < arr.length(); i++) {
                        for (Atributo a : atts) {
                            JSONObject j4 = new JSONObject(arr.get(i).toString());
                            if (j4.has(a.getNombre())) {
                                if (!ret.contains(a.getNombre())) {
                                    ret.add(a.getNombre());
                                }
                            }
                        }

                    }
                }
            } else if (j3.has("update")) {
                JSONArray arr = j3.getJSONArray("updates");
                JSONObject j4 = new JSONObject(arr.get(0).toString());
                JSONObject j5 = new JSONObject(j4.get("u").toString());
                JSONObject j6 = new JSONObject(j5.get("$set").toString());
                for (Atributo a : atts) {
                    if (j6.has(a.getNombre())) {
                        ret.add(a.getNombre().toLowerCase());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public void matrizAfinidad(ArrayList<Atributo> attOpe, String ip, double costoOperacion) {
        //Agregar llave primaria a attOpe si no la tiene
        boolean llavePrimaria = false;
        for (Atributo it : attOpe) {
            if (it.isPk()) {
                llavePrimaria = true;
            }
        }
        if (!llavePrimaria) {
            this.oBD.getTabla().getAtributoLlave().setPk(true);
            attOpe.add(this.oBD.getTabla().getAtributoLlave());
        }

        boolean itemEncontrado = false;
        Iterator<ArrayList<Atributo>> it = atributosSitioCostos.keySet().iterator();
        int control = 0;
        while (it.hasNext() && !itemEncontrado) {
            ArrayList<Atributo> clave = it.next();
            //buscar en listaAtributos este conjunto (attOpe)
            //buscar si hay algun atributo en a1 que no este en attOpe
            //que compartan la longitud
            //
            if (clave.size() == attOpe.size()) {
                boolean aunpuedenestarTodos = true;
                for (int j = 0; j < clave.size() && aunpuedenestarTodos; j++) {
                    boolean todosenAttOpe = false;
                    for (int i = 0; i < attOpe.size() && !todosenAttOpe; i++) {
                        if (clave.get(j).getNombre().compareTo(attOpe.get(i).getNombre()) == 0) {
                            todosenAttOpe = true;
                        }
                    }
                    if (!todosenAttOpe) {
                        System.out.println("No hay coincidencia en la linea " + control);
                        aunpuedenestarTodos = false;
                    }
                }
                if (aunpuedenestarTodos) {
                    System.out.println("Coincidencia en la linea " + control);
                    if (atributosSitioCostos.get(clave).containsKey(ip)) {
                        atributosSitioCostos.get(clave).put(ip, atributosSitioCostos.get(clave).get(ip) + costoOperacion);
                    } else {
                        atributosSitioCostos.get(clave).put(ip, costoOperacion);
                    }
                    itemEncontrado = true;
                }
            } else {
                System.out.println("No hay coincidencia en la linea " + control);
            }
            control++;
        }
        if (!itemEncontrado) {
            //agregar esta combinación nueva
            HashMap<String, Double> temp = new HashMap<>();
            temp.put(ip, costoOperacion);
            atributosSitioCostos.put(attOpe, temp);
            System.out.println("Agregada");
        }

    }

    /**
     * @return the atributosSitioCostos
     */
    public HashMap<ArrayList<Atributo>, HashMap<String, Double>> getAtributosSitioCostos() {
        return atributosSitioCostos;
    }

    /**
     * @param atributosSitioCostos the atributosSitioCostos to set
     */
    public void setAtributosSitioCostos(HashMap<ArrayList<Atributo>, HashMap<String, Double>> atributosSitioCostos) {
        this.atributosSitioCostos = atributosSitioCostos;
    }
}
