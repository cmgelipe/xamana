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
    ArrayList<HybridFragment> fragments;
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
                fragments.add(newFragment);
            }
        }
    }

    public void costsMySQL(UploadedFiles files) {
        System.out.println("-----------COSTOS PIPE-------------");
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
                            for (HybridFragment fragmento : this.fragments) {
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
                            int nJoins = 0;
                            for (HybridFragment fragmento : this.fragments) {
                                boolean seUsa = false;
                                for (Atributo l : fragmento.getAtts()) {
                                    if (actual.toLowerCase().contains(l.getNombre().toLowerCase()) || (actual.contains("*") && actual.toLowerCase().contains("select"))) {
                                        seUsa = true;
                                    }
                                }
                                if (seUsa) {
                                    nJoins++;
                                }
                            }
                            if (nJoins < 1) {
                                nJoins = 1;
                            }

                            System.out.println("CR: " + nJoins);
                            double costoOperacionv = 0.0;
                            for (Atributo a : this.oBD.getTabla().getAtributos()) {
                                if (actual.toLowerCase().contains(a.getNombre().toLowerCase())) {
                                    System.out.println("\tTamaño de atributo " + a.getNombre() + ": " + Double.parseDouble(a.getTamanio()));
                                    costoOperacionv += ValorAsignadoOperacion * nJoins * Double.parseDouble(a.getTamanio());
                                    System.out.println("Costo de operación: " + costoOperacionv);
                                }
                            }
                            this.setVHCost(this.getVHCost() + costoOperacionv);
                            //se recalcula pero ahora con sitioh para el HVcosto
                            siHay = false;
                            for (HybridFragment fragmento : this.fragments) {
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
                                    costoOperacionh += ValorAsignadoOperacion * nJoins * Double.parseDouble(a.getTamanio());
                                }
                            }
                            this.setHVCost(this.getHVCost() + costoOperacionh);

                            //////////horizontal
                            ValorAsignadoOperacion = 0;
                            if (actual.toLowerCase().contains("select") && actual.toLowerCase().contains(this.oBD.getTabla().getNombre().toLowerCase())) {
                                ValorAsignadoOperacion = 1;
                            } else if ((actual.toLowerCase().contains("delete")
                                    || actual.toLowerCase().contains("insert")) && actual.toLowerCase().contains(this.oBD.getTabla().getNombre().toLowerCase())) {
                                ValorAsignadoOperacion = 2;
                            } else if (actual.toLowerCase().contains("update") && actual.toLowerCase().contains(this.oBD.getTabla().getNombre().toLowerCase())) {
                                ValorAsignadoOperacion = 3;
                            }

                            ArrayList<String> predicados = this.predicado(actual);
                            double nTuplas = this.nTuplas((predicados.size() > 0) ? predicados.get(0) : "");
                            //cuanto sitios se requieren para satisfacer la operación
                            double vRemotoV = this.vRemoto(ipLinea, (predicados.size() > 0) ? predicados.get(0) : "", "v");
                            double vRemotoH = this.vRemoto(ipLinea, (predicados.size() > 0) ? predicados.get(0) : "", "h");
                            this.setVHCost(this.getVHCost() + nTuplas * ValorAsignadoOperacion * vRemotoV);
                            this.setHVCost(this.getHVCost() + nTuplas * ValorAsignadoOperacion * vRemotoH);
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
        String q1 = "select count(*) from " + this.oBD.getTabla().getNombre() + " where " + predicado;
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

    private double vRemoto(String ip, String predicado, String sitiovh) {
        boolean local = false;
        for (HybridFragment hf : this.fragments) {
            String predicadoFragmento = hf.getPredicado();
            String q1 = "select count(*) from " + this.oBD.getTabla().getNombre() + " where " + predicado;
            String q2 = "select count(*) from " + this.oBD.getTabla().getNombre() + " where " + predicadoFragmento + " and " + predicado;
            ArrayList r1 = new ArrayList();
            ArrayList r2 = new ArrayList();
            try {
                if (this.oBD.getAccexternomysql().conectar()) {
                    r1 = this.oBD.getAccexternomysql().ejecutarConsulta(q1);
                    r2 = this.oBD.getAccexternomysql().ejecutarConsulta(q2);
                    this.oBD.getAccexternomysql().desconectar();
                }
                if (r1.size() > 0 && r2.size() > 0) {
                    double count1 = (double) ((ArrayList) r1.get(0)).get(0);
                    double count2 = (double) ((ArrayList) r2.get(0)).get(0);
                    if (count2 >= count1) {
                        local = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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

}
