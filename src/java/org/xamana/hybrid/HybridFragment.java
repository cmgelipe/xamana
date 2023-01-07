/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.xamana.hybrid;

import java.util.ArrayList;
import org.xamana.db.Atributo;

/**
 *
 * @author pipe
 */
public class HybridFragment {
    private String nombre;
    private ArrayList<Atributo> atts;
    private String predicado;
    private String sitioh;
    private String sitiov;
    private double costo;

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return the atts
     */
    public ArrayList<Atributo> getAtts() {
        return atts;
    }

    /**
     * @param atts the atts to set
     */
    public void setAtts(ArrayList<Atributo> atts) {
        this.atts = atts;
    }

    /**
     * @return the predicado
     */
    public String getPredicado() {
        return predicado;
    }

    /**
     * @param predicado the predicado to set
     */
    public void setPredicado(String predicado) {
        this.predicado = predicado;
    }

    /**
     * @return the sitio
     */
    public String getSitioh() {
        return sitioh;
    }

    /**
     * @param sitio the sitio to set
     */
    public void setSitioh(String sitio) {
        this.sitioh = sitio;
    }
    
    /**
     * @return the sitio
     */
    public String getSitiov() {
        return sitiov;
    }

    /**
     * @param sitio the sitio to set
     */
    public void setSitiov(String sitio) {
        this.sitiov = sitio;
    }

    /**
     * @return the costo
     */
    public double getCosto() {
        return costo;
    }

    /**
     * @param costo the costo to set
     */
    public void setCosto(double costo) {
        this.costo = costo;
    }
    
}
