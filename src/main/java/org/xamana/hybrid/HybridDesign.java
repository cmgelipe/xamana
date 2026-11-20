/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.xamana.hybrid;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import org.xamana.datos.AccesoDatos;
import org.xamana.datos.AccesoDatosMG;
import org.xamana.datos.AccesoDatosPG;
import org.xamana.db.Atributo;
import org.xamana.db.Sitio;
import org.xamana.db.SitioDAO;
import org.xamana.db.bd;
import org.xamana.dhf.model.Fragment;
import org.xamana.dhf.schema.Schema;
import org.xamana.verticalCBIR.model.TablaCosto;

/**
 *
 * @author pipe
 */
public class HybridDesign implements Serializable {

    private double HVCost;
    private double VHCost;
    private ArrayList<HybridFragment> fragments;
    private int progress1;
    bd oBD;
    private final SitioDAO sitioDAO = new SitioDAO();

    public HybridDesign(bd oBD) {
        fragments = new ArrayList<>();
        this.oBD = oBD;
    }

    /**
     * Línea de conexión registrada de ese sitio (puerto, base de datos,
     * usuario, contraseña), en vez de reutilizar la del sitio local para
     * cualquier destino.
     */
    private Sitio conexionSitio(String direccion) throws Exception {
        Sitio sitio = sitioDAO.obtenerConexion(direccion);
        if (sitio == null) {
            throw new Exception("El sitio " + direccion + " no está registrado en Site registry; "
                    + "no se conoce su puerto/usuario/contraseña.");
        }
        return sitio;
    }

    public void mix(Schema schema, ArrayList<TablaCosto> esquemaVertical) {
        List<Fragment> hfs = schema.getSchema();
        for (Fragment hf : hfs) {
            for (int i = 0; i < esquemaVertical.size(); i++) {
                TablaCosto vf = esquemaVertical.get(i);
                HybridFragment newFragment = new HybridFragment();
                newFragment.setNombre(hf.getName() + "_" + i);
                newFragment.setPredicado(hf.getPredicate());
                newFragment.setAtts(vf.getArrAtri());
                newFragment.setSitioh(hf.getSite());
                newFragment.setSitiov(vf.getSitio());
                getFragments().add(newFragment);
            }
        }
    }

    public void mixMongo(List<Fragment> hfs, ArrayList<TablaCosto> esquemaVertical) {
        for (Fragment hf : hfs) {
            for (int i = 0; i < esquemaVertical.size(); i++) {
                TablaCosto vf = esquemaVertical.get(i);
                HybridFragment newFragment = new HybridFragment();
                newFragment.setNombre(hf.getName() + "_" + i);
                newFragment.setPredicado(hf.getPredicate());
                newFragment.setAtts(vf.getArrAtri());
                newFragment.setSitioh(hf.getSite());
                newFragment.setSitiov(vf.getSitio());
                getFragments().add(newFragment);
            }
        }
    }

    public void costsMySQL(UploadedFiles files) {
        System.out.println("-----------COSTOS PIPE HÍBRIDA-------------");
        HashMap<String, String> NombreIp = new HashMap<>();
        //limpiar
        this.setVHCost(0.0);
        this.setHVCost(0.0);
        for (HybridFragment temp : this.getFragments()) {
            temp.setCostoDesH(0.0);
            temp.setCostoDesV(0.0);
        }
        for (UploadedFile f : files.getFiles()) {
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
                        int ValorAsignadoOperacion = this.VAO(actual);
                        if (ValorAsignadoOperacion != 0 && actual.toLowerCase().contains(this.oBD.getTabla().getNombre().toLowerCase())) {
                            String y[] = actual.split("\t");
                            String sujeto = y[1].substring(0, y[1].indexOf(" "));
                            String ipTraduccion = NombreIp.get(sujeto);
                            System.out.println(actual);
                            System.out.println("SITIO: " + ipTraduccion);

                            //calcular el número de sitios que necesita visitar para llevar
                            //a cabo la operación (será el número de reuniones necesarias)
                            //primero sitiov y luego sitioh
                            ///BUSCAR QUÉ FRAGMENTOS OCUPAN EL PREDICADO DE LA OPERACIÓN 
                            int nJoins = 0;
                            ArrayList<String> atributosOpe = buscarAtributos(actual);
                            ArrayList<HybridFragment> fResuelvenAtributos = new ArrayList<>();
                            for (HybridFragment fragmento : this.getFragments()) {
                                boolean seUsa = false;
                                for (Atributo l : fragmento.getAtts()) {
                                    if (atributosOpe.contains(l.getNombre().toLowerCase())) {
                                        seUsa = true;
                                    }
                                }
                                if (seUsa) {
                                    fResuelvenAtributos.add(fragmento);
                                }
                            }
                            String where_statement = where_statement(actual);
                            double tuplasDelWhere = this.nTuplas(where_statement.toLowerCase().replace("where", ""));
                            ArrayList<HybridFragment> fResuelvenPredicados = new ArrayList<>();
                            //filtrado por predicado
                            if (tuplasDelWhere > 0) {
                                for (HybridFragment fragmento : fResuelvenAtributos) {
                                    double tuplasDeAmbosPredicados = 0;
                                    if (!where_statement.isEmpty()) {
                                        //fragmento sin predicado
                                        if (fragmento.getPredicado().toLowerCase().contains("predicate") || fragmento.getPredicado().toLowerCase().contains("predicado")) {
                                            String predicadoFragmento = "";
                                            for (HybridFragment hf2 : this.getFragments()) {
                                                if (!hf2.getPredicado().contains("predicate") && !hf2.getPredicado().contains("predicado")) {
                                                    predicadoFragmento += "!(" + hf2.getPredicado() + ") AND ";
                                                }
                                            }
                                            predicadoFragmento = predicadoFragmento.substring(0, predicadoFragmento.length() - 5);
                                            tuplasDeAmbosPredicados = this.nTuplas(where_statement.toLowerCase().replace("where", "") + " and " + predicadoFragmento);
                                        } else {
                                            tuplasDeAmbosPredicados = this.nTuplas(where_statement.toLowerCase().replace("where", "") + " and " + fragmento.getPredicado());
                                            System.out.println("Tuplas de " + where_statement.toLowerCase().replace("where", "") + " and " + fragmento.getPredicado() + ": " + tuplasDeAmbosPredicados);
                                        }
                                    } else {
                                        tuplasDeAmbosPredicados = 1;
                                    }

                                    if (tuplasDeAmbosPredicados > 0) {
                                        fResuelvenPredicados.add(fragmento);
                                    }
                                }
                            }
                            nJoins = (fResuelvenPredicados.size() < 1) ? 1 : fResuelvenPredicados.size();
                            //buscar en esquema si la operación se resuelve en parte o entera de manera remota 
                            //primero sitiov y luego sitioh
                            boolean siHay = false;
                            for (HybridFragment fragmento : fResuelvenPredicados) {
                                if (fragmento.getSitiov().compareTo(ipLinea) != 0) {
                                    siHay = true;
                                }
                            }
                            if (siHay) {
                                ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                            }
                            System.out.println("VAO: " + ValorAsignadoOperacion);
                            System.out.println("CR: " + nJoins);
                            double costoOperacionv = 0.0;
                            for (Atributo a : this.oBD.getTabla().getAtributos()) {
                                if (actual.toLowerCase().contains(a.getNombre().toLowerCase())) {
                                    System.out.println("\tTamaño de atributo " + a.getNombre() + ": " + Double.parseDouble(a.getTamanio()));
                                    costoOperacionv += ValorAsignadoOperacion * nJoins * Double.parseDouble(a.getTamanio());
                                }
                            }
                            System.out.println("Costo de operación vertical V: " + costoOperacionv);
                            this.setVHCost(this.getVHCost() + costoOperacionv);
                            //se recalcula pero ahora con sitioh para el HVcosto
                            ValorAsignadoOperacion = this.VAO(actual);
                            siHay = false;
                            for (HybridFragment fragmento : fResuelvenPredicados) {
                                if (fragmento.getSitioh().compareTo(ipLinea) != 0) {
                                    siHay = true;
                                }
                            }
                            if (siHay) {
                                ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                            }
                            System.out.println("VAO: " + ValorAsignadoOperacion);
                            double costoOperacionh = 0.0;
                            for (Atributo a : this.oBD.getTabla().getAtributos()) {
                                if (actual.toLowerCase().contains(a.getNombre().toLowerCase())) {
                                    System.out.println("\tTamaño de atributo " + a.getNombre() + ": " + Double.parseDouble(a.getTamanio()));
                                    costoOperacionh += ValorAsignadoOperacion * nJoins * Double.parseDouble(a.getTamanio());
                                }
                            }
                            System.out.println("Costo de operación vertical h: " + costoOperacionh);
                            this.setHVCost(this.getHVCost() + costoOperacionh);

                            //////////horizontal
                            String predicados = this.where_statement(actual).toLowerCase().replace("where", "");
                            double nTuplas = this.nTuplas(predicados);
                            System.out.println("Tuplas: " + nTuplas);
                            //¿qué sitios se requieren para satisfacer el predicado y atributos 
                            //de la operación? R: los mismos sitios de la vertical

                            //¿se requieren sitios remotos? R: de la misma manera que en la vertical
                            System.out.println("VAO para horizontal V: " + ValorAsignadoOperacion);
                            double costohorizontalh = 0.0;
                            if (this.oBD.isHybridCBIR()) {
                                costohorizontalh = ValorAsignadoOperacion; //VAO ya contiene si es remota o local
                            } else {
                                costohorizontalh = nTuplas * ValorAsignadoOperacion; //VAO ya contiene si es remota o local
                            }

                            ValorAsignadoOperacion = this.VAO(actual);
                            siHay = false;
                            for (HybridFragment fragmento : fResuelvenPredicados) {
                                if (fragmento.getSitiov().compareTo(ipLinea) != 0) {
                                    siHay = true;
                                }
                            }
                            if (siHay) {
                                ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                            }
                            double costohorizontalv = 0.0;
                            if (this.oBD.isHybridCBIR()) {
                                costohorizontalv = ValorAsignadoOperacion; //VAO ya contiene si es remota o local
                            } else {
                                costohorizontalv = nTuplas * ValorAsignadoOperacion; //VAO ya contiene si es remota o local
                            }

                            System.out.println("Costo remoto horizontal v: " + costohorizontalv);
                            System.out.println("Costo remoto horizontal h: " + costohorizontalh);
                            this.setVHCost(this.getVHCost() + costohorizontalv);
                            this.setHVCost(this.getHVCost() + costohorizontalh);

                            //Agregar el costo a los fragmentos que resuelven la operación
                            for (int i = 0; i < this.fragments.size(); i++) {
                                boolean b = false;
                                for (int j = 0; j < fResuelvenPredicados.size(); j++) {
                                    if (fResuelvenPredicados.get(j).getNombre().compareTo(fragments.get(i).getNombre()) == 0) {
                                        b = true;
                                    }
                                }
                                if (b) {
                                    fragments.get(i).setCostoDesV(fragments.get(i).getCostoDesV() + costoOperacionv + costohorizontalv);
                                    fragments.get(i).setCostoDesH(fragments.get(i).getCostoDesH() + costoOperacionh + costohorizontalh);
                                }
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void costsPostgreSQL(UploadedFiles files) {
        System.out.println("-----------COSTOS PIPE HÍBRIDA-------------");
        HashMap<String, String> NombreIp = new HashMap<>();
        //limpiar
        this.setVHCost(0.0);
        this.setHVCost(0.0);
        for (HybridFragment temp : this.getFragments()) {
            temp.setCostoDesH(0.0);
            temp.setCostoDesV(0.0);
        }
        for (UploadedFile f : files.getFiles()) {
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
                        int ValorAsignadoOperacion = this.VAO(actual);
                        if (ValorAsignadoOperacion != 0 && actual.toLowerCase().contains(this.oBD.getTabla().getNombre().toLowerCase())) {
                            String sujeto = actual.substring(actual.indexOf("[") + 1, actual.indexOf("]"));
                            String ipTraduccion = NombreIp.get(sujeto);
                            System.out.println(actual);
                            System.out.println("SITIO: " + ipTraduccion);
                            //calcular el número de sitios que necesita visitar para llevar
                            //a cabo la operación (será el número de reuniones necesarias)
                            //primero sitiov y luego sitioh

                            ///BUSCAR QUÉ FRAGMENTOS OCUPAN EL PREDICADO DE LA OPERACIÓN 
                            int nJoins = 0;
                            ArrayList<String> atributosOpe = buscarAtributos(actual);
                            ArrayList<HybridFragment> fResuelvenAtributos = new ArrayList<>();
                            for (HybridFragment fragmento : this.getFragments()) {
                                boolean seUsa = false;
                                for (Atributo l : fragmento.getAtts()) {
                                    if (atributosOpe.contains(l.getNombre().toLowerCase())) {
                                        seUsa = true;
                                    }
                                }
                                if (seUsa) {
                                    fResuelvenAtributos.add(fragmento);
                                }
                            }
                            String where_statement = where_statement(actual);
                            double tuplasDelWhere = this.nTuplas(where_statement.toLowerCase().replace("where", ""));
                            ArrayList<HybridFragment> fResuelvenPredicados = new ArrayList<>();
                            //filtrado por predicado
                            if (tuplasDelWhere > 0) {
                                for (HybridFragment fragmento : fResuelvenAtributos) {
                                    double tuplasDeAmbosPredicados = 0;
                                    if (!where_statement.isEmpty()) {
                                        //fragmento sin predicado
                                        if (fragmento.getPredicado().toLowerCase().contains("predicate") || fragmento.getPredicado().toLowerCase().contains("predicado")) {
                                            String predicadoFragmento = "";
                                            for (HybridFragment hf2 : this.getFragments()) {
                                                if (!hf2.getPredicado().contains("predicate") && !hf2.getPredicado().contains("predicado")) {
                                                    predicadoFragmento += "!(" + hf2.getPredicado() + ") AND ";
                                                }
                                            }
                                            predicadoFragmento = predicadoFragmento.substring(0, predicadoFragmento.length() - 5);
                                            tuplasDeAmbosPredicados = this.nTuplas(where_statement.toLowerCase().replace("where", "") + " and " + predicadoFragmento);
                                        } else {
                                            tuplasDeAmbosPredicados = this.nTuplas(where_statement.toLowerCase().replace("where", "") + " and " + fragmento.getPredicado());
                                            System.out.println("Tuplas de " + where_statement.toLowerCase().replace("where", "") + " and " + fragmento.getPredicado() + ": " + tuplasDeAmbosPredicados);
                                        }
                                    } else {
                                        tuplasDeAmbosPredicados = 1;
                                    }

                                    if (tuplasDeAmbosPredicados > 0) {
                                        fResuelvenPredicados.add(fragmento);
                                    }
                                }
                            }
                            nJoins = (fResuelvenPredicados.size() < 1) ? 1 : fResuelvenPredicados.size();
                            //buscar en esquema si la operación se resuelve en parte o entera de manera remota 
                            //primero sitiov y luego sitioh
                            boolean siHay = false;
                            for (HybridFragment fragmento : fResuelvenPredicados) {
                                if (fragmento.getSitiov().compareTo(ipLinea) != 0) {
                                    siHay = true;
                                }
                            }
                            if (siHay) {
                                ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                            }
                            System.out.println("VAO: " + ValorAsignadoOperacion);
                            System.out.println("CR: " + nJoins);
                            double costoOperacionv = 0.0;
                            for (Atributo a : this.oBD.getTabla().getAtributos()) {
                                if (actual.toLowerCase().contains(a.getNombre().toLowerCase())) {
                                    System.out.println("\tTamaño de atributo " + a.getNombre() + ": " + Double.parseDouble(a.getTamanio()));
                                    costoOperacionv += ValorAsignadoOperacion * nJoins * Double.parseDouble(a.getTamanio());
                                }
                            }
                            System.out.println("Costo de operación vertical V: " + costoOperacionv);
                            this.setVHCost(this.getVHCost() + costoOperacionv);
                            //se recalcula pero ahora con sitioh para el HVcosto
                            ValorAsignadoOperacion = this.VAO(actual);
                            siHay = false;
                            for (HybridFragment fragmento : fResuelvenPredicados) {
                                if (fragmento.getSitioh().compareTo(ipLinea) != 0) {
                                    siHay = true;
                                }
                            }
                            if (siHay) {
                                ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                            }
                            double costoOperacionh = 0.0;
                            for (Atributo a : this.oBD.getTabla().getAtributos()) {
                                if (actual.toLowerCase().contains(a.getNombre().toLowerCase())) {
                                    System.out.println("\tTamaño de atributo " + a.getNombre() + ": " + Double.parseDouble(a.getTamanio()));
                                    costoOperacionh += ValorAsignadoOperacion * nJoins * Double.parseDouble(a.getTamanio());
                                }
                            }
                            System.out.println("Costo de operación vertical h: " + costoOperacionh);
                            this.setHVCost(this.getHVCost() + costoOperacionh);

                            //////////horizontal
                            System.out.println("Tuplas: " + tuplasDelWhere);
                            //¿qué sitios se requieren para satisfacer el predicado y atributos 
                            //de la operación? R: los mismos sitios de la vertical

                            //¿se requieren sitios remotos? R: de la misma manera que en la vertical
                            double costohorizontalh = tuplasDelWhere * ValorAsignadoOperacion;
                            ValorAsignadoOperacion = this.VAO(actual);
                            siHay = false;
                            for (HybridFragment fragmento : fResuelvenPredicados) {
                                if (fragmento.getSitiov().compareTo(ipLinea) != 0) {
                                    siHay = true;
                                }
                            }
                            if (siHay) {
                                ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                            }
                            double costohorizontalv = tuplasDelWhere * ValorAsignadoOperacion;

                            System.out.println("Costo remoto horizontal v: " + costohorizontalv);
                            System.out.println("Costo remoto horizontal h: " + costohorizontalh);
                            this.setVHCost(this.getVHCost() + costohorizontalv);
                            this.setHVCost(this.getHVCost() + costohorizontalh);

                            //Agregar el costo a los fragmentos que resuelven la operación
                            for (int i = 0; i < this.fragments.size(); i++) {
                                boolean b = false;
                                for (int j = 0; j < fResuelvenPredicados.size(); j++) {
                                    if (fResuelvenPredicados.get(j).getNombre().compareTo(fragments.get(i).getNombre()) == 0) {
                                        b = true;
                                    }
                                }
                                if (b) {
                                    fragments.get(i).setCostoDesV(fragments.get(i).getCostoDesV() + costoOperacionv + costohorizontalv);
                                    fragments.get(i).setCostoDesH(fragments.get(i).getCostoDesH() + costoOperacionh + costohorizontalh);
                                }
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void costsMongoDB(UploadedFiles files) {
        System.out.println("-----------COSTOS PIPE HÍBRIDA-------------");
        HashMap<String, String> NombreIp = new HashMap<>();
        //limpiar
        this.setVHCost(0.0);
        this.setHVCost(0.0);

        for (HybridFragment temp : this.getFragments()) {
            temp.setCostoDesH(0.0);
            temp.setCostoDesV(0.0);
        }
        for (UploadedFile f : files.getFiles()) {
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
                                NombreIp.put(ipLinea, ipLinea);
                                //calcular el número de sitios que necesita visitar para llevar
                                //a cabo la operación (será el número de reuniones necesarias)
                                //primero sitiov y luego sitioh

                                ///BUSCAR QUÉ FRAGMENTOS OCUPAN EL PREDICADO DE LA OPERACIÓN 
                                int nJoins = 0;
                                ArrayList<String> atributosOpe = buscarAtributos(actual);
                                System.out.println("");
                                atributosOpe.forEach(x -> System.out.print(x));
                                ArrayList<HybridFragment> fResuelvenAtributos = new ArrayList<>();
                                for (HybridFragment fragmento : this.getFragments()) {
                                    boolean seUsa = false;
                                    for (Atributo l : fragmento.getAtts()) {
                                        if (atributosOpe.contains(l.getNombre().toLowerCase())) {
                                            seUsa = true;
                                        }
                                    }
                                    if (seUsa) {
                                        fResuelvenAtributos.add(fragmento);
                                    }
                                }
                                String where_statement = where_statement(actual);
                                double tuplasDelWhere = this.nTuplas(where_statement.toLowerCase());
                                ArrayList<HybridFragment> fResuelvenPredicados = new ArrayList<>();
                                //filtrado por predicado
                                if (tuplasDelWhere > 0) {
                                    for (HybridFragment fragmento : fResuelvenAtributos) {
                                        double tuplasDeAmbosPredicados = 0;
                                        if (!where_statement.isEmpty()) {
                                            //fragmento sin predicado
                                            if (fragmento.getPredicado().toLowerCase().contains("predicate") || fragmento.getPredicado().toLowerCase().contains("predicado")) {
                                                String predicadoFragmento = "";
                                                for (HybridFragment hf2 : this.getFragments()) {
                                                    if (!hf2.getNombre().contains("predicate") && !hf2.getNombre().contains("predicado")) {
                                                        String campo = hf2.getPredicado().substring(1, hf2.getPredicado().indexOf(":"));
                                                        String valor = hf2.getPredicado().substring(hf2.getPredicado().indexOf(":") + 1, hf2.getPredicado().length() - 1);
                                                        predicadoFragmento = "{" + campo + ":{$not:" + valor + "}}," + predicadoFragmento;
                                                    }
                                                }
                                                tuplasDeAmbosPredicados = this.nTuplas("{$and:[" + where_statement.toLowerCase() + "," + predicadoFragmento.substring(0, predicadoFragmento.length() - 1) + "]}");
                                                System.out.println("Tuplas de {$and:[" + where_statement.toLowerCase() + "," + predicadoFragmento.substring(0, predicadoFragmento.length() - 1) + "]}");
                                            } else {
                                                String campo = fragmento.getPredicado().substring(1, fragmento.getPredicado().indexOf(":"));
                                                String valor = fragmento.getPredicado().substring(fragmento.getPredicado().indexOf(":") + 1, fragmento.getPredicado().length() - 1);
                                                if (campo.contains("$and")) {
                                                    valor = valor.substring(0, valor.length() - 1) + "," + where_statement.toLowerCase() + "]";
                                                    tuplasDeAmbosPredicados = this.nTuplas("{" + campo + ":" + valor + "}");
                                                } else {
                                                    tuplasDeAmbosPredicados = this.nTuplas("{$and:[" + where_statement.toLowerCase() + ",{" + campo + ":" + valor + "}]}");
                                                }
                                                System.out.println("Tuplas de " + "{$and:[" + where_statement.toLowerCase() + ",{" + campo + ":" + valor + "}]}" + ": " + tuplasDeAmbosPredicados);
                                            }
                                        } else {
                                            tuplasDeAmbosPredicados = 1;
                                        }

                                        if (tuplasDeAmbosPredicados > 0) {
                                            fResuelvenPredicados.add(fragmento);
                                        }
                                    }
                                }
                                nJoins = (fResuelvenPredicados.size() < 1) ? 1 : fResuelvenPredicados.size();
                                //buscar en esquema si la operación se resuelve en parte o entera de manera remota 
                                //primero sitiov y luego sitioh
                                boolean siHay = false;
                                for (HybridFragment fragmento : fResuelvenPredicados) {
                                    if (fragmento.getSitiov().compareTo(ipLinea) != 0) {
                                        siHay = true;
                                    }
                                }
                                if (siHay) {
                                    ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                                }
                                System.out.println("VAO: " + ValorAsignadoOperacion);
                                System.out.println("CR: " + nJoins);
                                double costoOperacionv = 0.0;
                                for (Atributo a : this.oBD.getTabla().getAtributos()) {
                                    if (actual.toLowerCase().contains(a.getNombre().toLowerCase())) {
                                        System.out.println("\tTamaño de atributo " + a.getNombre() + ": " + Double.parseDouble(a.getTamanio()));
                                        costoOperacionv += ValorAsignadoOperacion * nJoins * Double.parseDouble(a.getTamanio());
                                    }
                                }
                                System.out.println("Costo de operación vertical V: " + costoOperacionv);
                                this.setVHCost(this.getVHCost() + costoOperacionv);
                                //se recalcula pero ahora con sitioh para el HVcosto
                                ValorAsignadoOperacion = this.VAO(j2.get("command").toString());
                                siHay = false;
                                for (HybridFragment fragmento : fResuelvenPredicados) {
                                    if (fragmento.getSitioh().compareTo(ipLinea) != 0) {
                                        siHay = true;
                                    }
                                }
                                if (siHay) {
                                    ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                                }
                                double costoOperacionh = 0.0;
                                for (Atributo a : this.oBD.getTabla().getAtributos()) {
                                    if (actual.toLowerCase().contains(a.getNombre().toLowerCase())) {
                                        System.out.println("\tTamaño de atributo " + a.getNombre() + ": " + Double.parseDouble(a.getTamanio()));
                                        costoOperacionh += ValorAsignadoOperacion * nJoins * Double.parseDouble(a.getTamanio());
                                    }
                                }
                                System.out.println("Costo de operación vertical h: " + costoOperacionh);
                                this.setHVCost(this.getHVCost() + costoOperacionh);
                                //////////horizontal
                                System.out.println("Tuplas: " + tuplasDelWhere);
                                //¿qué sitios se requieren para satisfacer el predicado y atributos 
                                //de la operación? R: los mismos sitios de la vertical

                                //¿se requieren sitios remotos? R: de la misma manera que en la vertical
                                double costohorizontalh = tuplasDelWhere * ValorAsignadoOperacion;
                                ValorAsignadoOperacion = this.VAO(j2.get("command").toString());
                                siHay = false;
                                for (HybridFragment fragmento : fResuelvenPredicados) {
                                    if (fragmento.getSitiov().compareTo(ipLinea) != 0) {
                                        siHay = true;
                                    }
                                }
                                if (siHay) {
                                    ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                                }
                                System.out.println("Valor asignado de operacion: " + ValorAsignadoOperacion);
                                double costohorizontalv = tuplasDelWhere * ValorAsignadoOperacion;

                                System.out.println("Costo remoto horizontal v: " + costohorizontalv);
                                System.out.println("Costo remoto horizontal h: " + costohorizontalh);
                                this.setVHCost(this.getVHCost() + costohorizontalv);
                                this.setHVCost(this.getHVCost() + costohorizontalh);

                                //Agregar el costo a los fragmentos que resuelven la operación
                                for (int i = 0; i < this.fragments.size(); i++) {
                                    boolean b = false;
                                    for (int r = 0; r < fResuelvenPredicados.size(); r++) {

                                        if (fResuelvenPredicados.get(r).getNombre().compareTo(fragments.get(i).getNombre()) == 0) {
                                            b = true;
                                        }
                                    }
                                    if (b) {
                                        System.out.println("El costo de la ope se asigno al frag " + fragments.get(i).getNombre());
                                        fragments.get(i).setCostoDesV(fragments.get(i).getCostoDesV() + costoOperacionv + costohorizontalv);
                                        fragments.get(i).setCostoDesH(fragments.get(i).getCostoDesH() + costoOperacionh + costohorizontalh);
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
    }

    private double nTuplas(String predicado) {
        double count1 = 0.0;
        if (this.oBD.getTipoBD().compareTo("a") == 0 || this.oBD.getTipoBD().compareTo("b") == 0 || this.oBD.getTipoBD().compareTo("c") == 0) {
            String q1 = "";
            if (!predicado.isEmpty()) {
                q1 = "select count(*) from " + this.oBD.getTabla().getNombre() + " where " + predicado;
            } else {
                q1 = "select count(*) from " + this.oBD.getTabla().getNombre();
            }

            ArrayList r1 = new ArrayList();

            try {
                if (this.oBD.getTipoBD().compareTo("a") == 0) {
                    if (this.oBD.getAccexternomysql().conectar()) {
                        r1 = this.oBD.getAccexternomysql().ejecutarConsulta(q1);

                        this.oBD.getAccexternomysql().desconectar();
                    }
                } else if (this.oBD.getTipoBD().compareTo("b") == 0 || this.oBD.getTipoBD().compareTo("c") == 0) {
                    if (this.oBD.getAccexternopost().conectar()) {
                        r1 = this.oBD.getAccexternopost().ejecutarConsulta(q1);
                        this.oBD.getAccexternopost().desconectar();
                    }
                }
                if (r1 != null) {
                    if (r1.size() > 0) {
                        count1 = (double) ((ArrayList) r1.get(0)).get(0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                //mongo
                if (this.oBD.getAccexternomongo().conectar()) {
                    MongoCollection<Document> doc = this.oBD.getAccexternomongo().getMongoClient().getDatabase(this.oBD.getNombd()).getCollection(this.oBD.getTabla().getNombre());
                    System.out.println(predicado);
                    Document d = Document.parse((predicado.isEmpty()) ? "{}" : predicado);
                    count1 = doc.countDocuments(d);
                    this.oBD.getAccexternomongo().getMongoClient().close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return count1;
    }

    private ArrayList<String> predicado(String q) {
        ArrayList<String> ret = new ArrayList<>();
        if (this.oBD.getTipoBD().compareTo("a") == 0 || this.oBD.getTipoBD().compareTo("b") == 0 || this.oBD.getTipoBD().compareTo("c") == 0) {
            if (q.toUpperCase().contains("INSERT ") && !q.toUpperCase().contains(" SET ")) {
                boolean existeOrden = false;
                for (int i = 0; i < this.oBD.getTabla().getAtributos().size(); i++) {
                    if (q.indexOf(this.oBD.getTabla().getAtributos().get(i).getNombre()) != -1 && q.indexOf(this.oBD.getTabla().getAtributos().get(i).getNombre()) < q.toUpperCase().indexOf(" VALUES")) {
                        existeOrden = true;
                    }
                }
                String valores1 = q.substring(q.indexOf("(", q.toUpperCase().indexOf(" VALUES") + 1) + 1, q.indexOf(")", q.toUpperCase().indexOf(" VALUES") + 1));
                valores1.replace("(", "");
                valores1.replace(")", "");
                String[] valores2 = valores1.split(",");
                if (existeOrden) {
                    String col = q.substring(q.indexOf("(") + 1, q.indexOf(")"));
                    col.replace("(", "");
                    col.replace(")", "");
                    String[] col1 = col.split(",");
                    if (col1.length == valores2.length) {
                        for (int t = 0; t < col1.length; t++) {
                            ret.add(col1[t] + "=" + valores2[t].trim());
                        }
                    }
                } else {
                    if (valores2.length == this.oBD.getTabla().getAtributos().size()) {
                        for (int t = 0; t < valores2.length; t++) {
                            ret.add(this.oBD.getTabla().getAtributos().get(t).getNombre() + "=" + valores2[t].trim());
                        }
                    }
                }
            } else {
                StringTokenizer tokens = new StringTokenizer(q, " ");
                while (tokens.hasMoreTokens()) {
                    String token = tokens.nextToken();
                    if (token.contains("!=")) {
                        StringTokenizer valores = new StringTokenizer(token, "!=");
                        if (unaColumna(valores.nextToken(), valores.nextToken())) {
                            ret.add(token);
                        }
                    } else if (token.contains("<=")) {
                        StringTokenizer valores = new StringTokenizer(token, "<=");
                        if (unaColumna(valores.nextToken(), valores.nextToken())) {
                            ret.add(token);
                        }
                    } else if (token.contains(">=")) {
                        StringTokenizer valores = new StringTokenizer(token, ">=");
                        if (unaColumna(valores.nextToken(), valores.nextToken())) {
                            ret.add(token);
                        }
                    } else if (token.contains("<")) {
                        StringTokenizer valores = new StringTokenizer(token, "<");
                        if (unaColumna(valores.nextToken(), valores.nextToken())) {
                            ret.add(token);
                        }
                    } else if (token.contains(">")) {
                        StringTokenizer valores = new StringTokenizer(token, ">");
                        if (unaColumna(valores.nextToken(), valores.nextToken())) {
                            ret.add(token);
                        }
                    } else if (token.contains("=")) {
                        StringTokenizer valores = new StringTokenizer(token, "=");
                        if (unaColumna(valores.nextToken(), valores.nextToken())) {
                            ret.add(token);
                        }
                    }
                }
            }
        }
        return ret;
    }

    private boolean unaColumna(String izq, String der) {
        boolean ret = true;

        for (Atributo att : this.oBD.getTabla().getAtributos()) {
            if ((izq.contains("`" + att.getNombre() + "`") || izq.contains("." + att.getNombre()) || izq.contains(" " + att.getNombre()))
                    && (der.contains("`" + att.getNombre() + "`") || der.contains("." + att.getNombre()) || der.contains(att.getNombre() + " "))) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * @return the HVCost
     */
    public double getHVCost() {
        return HVCost;
    }

    /**
     * @param HVCost the HVCost to set
     */
    public void setHVCost(double HVCost) {
        this.HVCost = HVCost;
    }

    /**
     * @return the VHCost
     */
    public double getVHCost() {
        return VHCost;
    }

    /**
     * @param VHCost the VHCost to set
     */
    public void setVHCost(double VHCost) {
        this.VHCost = VHCost;
    }

    /**
     * @return the fragments
     */
    public ArrayList<HybridFragment> getFragments() {
        return fragments;
    }

    /**
     * @param fragments the fragments to set
     */
    public void setFragments(ArrayList<HybridFragment> fragments) {
        this.fragments = fragments;
    }

    private ArrayList<String> buscarAtributos(String actual) {
        ArrayList<String> ret = new ArrayList<>();
        if (this.oBD.getTipoBD().compareTo("a") == 0 || this.oBD.getTipoBD().compareTo("b") == 0 || this.oBD.getTipoBD().compareTo("c") == 0) {
            if (actual.toLowerCase().contains("select")) {
                if (actual.toLowerCase().contains("from")) {
                    if (actual.toLowerCase().contains("*")) {
                        for (Atributo a : this.oBD.getTabla().getAtributos()) {
                            ret.add(a.getNombre().toLowerCase());
                        }
                    } else {
                        for (Atributo a : this.oBD.getTabla().getAtributos()) {
                            if (actual.toLowerCase().indexOf(a.getNombre().toLowerCase()) < actual.toLowerCase().indexOf("from")
                                    && actual.toLowerCase().contains(a.getNombre().toLowerCase())) {
                                ret.add(a.getNombre().toLowerCase());
                            }
                        }
                    }
                }
            } else if (actual.toLowerCase().contains("delete") || actual.toLowerCase().contains("insert")) {
                for (Atributo a : this.oBD.getTabla().getAtributos()) {
                    ret.add(a.getNombre().toLowerCase());
                }
            } else if (actual.toLowerCase().contains("update")) {
                if (actual.toLowerCase().contains("set")) {
                    if (actual.toLowerCase().contains("where")) {
                        for (Atributo a : this.oBD.getTabla().getAtributos()) {
                            if (actual.toLowerCase().indexOf(a.getNombre().toLowerCase(), actual.toLowerCase().indexOf("set")) < actual.toLowerCase().indexOf("where")
                                    && actual.toLowerCase().contains(a.getNombre().toLowerCase())) {
                                ret.add(a.getNombre().toLowerCase());
                            }
                        }
                    } else {
                        for (Atributo a : this.oBD.getTabla().getAtributos()) {
                            if (actual.toLowerCase().indexOf(a.getNombre().toLowerCase(), actual.toLowerCase().indexOf("set")) > -1
                                    && actual.toLowerCase().contains(a.getNombre().toLowerCase())) {
                                ret.add(a.getNombre().toLowerCase());
                            }
                        }
                    }
                }
            }
        } else {
            //mongo
            JSONObject j;
            try {
                j = new JSONObject(actual);
                String c = j.get("c").toString().toLowerCase().trim();
                JSONObject j2 = new JSONObject(j.get("attr").toString());
                JSONObject j3 = new JSONObject(j2.get("command").toString());
                if (j3.has("find")) {
                    if (j3.has("projection")) {
                        JSONObject j4 = new JSONObject(j3.get("projection").toString());
                        for (Atributo a : this.oBD.getTabla().getAtributos()) {
                            if (j4.has(a.getNombre())) {
                                ret.add(a.getNombre().toLowerCase());
                            }
                        }
                    } else {
                        for (Atributo a : this.oBD.getTabla().getAtributos()) {
                            ret.add(a.getNombre().toLowerCase());
                        }
                    }
                } else if (j3.has("delete")) {
                    for (Atributo a : this.oBD.getTabla().getAtributos()) {
                        ret.add(a.getNombre().toLowerCase());
                    }
                } else if (j3.has("insert")) {
                    //insert many
                    if (j3.has("documents")) {
                        JSONArray arr = j3.getJSONArray("documents");
                        for (int i = 0; i < arr.length(); i++) {
                            for (Atributo a : this.oBD.getTabla().getAtributos()) {
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
                    for (Atributo a : this.oBD.getTabla().getAtributos()) {
                        if (j6.has(a.getNombre())) {
                            ret.add(a.getNombre().toLowerCase());
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        return ret;
    }

    private String where_statement(String actual) {
        String ret = "";
        if (this.oBD.getTipoBD().compareTo("a") == 0 || this.oBD.getTipoBD().compareTo("b") == 0 || this.oBD.getTipoBD().compareTo("c") == 0) {
            if (actual.toLowerCase().contains("select")) {
                if (actual.toLowerCase().contains("where")) {
                    String patron = (this.oBD.getTipoBD().compareTo("a") == 0) ? "GROUP BY|HAVING|WINDOW|ORDER BY|LIMIT|FOR|INTO OUTFILE|INTO DUMPFILE|INTO" : "GROUP BY|HAVING|WINDOW|UNION|INTERSECT|EXCEPT|ORDER BY|LIMIT|OFFSET|FETCH|FOR";
                    Pattern pattern = Pattern.compile(patron);
                    Matcher matcher = pattern.matcher(actual.toUpperCase());
                    matcher.find();
                    int indexMatch = actual.length();
                    try {
                        indexMatch = matcher.start();
                    } catch (Exception e) {

                    }
                    ret = actual.substring(actual.toLowerCase().indexOf("where"), indexMatch);
                }
            } else if (actual.toLowerCase().contains("delete") || actual.toLowerCase().contains("update")) {
                if (actual.toLowerCase().contains("where")) {
                    String patron = (this.oBD.getTipoBD().compareTo("a") == 0) ? "ORDER BY|LIMIT" : "RETURNING";
                    Pattern pattern = Pattern.compile(patron);
                    Matcher matcher = pattern.matcher(actual.toUpperCase());
                    matcher.find();
                    int indexMatch = actual.length();
                    try {
                        indexMatch = matcher.start();
                    } catch (Exception e) {

                    }
                    ret = actual.substring(actual.toLowerCase().indexOf("where"), indexMatch);
                }
            } else if (actual.toLowerCase().contains("insert")) {
                for (String p : this.predicado(actual)) {
                    ret += p + " and ";
                }
                ret = (ret.length() > 4) ? "where " + ret.substring(0, ret.length() - 5) : ret;
            }
        } else {
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
        }
        return ret;
    }

    /**
     * @return the progress1
     */
    public int getProgress1() {
        return progress1;
    }

    /**
     * @param progress1 the progress1 to set
     */
    public void setProgress1(int progress1) {
        this.progress1 = progress1;
    }

    public void hybridFragmentation() {
        //1 vertical 2para horizontal
        int opc = (this.getHVCost() > this.getVHCost()) ? 1 : 2;

        this.oBD.guardarDatos();
        if (this.oBD.getTipoBD().compareTo("a") == 0) {
            hybridFragmentationMySQL(opc);
        } else if (this.oBD.getTipoBD().compareTo("b") == 0 || this.oBD.getTipoBD().compareTo("c") == 0) {
            hybridFragmentationPostgreSQL(opc);
        } else {
            hybridFragmentationMongoDB(opc);
        }
    }

    private void hybridFragmentationMySQL(int opc) {
        //creación de tablas
        int percent = 100 / this.fragments.size();
        double cDesempenioTablaOriginal = (opc == 1) ? this.VHCost : this.HVCost;
        for (HybridFragment frag : this.fragments) {
            String sitio = (opc == 1) ? frag.getSitiov() : frag.getSitioh();
            double costo = (opc == 1) ? frag.getCostoDesV() : frag.getCostoDesH();
            String sql;
            String att_tipo = "";
            for (Atributo r : frag.getAtts()) {
                if (this.oBD.transTipoDato(r.getTipo()) != 0) {
                    att_tipo += r.getNombre() + " " + r.getTipo() + ", ";
                } else {
                    if (r.getTipo().compareTo("BIT") != 0) {
                        if (r.getTipo().compareTo("JSON") == 0 || r.getTipo().compareTo("LONGBLOB") == 0) {
                            att_tipo += r.getNombre() + " " + r.getTipo() + ", ";
                        } else {
                            att_tipo += r.getNombre() + " " + r.getTipo() + "(" + r.getTamanio() + "), ";
                        }
                    } else {
                        att_tipo += r.getNombre() + " TINYINT, ";
                    }
                }
            }
            att_tipo = att_tipo.substring(0, att_tipo.length() - 2);
            try {
                Sitio conexionDestino = conexionSitio(sitio);
                Sitio conexionOrigen = conexionSitio(this.oBD.getDirbd());
                AccesoDatos acc = new AccesoDatos(sitio + ":" + conexionDestino.getPuerto(), conexionDestino.getBasedatos(), conexionDestino.getUsuario(), conexionDestino.getPassword());
                if (acc.conectar()) {
                    sql = "drop table if exists " + frag.getNombre() + ";";
                    acc.ejecutarComando(sql);
                    sql = "CREATE TABLE " + frag.getNombre() + " (" + att_tipo + ", PRIMARY KEY (" + this.oBD.getTabla().getAtributoLlave().getNombre() + "));";
                    acc.ejecutarComando(sql);
                    acc.desconectar();
                }
                ArrayList atributos = null;
                ArrayList n = null;
                AccesoDatos acc2 = new AccesoDatos(this.oBD.getDirbd() + ":" + conexionOrigen.getPuerto(), conexionOrigen.getBasedatos(), conexionOrigen.getUsuario(), conexionOrigen.getPassword());
                if (acc2.conectar()) {
                    String numero = "Select count(*) from " + this.oBD.getTabla().getNombre() + " where " + frag.getPredicado() + ";";
                    n = acc2.ejecutarConsulta(numero);
                    acc2.desconectar();
                }
                int num = ((Double) Double.parseDouble("" + ((ArrayList) n.get(0)).get(0))).intValue();
                for (int i = 0; i < num; i++) {
                    acc2 = new AccesoDatos(this.oBD.getDirbd() + ":" + conexionOrigen.getPuerto(), conexionOrigen.getBasedatos(), conexionOrigen.getUsuario(), conexionOrigen.getPassword());
                    if (acc2.conectar()) {
                        sql = "Select " + frag.getAtributosMostrar() + " from " + this.oBD.getTabla().getNombre() + " where " + frag.getPredicado() + " Limit 1 offset " + i;
                        atributos = acc2.ejecutarConsulta(sql);
                        acc2.desconectar();
                    }
                    if (atributos != null) {
                        acc = new AccesoDatos(sitio + ":" + conexionDestino.getPuerto(), conexionDestino.getBasedatos(), conexionDestino.getUsuario(), conexionDestino.getPassword());
                        if (acc.conectar()) {
                            for (int k = 0; k < atributos.size(); k++) {
                                String valor = "";
                                ArrayList tupla = (ArrayList) atributos.get(k);
                                for (int j = 0; j < tupla.size(); j++) {
                                    if (this.oBD.transTipoDato(frag.getAtts().get(j).getTipo()) == 0 || this.oBD.transTipoDato(frag.getAtts().get(j).getTipo()) == 2) {
                                        valor += "\"" + tupla.get(j) + "\",";
                                    } else {
                                        valor += tupla.get(j) + ",";
                                    }
                                }
                                valor = valor.substring(0, valor.length() - 1);
                                sql = "insert into " + frag.getNombre() + "(" + frag.getAtributosMostrar() + ") values (" + valor + ");";
                                acc.ejecutarComando(sql);

                            }
                            acc.desconectar();
                        }
                    }
                }

                //Insertar a XAMANA
                //SITIO
                String id_sitio = "";
                String idFragmento = "";

                AccesoDatos accXamana = new AccesoDatos();
                if (accXamana.conectar()) {
                    sql = "insert into Sitio (direccion) "
                            + "select * from (select '" + sitio + "' as direccion ) as tmp "
                            + "where not exists (select direccion from Sitio where direccion='" + sitio + "') limit 1;";
                    accXamana.ejecutarComando(sql);
                    sql = "select id_sitio from Sitio where direccion='" + sitio + "'";
                    id_sitio = "" + ((ArrayList) accXamana.ejecutarConsulta(sql).get(0)).get(0);
                    //FRAGMENTO
                    if (frag.isMultimedia()) {
                        accXamana.ejecutarComando("insert into Fragmento(nombre, id_tabla, id_sitio, noperaciones, ndesempenio, isCBIR) value ('"
                                + frag.getNombre() + "', " + this.oBD.getTabla().getIdTabla()
                                + ", " + id_sitio + ",0," + costo + ",1)");
                        idFragmento = "" + ((ArrayList) accXamana.ejecutarConsulta("Select LAST_INSERT_ID()").get(0)).get(0);
                    } else {
                        accXamana.ejecutarComando("insert into Fragmento(nombre, id_tabla, id_sitio, noperaciones, ndesempenio, isCBIR) value ('"
                                + frag.getNombre() + "', " + this.oBD.getTabla().getIdTabla()
                                + ", " + id_sitio + ",0," + costo + ",0)");
                        idFragmento = "" + ((ArrayList) accXamana.ejecutarConsulta("Select LAST_INSERT_ID()").get(0)).get(0);
                    }
                    if (!this.oBD.isNullorEmpty(idFragmento)) {
                        for (Atributo a : frag.getAtts()) {
                            accXamana.ejecutarComando("insert into Atributo (tipo,size,nom,id_fragmento,descriptor,multimedia,pk,id_tabla,descriptor_Visual) "
                                    + "values ('" + a.getTipo() + "', " + a.getTamanio() + ", '" + a.getNombre() + "', " + idFragmento + ", "
                                    + (a.isDescriptor() ? 1 : 0) + ", " + (a.isMultimedia() ? 1 : 0) + ", "
                                    + (a.getNombre().compareTo(this.oBD.getTabla().getAtributoLlave().getNombre()) == 0 ? 1 : 0) + ", null,"
                                    + (a.getNombre().compareTo(this.oBD.getTabla().getAtributoMultimedia().split(":")[0]) == 0 ? 1 : 0) + ")");
                        }

                    }
                    //Atributo pk de tabla original
                    accXamana.ejecutarComando("insert into Atributo (tipo,size,nom,id_fragmento,descriptor,multimedia,pk,id_tabla) "
                            + "values ('" + this.oBD.getTabla().getAtributoLlave().getTipo() + "', " + this.oBD.getTabla().getAtributoLlave().getTamanio() + ", '" + this.oBD.getTabla().getAtributoLlave().getNombre() + "', null, 0, 0, 1, "
                            + this.oBD.getTabla().getIdTabla() + ")");
                    //Desempenio de tabla original
                    accXamana.ejecutarComando("update Tabla set ndesempenio=" + cDesempenioTablaOriginal + " where id_tabla=" + this.oBD.getTabla().getIdTabla());
                    accXamana.desconectar();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.progress1 += percent;

        }
        this.progress1 = 100;
    }

    public void hybridOverlapRelational(int opc) {
        ArrayList<String> predicados = new ArrayList<>();
        for (HybridFragment f : this.fragments) {
            if (!predicados.contains(f.getPredicado())) {
                predicados.add(f.getPredicado());
            }
        }
        ArrayList<PredicadoCosto> predicadoCosto = new ArrayList<>();
        String predicadoFragmento = "";
        for (String item : predicados) {
            if (item.contains("predicate") || item.contains("predicado")) {
                predicadoFragmento = "";
                for (HybridFragment hf2 : this.getFragments()) {
                    if (!hf2.getPredicado().contains("predicate") && !hf2.getPredicado().contains("predicado")) {
                        if (this.oBD.getTipoBD().compareTo("a") == 0) {
                            predicadoFragmento += "!(" + hf2.getPredicado() + ") AND ";
                        } else if (this.oBD.getTipoBD().compareTo("b") == 0 || this.oBD.getTipoBD().compareTo("c") == 0) {
                            predicadoFragmento += "not (" + hf2.getPredicado() + ") AND ";
                        }

                    }
                }
                predicadoFragmento = predicadoFragmento.substring(0, predicadoFragmento.length() - 5);
            }
            int ocurrencias = 0;
            double costo = 0;
            for (HybridFragment f : this.fragments) {
                if (f.getPredicado().compareTo(item) == 0) {
                    ocurrencias++;
                    costo = (opc == 1) ? costo + f.getCostoDesV() : costo + f.getCostoDesH();
                }
            }
            PredicadoCosto p = new PredicadoCosto();
            p.setPredicado(item);
            p.setCosto(costo / ocurrencias);
            predicadoCosto.add(p);
        }
        Collections.sort(predicadoCosto, (PredicadoCosto a, PredicadoCosto b) -> new Double(b.getCosto()).compareTo(new Double(a.getCosto())));
        ArrayList<HybridFragment> ordenados = new ArrayList<>();
        for (PredicadoCosto item : predicadoCosto) {
            for (HybridFragment hf : this.getFragments()) {
                if (hf.getPredicado().compareTo(item.getPredicado()) == 0) {
                    ordenados.add(hf);
                }
            }
        }
        this.setFragments(ordenados);
        String negado = "";
        for (PredicadoCosto item : predicadoCosto) {
            //item=(item.getPredicado().toLowerCase().contains("predicate") || item.getPredicado().toLowerCase().contains("predicado"))?
            //      predicadoFragmento:item;    
            if (!item.getPredicado().toLowerCase().contains("predicate") && !item.getPredicado().toLowerCase().contains("predicado")) {
                if (negado.isEmpty()) {
                    item.setNegado(item.getPredicado());
                } else {
                    item.setNegado(negado + item.getPredicado());
                }
                if (!negado.contains(item.getPredicado())) {
                    if (this.oBD.getTipoBD().compareTo("a") == 0) {
                        negado += "!(" + item.getPredicado() + ") and ";
                    } else if (this.oBD.getTipoBD().compareTo("b") == 0 || this.oBD.getTipoBD().compareTo("c") == 0) {
                        negado += "not (" + item.getPredicado() + ") and ";
                    }

                }
            } else {
                item.setNegado(predicadoFragmento);
            }
        }
        for (PredicadoCosto item : predicadoCosto) {
            for (HybridFragment hf : this.getFragments()) {
                if (hf.getPredicado().compareTo(item.getPredicado()) == 0) {
                    hf.setPredicado(item.getNegado());
                }
            }
        }
        //eliminar ands
        for (HybridFragment hf : this.getFragments()) {
            if (hf.getPredicado().length() >= 5 && hf.getPredicado().substring(hf.getPredicado().length() - 5, hf.getPredicado().length()).toLowerCase().contains("and")) {
                hf.setPredicado(hf.getPredicado().substring(0, hf.getPredicado().length() - 5).toLowerCase());
            }
        }
        this.getFragments().forEach(x -> System.out.println(x.getPredicado()));
    }

    private void hybridFragmentationPostgreSQL(int opc) {
        //creación de tablas
        int percent = 100 / this.fragments.size();
        double cDesempenioTablaOriginal = (opc == 1) ? this.VHCost : this.HVCost;
        for (HybridFragment frag : this.fragments) {
            String sitio = (opc == 1) ? frag.getSitiov() : frag.getSitioh();
            double costo = (opc == 1) ? frag.getCostoDesV() : frag.getCostoDesH();
            String sql;
            String att_tipo = "";
            for (Atributo r : frag.getAtts()) {
                if (this.oBD.transTipoDato(r.getTipo()) != 0) {
                    att_tipo += r.getNombre() + " " + r.getTipo() + ", ";
                } else {
                    if (r.getTipo().compareTo("BIT") != 0) {
                        if (r.getTipo().compareTo("JSON") == 0 || r.getTipo().compareTo("LONGBLOB") == 0) {
                            att_tipo += r.getNombre() + " " + r.getTipo() + ", ";
                        } else {
                            att_tipo += r.getNombre() + " " + r.getTipo() + "(" + r.getTamanio() + "), ";
                        }
                    } else {
                        att_tipo += r.getNombre() + " TINYINT, ";
                    }
                }
            }
            att_tipo = att_tipo.substring(0, att_tipo.length() - 2);
            try {
                Sitio conexionDestino = conexionSitio(sitio);
                Sitio conexionOrigen = conexionSitio(this.oBD.getDirbd());
                AccesoDatosPG acc = new AccesoDatosPG(sitio + ":" + conexionDestino.getPuerto(), conexionDestino.getBasedatos(), conexionDestino.getUsuario(), conexionDestino.getPassword());
                if (acc.conectar()) {
                    sql = "drop table if exists " + frag.getNombre() + ";";
                    acc.ejecutarComando(sql);
                    sql = "CREATE TABLE " + frag.getNombre() + " (" + att_tipo + ", PRIMARY KEY (" + this.oBD.getTabla().getAtributoLlave().getNombre() + "));";
                    acc.ejecutarComando(sql);
                    acc.desconectar();
                }
                ArrayList atributos = null;
                ArrayList n = null;
                AccesoDatosPG acc2 = new AccesoDatosPG(this.oBD.getDirbd() + ":" + conexionOrigen.getPuerto(), conexionOrigen.getBasedatos(), conexionOrigen.getUsuario(), conexionOrigen.getPassword());
                if (acc2.conectar()) {
                    String numero = "Select count(*) from " + this.oBD.getTabla().getNombre() + " where " + frag.getPredicado() + ";";
                    n = acc2.ejecutarConsulta(numero);
                    acc2.desconectar();
                }
                int num = ((Double) Double.parseDouble("" + ((ArrayList) n.get(0)).get(0))).intValue();
                for (int i = 0; i < num; i++) {
                    acc2 = new AccesoDatosPG(this.oBD.getDirbd() + ":" + conexionOrigen.getPuerto(), conexionOrigen.getBasedatos(), conexionOrigen.getUsuario(), conexionOrigen.getPassword());
                    if (acc2.conectar()) {
                        sql = "Select " + frag.getAtributosMostrar() + " from " + this.oBD.getTabla().getNombre() + " where " + frag.getPredicado() + " Limit 1 offset " + i;
                        atributos = acc2.ejecutarConsulta(sql);
                        acc2.desconectar();
                    }
                    if (atributos != null) {
                        acc = new AccesoDatosPG(sitio + ":" + conexionDestino.getPuerto(), conexionDestino.getBasedatos(), conexionDestino.getUsuario(), conexionDestino.getPassword());
                        if (acc.conectar()) {
                            for (int k = 0; k < atributos.size(); k++) {
                                String valor = "";
                                ArrayList tupla = (ArrayList) atributos.get(k);
                                for (int j = 0; j < tupla.size(); j++) {
                                    if (this.oBD.transTipoDato(frag.getAtts().get(j).getTipo()) == 0 || this.oBD.transTipoDato(frag.getAtts().get(j).getTipo()) == 2) {
                                        valor += "'" + tupla.get(j) + "',";
                                    } else {
                                        valor += tupla.get(j) + ",";
                                    }
                                }
                                valor = valor.substring(0, valor.length() - 1);
                                sql = "insert into " + frag.getNombre() + "(" + frag.getAtributosMostrar() + ") values (" + valor + ");";
                                acc.ejecutarComando(sql);

                            }
                            acc.desconectar();
                        }
                    }
                }

                //Insertar a XAMANA
                //SITIO
                String id_sitio = "";
                String idFragmento = "";

                AccesoDatos accXamana = new AccesoDatos();
                if (accXamana.conectar()) {
                    sql = "insert into Sitio (direccion) "
                            + "select * from (select '" + sitio + "' as direccion ) as tmp "
                            + "where not exists (select direccion from Sitio where direccion='" + sitio + "') limit 1;";
                    accXamana.ejecutarComando(sql);
                    sql = "select id_sitio from Sitio where direccion='" + sitio + "'";
                    id_sitio = "" + ((ArrayList) accXamana.ejecutarConsulta(sql).get(0)).get(0);
                    //FRAGMENTO
                    if (frag.isMultimedia()) {
                        accXamana.ejecutarComando("insert into Fragmento(nombre, id_tabla, id_sitio, noperaciones, ndesempenio, isCBIR) value ('"
                                + frag.getNombre() + "', " + this.oBD.getTabla().getIdTabla()
                                + ", " + id_sitio + ",0," + costo + ",1)");
                        idFragmento = "" + ((ArrayList) accXamana.ejecutarConsulta("Select LAST_INSERT_ID()").get(0)).get(0);
                    } else {
                        accXamana.ejecutarComando("insert into Fragmento(nombre, id_tabla, id_sitio, noperaciones, ndesempenio, isCBIR) value ('"
                                + frag.getNombre() + "', " + this.oBD.getTabla().getIdTabla()
                                + ", " + id_sitio + ",0," + costo + ",0)");
                        idFragmento = "" + ((ArrayList) accXamana.ejecutarConsulta("Select LAST_INSERT_ID()").get(0)).get(0);
                    }
                    if (!this.oBD.isNullorEmpty(idFragmento)) {
                        for (Atributo a : frag.getAtts()) {
                            if (a.getNombre().compareTo(this.oBD.getTabla().getAtributoMultimedia().split(":")[0]) == 0) {
                                accXamana.ejecutarComando("insert into Atributo (tipo,size,nom,id_fragmento,descriptor,multimedia,pk,id_tabla,descriptor_Visual) "
                                        + "values ('" + a.getTipo() + "', " + a.getTamanio() + ", '" + a.getNombre() + "', " + idFragmento + ", "
                                        + (a.isDescriptor() ? 1 : 0) + ", " + (a.isMultimedia() ? 1 : 0) + ", "
                                        + (a.getNombre().compareTo(this.oBD.getTabla().getAtributoLlave().getNombre()) == 0 ? 1 : 0) + ", null,1)");
                            } else {
                                accXamana.ejecutarComando("insert into Atributo (tipo,size,nom,id_fragmento,descriptor,multimedia,pk,id_tabla,descriptor_Visual) "
                                        + "values ('" + a.getTipo() + "', " + a.getTamanio() + ", '" + a.getNombre() + "', " + idFragmento + ", "
                                        + (a.isDescriptor() ? 1 : 0) + ", " + (a.isMultimedia() ? 1 : 0) + ", "
                                        + (a.getNombre().compareTo(this.oBD.getTabla().getAtributoLlave().getNombre()) == 0 ? 1 : 0) + ", null,0)");
                            }

                        }

                    }
                    //Atributo pk de tabla original
                    accXamana.ejecutarComando("insert into Atributo (tipo,size,nom,id_fragmento,descriptor,multimedia,pk,id_tabla) "
                            + "values ('" + this.oBD.getTabla().getAtributoLlave().getTipo() + "', " + this.oBD.getTabla().getAtributoLlave().getTamanio() + ", '" + this.oBD.getTabla().getAtributoLlave().getNombre() + "', null, 0, 0, 1, "
                            + this.oBD.getTabla().getIdTabla() + ")");
                    //Desempenio de tabla original
                    accXamana.ejecutarComando("update Tabla set ndesempenio=" + cDesempenioTablaOriginal + " where id_tabla=" + this.oBD.getTabla().getIdTabla());
                    accXamana.desconectar();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.progress1 += percent;

        }
        this.progress1 = 100;
    }

    private void hybridFragmentationMongoDB(int opc) {
        try {
            //creación de tablas
            this.progress1 = 1;
            int percent = 100 / this.fragments.size();
            AccesoDatos accXamana = new AccesoDatos();
            double cDesempenioTablaOriginal = (opc == 1) ? this.VHCost : this.HVCost;
            for (HybridFragment frag : this.fragments) {
                this.progress1 += percent;
                String sitio = (opc == 1) ? frag.getSitiov() : frag.getSitioh();
                double costo = (opc == 1) ? frag.getCostoDesV() : frag.getCostoDesH();
                Sitio conexionDestino = conexionSitio(sitio);
                Sitio conexionOrigen = conexionSitio(this.oBD.getDirbd());
                AccesoDatosMG accDestino = new AccesoDatosMG(sitio + ":" + conexionDestino.getPuerto(), conexionDestino.getBasedatos(), this.oBD.getTabla().getNombre(), conexionDestino.getUsuario(), conexionDestino.getPassword());
                AccesoDatosMG accOrigen = new AccesoDatosMG(this.oBD.getDirbd() + ":" + conexionOrigen.getPuerto(), conexionOrigen.getBasedatos(), this.oBD.getTabla().getNombre(), conexionOrigen.getUsuario(), conexionOrigen.getPassword());
                if (accDestino.conectar() && accOrigen.conectar()) {
                    //Destino
                    MongoDatabase dbDestino = accDestino.getMongoClient().getDatabase(conexionDestino.getBasedatos());
                    MongoCollection<Document> docDestino = dbDestino.getCollection(frag.getNombre());
                    dbDestino.createCollection(frag.getNombre());

                    //Origen
                    MongoDatabase dbOrigen = accOrigen.getMongoClient().getDatabase(conexionOrigen.getBasedatos());
                    MongoCollection<Document> docOrigen = dbOrigen.getCollection(this.oBD.getTabla().getNombre());

                    List<String> nombresAtt = new ArrayList<>();
                    frag.getAtts().forEach(x -> nombresAtt.add(x.getNombre()));
                    Bson proyeccion = Projections.fields(Projections.include(nombresAtt));
                    Document d = Document.parse((frag.getPredicado().isEmpty()) ? "{}" : frag.getPredicado());
                    MongoCursor<Document> resultado = docOrigen.find(d).projection(proyeccion).iterator();
                    while (resultado.hasNext()) {
                        docDestino.insertOne(resultado.next());
                    }

                }

                //Insertar a XAMANA
                //SITIO
                String id_sitio = "";
                String idFragmento = "";
                String sql;
                if (accXamana.conectar()) {
                    sql = "insert into Sitio (direccion) "
                            + "select * from (select '" + sitio + "' as direccion ) as tmp "
                            + "where not exists (select direccion from Sitio where direccion='" + sitio + "') limit 1;";
                    accXamana.ejecutarComando(sql);
                    sql = "select id_sitio from Sitio where direccion='" + sitio + "'";

                    id_sitio = "" + ((ArrayList) accXamana.ejecutarConsulta(sql).get(0)).get(0);
                    //FRAGMENTO
                    accXamana.ejecutarComando("insert into Fragmento(nombre, id_tabla, id_sitio, noperaciones, ndesempenio, isCBIR) value ('"
                            + frag.getNombre() + "', " + this.oBD.getTabla().getIdTabla()
                            + ", " + id_sitio + ",0," + costo + ",0)");
                    idFragmento = "" + ((ArrayList) accXamana.ejecutarConsulta("Select LAST_INSERT_ID()").get(0)).get(0);
                    if (!this.isNullorEmpty(idFragmento)) {
                        for (Atributo a : frag.getAtts()) {
                            if (a.getNombre().compareTo(this.oBD.getTabla().getAtributoMultimedia().split(":")[0]) == 0) {
                                accXamana.ejecutarComando("insert into Atributo (tipo,size,nom,id_fragmento,descriptor,multimedia,pk,id_tabla,descriptor_Visual) "
                                        + "values ('" + a.getTipo() + "', " + a.getTamanio() + ", '" + a.getNombre() + "', " + idFragmento + ", "
                                        + (a.isDescriptor() ? 1 : 0) + ", " + (a.isMultimedia() ? 1 : 0) + ", "
                                        + (a.getNombre().compareTo(this.oBD.getTabla().getAtributoLlave().getNombre()) == 0 ? 1 : 0) + ", null,1)");
                            } else {
                                accXamana.ejecutarComando("insert into Atributo (tipo,size,nom,id_fragmento,descriptor,multimedia,pk,id_tabla,descriptor_Visual) "
                                        + "values ('" + a.getTipo() + "', " + a.getTamanio() + ", '" + a.getNombre() + "', " + idFragmento + ", "
                                        + (a.isDescriptor() ? 1 : 0) + ", " + (a.isMultimedia() ? 1 : 0) + ", "
                                        + (a.getNombre().compareTo(this.oBD.getTabla().getAtributoLlave().getNombre()) == 0 ? 1 : 0) + ", null,0)");
                            }
                        }

                    }
                }
                this.progress1 = this.getProgress1() + percent;
            }
            //Atributo pk de tabla original
            accXamana.ejecutarComando("insert into Atributo (tipo,size,nom,id_fragmento,descriptor,multimedia,pk,id_tabla) "
                    + "values ('" + this.oBD.getTabla().getAtributoLlave().getTipo() + "', " + this.oBD.getTabla().getAtributoLlave().getTamanio() + ", '" + this.oBD.getTabla().getAtributoLlave().getNombre() + "', null, 0, 0, 1, "
                    + this.oBD.getTabla().getIdTabla() + ")");
            //Desempenio de tabla original
            accXamana.ejecutarComando("update Tabla set ndesempenio=" + cDesempenioTablaOriginal + " where id_tabla=" + this.oBD.getTabla().getIdTabla());
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.progress1 = 100;
        this.oBD.setProgress1(100);
    }

    public boolean isNullorEmpty(String s) {
        return (s != null) ? ((!s.isEmpty()) ? false : true) : true;
    }

    int VAO(String linea) {
        int ret = 0;
        if (this.oBD.getTipoBD().compareTo("a") == 0 || this.oBD.getTipoBD().compareTo("b") == 0 || this.oBD.getTipoBD().compareTo("c") == 0) {
            if (linea.toLowerCase().contains("select")) {
                ret = 1;
            } else if ((linea.toLowerCase().contains("delete")
                    || linea.toLowerCase().contains("insert"))) {
                ret = 2;
            } else if (linea.toLowerCase().contains("update")) {
                ret = 3;
            }
        } else {
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
        }
        return ret;
    }

    public void hybridOverlapMongoDB(int opc) {
        ArrayList<String> predicados = new ArrayList<>();
        for (HybridFragment f : this.fragments) {
            if (!predicados.contains(f.getPredicado())) {
                if (f.getPredicado().charAt(0) != '{') {
                    predicados.add("{" + f.getPredicado() + "}");
                    f.setPredicado("{" + f.getPredicado() + "}");
                } else {
                    predicados.add(f.getPredicado());
                }

            }
        }
        ArrayList<PredicadoCosto> predicadoCosto = new ArrayList<>();
        String predicadoFragmento = "";
        for (String item : predicados) {
            if (item.contains("predicate") || item.contains("predicado")) {
                predicadoFragmento = "";
                for (HybridFragment hf2 : this.getFragments()) {
                    if (!hf2.getPredicado().contains("predicate") && !hf2.getPredicado().contains("predicado")) {
                        String campo = hf2.getPredicado().substring(1, hf2.getPredicado().indexOf(":"));
                        String valor = hf2.getPredicado().substring(hf2.getPredicado().indexOf(":") + 1, hf2.getPredicado().length() - 1);
                        if (!valor.contains("$eq") && !valor.contains("$gt") && !valor.contains("$gte")
                                && !valor.contains("$in") && !valor.contains("$lt")
                                && !valor.contains("$lte") && !valor.contains("$ne")
                                && !valor.contains("$nin")) {
                            valor = "{$eq:" + valor + "}";
                        }
                        predicadoFragmento = "{" + campo + ":{$not:" + valor + "}}," + predicadoFragmento;
                    }
                }
                predicadoFragmento = "{$and:[" + predicadoFragmento.substring(0, predicadoFragmento.length() - 1) + "]}";
            }
            int ocurrencias = 0;
            double costo = 0;
            for (HybridFragment f : this.fragments) {
                if (f.getPredicado().compareTo(item) == 0) {
                    ocurrencias++;
                    costo = (opc == 1) ? costo + f.getCostoDesV() : costo + f.getCostoDesH();
                }
            }
            PredicadoCosto p = new PredicadoCosto();
            p.setPredicado(item);
            p.setCosto(costo / ocurrencias);
            predicadoCosto.add(p);
        }
        Collections.sort(predicadoCosto, (PredicadoCosto a, PredicadoCosto b) -> new Double(b.getCosto()).compareTo(new Double(a.getCosto())));
        ArrayList<HybridFragment> ordenados = new ArrayList<>();
        for (PredicadoCosto item : predicadoCosto) {
            for (HybridFragment hf : this.getFragments()) {
                if (hf.getPredicado().compareTo(item.getPredicado()) == 0) {
                    ordenados.add(hf);
                }
            }
        }
        this.setFragments(ordenados);
        String negado = "";
        for (PredicadoCosto item : predicadoCosto) {
            //item=(item.getPredicado().toLowerCase().contains("predicate") || item.getPredicado().toLowerCase().contains("predicado"))?
            //      predicadoFragmento:item;    
            if (!item.getPredicado().toLowerCase().contains("predicate") && !item.getPredicado().toLowerCase().contains("predicado")) {
                String campo = item.getPredicado().substring(1, item.getPredicado().indexOf(":"));
                String valor = item.getPredicado().substring(item.getPredicado().indexOf(":") + 1, item.getPredicado().length() - 1);
                if (negado.isEmpty()) {
                    if (!valor.contains("$eq") && !valor.contains("$gt") && !valor.contains("$gte")
                            && !valor.contains("$in") && !valor.contains("$lt")
                            && !valor.contains("$lte") && !valor.contains("$ne")
                            && !valor.contains("$nin")) {
                        valor = "{$eq:" + valor + "}";
                        item.setNegado("{" + campo + ":" + valor + "}");
                    } else {
                        item.setNegado("{" + campo + ":" + valor + "}");
                    }
                } else {
                    item.setNegado(negado + item.getPredicado());
                }
                if (!negado.contains(item.getNegado())) {
                    if (!valor.contains("$eq") && !valor.contains("$gt") && !valor.contains("$gte")
                            && !valor.contains("$in") && !valor.contains("$lt")
                            && !valor.contains("$lte") && !valor.contains("$ne")
                            && !valor.contains("$nin")) {
                        valor = "{$eq:" + valor + "}";
                    }
                    negado = "{" + campo + ":{$not:" + valor + "}}," + negado;
                }
            } else {
                item.setNegado(predicadoFragmento);
            }
        }
        for (PredicadoCosto item : predicadoCosto) {
            for (HybridFragment hf : this.getFragments()) {
                if (hf.getPredicado().compareTo(item.getPredicado()) == 0) {
                    hf.setPredicado(item.getNegado());
                }
            }
        }
        //eliminar ands
        for (HybridFragment hf : this.getFragments()) {
            if (hf.getPredicado().length() >= 1 && hf.getPredicado().charAt(hf.getPredicado().length() - 1) == ',') {
                hf.setPredicado(hf.getPredicado().substring(0, hf.getPredicado().length() - 1).toLowerCase());
            }
        }
        this.getFragments().forEach(x -> System.out.println(x.getPredicado()));
    }

    public void hybridOverlapMongoDBv2(int opc) {
        ArrayList<String> predicados = new ArrayList<>();//predicados sin repetir (estan repetidos en el esquema híbrido)
        for (HybridFragment f : this.fragments) {
            if (!predicados.contains(f.getPredicado())) {
                predicados.add(f.getPredicado());
            }
        }
        ArrayList<PredicadoCosto> predicadoCosto = new ArrayList<>();
        for (String item : predicados) {
            int ocurrencias = 0;
            double costo = 0;
            for (HybridFragment f : this.fragments) {
                if (f.getPredicado().compareTo(item) == 0) {
                    ocurrencias++;
                    costo = (opc == 1) ? costo + f.getCostoDesV() : costo + f.getCostoDesH();
                }
            }
            PredicadoCosto p = new PredicadoCosto();
            p.setPredicado(item);
            p.setCosto(costo / ocurrencias);
            predicadoCosto.add(p);
        }
        Collections.sort(predicadoCosto, (PredicadoCosto a, PredicadoCosto b) -> new Double(b.getCosto()).compareTo(new Double(a.getCosto())));
        ArrayList<HybridFragment> ordenados = new ArrayList<>();
        for (PredicadoCosto item : predicadoCosto) {
            for (HybridFragment hf : this.getFragments()) {
                if (hf.getPredicado().compareTo(item.getPredicado()) == 0) {
                    ordenados.add(hf);
                }
            }
        }
        this.setFragments(ordenados);
        String negado = "";
        ArrayList<PredicadoCosto> aux = predicadoCosto;
        for (PredicadoCosto item : predicadoCosto) {
            if (item.getPredicado().contains(":")) {
                String[] camposValores = item.getPredicado().substring(1, item.getPredicado().length() - 1).split(",");
                if (negado.isEmpty()) {
                    String valorFuera = "";
                    for (String campoValorDentro : camposValores) {
                        String campo = campoValorDentro.substring(0, campoValorDentro.indexOf(":"));
                        String valor = campoValorDentro.substring(campoValorDentro.indexOf(":") + 1);
                        if (!valor.contains("$eq") && !valor.contains("$gt") && !valor.contains("$gte")
                                && !valor.contains("$in") && !valor.contains("$lt")
                                && !valor.contains("$lte") && !valor.contains("$ne")
                                && !valor.contains("$nin")) {
                            valorFuera += campo + ":{$eq:" + valor + "},";
                        } else {
                            valorFuera += campo + ":" + valor + ",";
                        }
                    }
                    valorFuera = valorFuera.substring(0, valorFuera.length() - 1);
                    item.setPredicadoTraslape("{" + valorFuera + "}");
                } else {
                    //corrección de llaves
                    item.setPredicado(item.getPredicado().replace("}", ""));
                    Long nLlaves = item.getPredicado().chars().filter(x -> x == '{').count();
                    item.setPredicado(item.getPredicado() + (new String(new char[nLlaves.intValue()]).replace("\0", "}")));
                    item.setPredicadoTraslape(negado + item.getPredicado());
                }
                String valorFuera = "";
                for (String campoValorDentro : camposValores) {
                    String campo = campoValorDentro.substring(0, campoValorDentro.indexOf(":"));
                    String valor = campoValorDentro.substring(campoValorDentro.indexOf(":") + 1);
                    if (!valor.contains("$eq") && !valor.contains("$gt") && !valor.contains("$gte")
                            && !valor.contains("$in") && !valor.contains("$lt")
                            && !valor.contains("$lte") && !valor.contains("$ne")
                            && !valor.contains("$nin")) {
                        String temp = "{" + campo + ":{$not:{$eq:" + valor;

                        //corrección de llaves
                        temp = temp.replace("}", "");
                        Long nLlaves = temp.chars().filter(x -> x == '{').count();
                        temp = (temp + (new String(new char[nLlaves.intValue()]).replace("\0", "}"))) + ",";

                        valorFuera += temp;
                    } else {
                        String temp = "{" + campo + ":{$not:" + valor;

                        //corrección de llaves
                        temp = temp.replace("}", "");
                        Long nLlaves = temp.chars().filter(x -> x == '{').count();
                        temp = (temp + (new String(new char[nLlaves.intValue()]).replace("\0", "}"))) + ",";

                        valorFuera += temp;
                    }
                }
                valorFuera = valorFuera.substring(0, valorFuera.length() - 1);
                negado = "{$or:[" + valorFuera + "]}," + negado;
            }
        }
        for (PredicadoCosto f2 : predicadoCosto) {
            if (f2.getPredicadoTraslape() != null) {
                if (f2.getPredicadoTraslape().length() >= 1 && f2.getPredicadoTraslape().charAt(f2.getPredicadoTraslape().length() - 1) == ',') {
                    f2.setPredicadoTraslape(f2.getPredicadoTraslape().substring(0, f2.getPredicadoTraslape().length() - 1));
                }
                f2.setPredicadoTraslape("{\"$and\":[" + f2.getPredicadoTraslape() + "]}");
            }
        }
        if (negado.length() >= 1 && negado.charAt(negado.length() - 1) == ',') {
            negado = negado.substring(0, negado.length() - 1);
        }
        String and = "{\"$and\":[" + negado + "]}";
        //agregar el traslape al esquema
        for (int i = 0; i < aux.size(); i++) {
            for (HybridFragment f : this.getFragments()) {
                if (aux.get(i).getPredicado().compareTo(f.getPredicado()) == 0) {
                    f.setPredicado(predicadoCosto.get(i).getPredicadoTraslape());
                }
            }
        }
        HybridFragment newFragment = new HybridFragment();
        newFragment.setNombre("temp");
        newFragment.setAtts(this.oBD.getTabla().getAtributos());
        newFragment.setCostoDesH(0.0);
        newFragment.setCostoDesV(0.0);
        newFragment.setMultimedia(false);
        newFragment.setSitiov(this.oBD.getDirbd());
        newFragment.setSitioh(this.oBD.getDirbd());
        newFragment.setPredicado(and);
        this.getFragments().add(newFragment);
    }

    public void nombrar() {
        for (int i = 0; i < this.getFragments().size(); i++) {
            this.getFragments().get(i).setNombre(this.oBD.getTabla().getNombre() + "_f" + (i + 1));
        }
    }

}
