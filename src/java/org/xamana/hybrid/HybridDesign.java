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

                            String predicado = this.predicado(actual);
                            double nTuplas = this.nTuplas(predicado);
                            //cuanto sitios se requieren para satisfacer la operación
                            double vRemotoV = this.vRemoto(ipLinea, predicado, "v");
                            double vRemotoH = this.vRemoto(ipLinea, predicado, "h");
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
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private String predicado(String sentencia) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private double vRemoto(String ip, String predicado, String sitiovh) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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
