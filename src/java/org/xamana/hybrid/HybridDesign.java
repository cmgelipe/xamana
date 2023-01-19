/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.xamana.hybrid;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import org.xamana.db.Atributo;
import org.xamana.db.bd;
import org.xamana.dhf.model.Fragment;
import org.xamana.dhf.schema.Schema;
import org.xamana.verticalCBIR.model.TablaCosto;

/**
 *
 * @author pipe
 */
public class HybridDesign {

    private double HVCost;
    private double VHCost;
    private ArrayList<HybridFragment> fragments;

    bd oBD;

    public HybridDesign(bd oBD) {
        fragments = new ArrayList<>();
        this.oBD = oBD;
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

    public void costsMySQL(UploadedFiles files) {
        System.out.println("-----------COSTOS PIPE HÍBRIDA-------------");
        HashMap<String, String> NombreIp = new HashMap<>();
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
                            //buscar en esquema si hay algun atributo (por lo menos uno) local 
                            //(la operación se realiza en el mismo lugar a donde esta el atributo), 
                            //si lo hay entonces considerar operación local
                            //de otro modo es remota
                            //primero sitiov y luego sitioh
                            boolean siHay = false;
                            for (HybridFragment fragmento : this.getFragments()) {
                                if (fragmento.getSitiov().compareTo(ipLinea) == 0) {
                                    for (Atributo l : fragmento.getAtts()) {
                                        if (actual.toLowerCase().contains(l.getNombre().toLowerCase()) || (actual.contains("*") && actual.toLowerCase().contains("select"))) {
                                            siHay = true;
                                        }
                                    }
                                }
                            }
                            if (!siHay) {
                                ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                            }
                            System.out.println("VAO: " + ValorAsignadoOperacion);
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

                            System.out.println("CR: " + nJoins);
                            double costoOperacionv = 0.0;
                            for (Atributo a : this.oBD.getTabla().getAtributos()) {
                                if (actual.toLowerCase().contains(a.getNombre().toLowerCase())) {
                                    System.out.println("\tTamaño de atributo " + a.getNombre() + ": " + Double.parseDouble(a.getTamanio()));
                                    costoOperacionv += ValorAsignadoOperacion * nJoins * Double.parseDouble(a.getTamanio());
                                    System.out.println("Costo de operación: " + costoOperacionv);
                                }
                            }
                            System.out.println("Costo de operación vertical V: " + costoOperacionv);
                            this.setVHCost(this.getVHCost() + costoOperacionv);
                            //se recalcula pero ahora con sitioh para el HVcosto
                            siHay = false;
                            for (HybridFragment fragmento : this.getFragments()) {
                                if (fragmento.getSitioh().compareTo(ipLinea) == 0) {
                                    for (Atributo l : fragmento.getAtts()) {
                                        if (actual.toLowerCase().contains(l.getNombre().toLowerCase()) || (actual.contains("*") && actual.toLowerCase().contains("select"))) {
                                            siHay = true;
                                        }
                                    }
                                }
                            }
                            if (!siHay) {
                                ValorAsignadoOperacion = ValorAsignadoOperacion * 2;
                            }
                            double costoOperacionh = 0.0;
                            for (Atributo a : this.oBD.getTabla().getAtributos()) {
                                if (actual.toLowerCase().contains(a.getNombre().toLowerCase())) {
                                    System.out.println("\tTamaño de atributo " + a.getNombre() + ": " + Double.parseDouble(a.getTamanio()));
                                    costoOperacionh += ValorAsignadoOperacion * nJoins * Double.parseDouble(a.getTamanio());
                                    System.out.println("Costo de operación: " + costoOperacionh);
                                }
                            }
                            System.out.println("Costo de operación vertical h: " + costoOperacionh);
                            this.setHVCost(this.getHVCost() + costoOperacionh);

                            //////////horizontal
                            ValorAsignadoOperacion = 0;
                            if (actual.toLowerCase().contains("select")) {
                                ValorAsignadoOperacion = 1;
                            } else if ((actual.toLowerCase().contains("delete")
                                    || actual.toLowerCase().contains("insert"))) {
                                ValorAsignadoOperacion = 2;
                            } else if (actual.toLowerCase().contains("update")) {
                                ValorAsignadoOperacion = 3;
                            }
                            String predicados = this.where_statement(actual).toLowerCase().replace("where", "");
                            double nTuplas = this.nTuplas(predicados);
                            System.out.println("Tuplas: " + nTuplas);
                            //¿qué sitios se requieren para satisfacer el predicado y atributos 
                            //de la operación? R: los mismos sitios de la vertical
                            
                            //¿se requieren sitios remotos?
                            double vRemotoV = this.vRemoto(ipLinea, actual, "v");
                            double vRemotoH = this.vRemoto(ipLinea, actual, "h");
                            double costohorizontalv = nTuplas * ValorAsignadoOperacion * vRemotoV;
                            double costohorizontalh = nTuplas * ValorAsignadoOperacion * vRemotoH;
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
                                    fragments.get(i).setCostoDesV(fragments.get(i).getCostoDesV() + costoOperacionv+costohorizontalv);
                                    fragments.get(i).setCostoDesH(fragments.get(i).getCostoDesH() + costoOperacionh+costohorizontalh);
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
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public void costsMongoDB(UploadedFiles files) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private double nTuplas(String predicado) {
        String q1 = "";
        if (!predicado.isEmpty()) {
            q1 = "select count(*) from " + this.oBD.getTabla().getNombre() + " where " + predicado;
        } else {
            q1 = "select count(*) from " + this.oBD.getTabla().getNombre();
        }
        double count1 = 0.0;
        ArrayList r1 = new ArrayList();

        try {
            if (this.oBD.getAccexternomysql().conectar()) {
                r1 = this.oBD.getAccexternomysql().ejecutarConsulta(q1);

                this.oBD.getAccexternomysql().desconectar();
            }
            if (r1.size() > 0) {
                count1 = (double) ((ArrayList) r1.get(0)).get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count1;
    }

    private ArrayList<String> predicado(String q) {
        ArrayList<String> ret = new ArrayList<>();
        if (this.oBD.getTipoBD().compareTo("a") == 0) {
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
        } else if (this.oBD.getTipoBD().compareTo("b") == 0 || this.oBD.getTipoBD().compareTo("c") == 0) {

        } else if (this.oBD.getTipoBD().compareTo("d") == 0) {

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

    private double vRemoto(String ip, String actual, String sitiovh) {
        String predicado = this.where_statement(actual).toLowerCase().replace("where", "");
        boolean local = false;
        ArrayList<HybridFragment> hf = new ArrayList<>();
        for (HybridFragment hf3 : this.getFragments()) {
            if (sitiovh.compareTo("h") == 0) {
                if (hf3.getSitioh().compareTo(ip) == 0) {
                    hf.add(hf3);
                }
            } else if (sitiovh.compareTo("v") == 0) {

                if (hf3.getSitiov().compareTo(ip) == 0) {
                    hf.add(hf3);
                }
            }
        }
        if (!hf.isEmpty()) {
            double count1 = 0.0;
            String q1 = (!predicado.isEmpty()) ? "select count(*) from " + this.oBD.getTabla().getNombre() + " where " + predicado : "select count(*) from " + this.oBD.getTabla().getNombre();
            ArrayList r1 = new ArrayList();
            try {
                if (this.oBD.getAccexternomysql().conectar()) {
                    r1 = this.oBD.getAccexternomysql().ejecutarConsulta(q1);
                    this.oBD.getAccexternomysql().desconectar();
                }
                if (!r1.isEmpty()) {
                    count1 = (double) ((ArrayList) r1.get(0)).get(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (count1 > 0) {
                ArrayList<HybridFragment> fResuelvenPredicados = new ArrayList<>();
                for (HybridFragment hf4 : hf) {
                    String predicadoFragmento = hf4.getPredicado();
                    if (hf4.getPredicado().contains("predicate") || hf4.getPredicado().contains("predicado")) {
                        predicadoFragmento = "";
                        for (HybridFragment hf2 : this.getFragments()) {
                            if (!hf2.getPredicado().contains("predicate") && !hf2.getPredicado().contains("predicado")) {
                                predicadoFragmento += "!(" + hf2.getPredicado() + ") AND ";
                            }
                        }
                        predicadoFragmento = predicadoFragmento.substring(0, predicadoFragmento.length() - 5);
                    }
                    String q2 = (!predicado.isEmpty()) ? "select count(*) from " + this.oBD.getTabla().getNombre() + " where " + predicadoFragmento + " and " + predicado : "select count(*) from " + this.oBD.getTabla().getNombre() + " where " + predicadoFragmento;

                    ArrayList r2 = new ArrayList();
                    try {
                        if (this.oBD.getAccexternomysql().conectar()) {
                            r2 = this.oBD.getAccexternomysql().ejecutarConsulta(q2);
                            this.oBD.getAccexternomysql().desconectar();
                        }
                        if (!r2.isEmpty()) {
                            double nTuplas = ((double) ((ArrayList) r2.get(0)).get(0));
                            if (nTuplas > 0) {
                                fResuelvenPredicados.add(hf4);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //filtrar fResuelvenPredicados por atributos
                ArrayList<String> atributosOpe = buscarAtributos(actual);
                ArrayList<HybridFragment> fResuelvenAtributos = new ArrayList<>();
                for (HybridFragment fragmento : fResuelvenPredicados) {
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
                if (!fResuelvenAtributos.isEmpty()) {
                    local = true;
                }
            } else {
                local = true;
            }
        } else {
            local = false;
        }
        return (local) ? 1 : 2;
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
        }
        return ret;
    }

}
