/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.verticalCBIR.model;

import java.util.ArrayList;
import java.util.stream.Collectors;
import org.xamana.db.Atributo;

/**
 *
 * @author aldoo
 */
public class TablaCosto {

    String sitio;
    Atributo atributo;
    double costo;
    String nombre;
    ArrayList<Atributo> arrAtri;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public ArrayList<Atributo> getArrAtri() {
        return arrAtri;
    }

    public void setArrAtri(ArrayList<Atributo> arrAtri) {
        this.arrAtri = arrAtri;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public String getSitio() {
        return sitio;
    }

    public void setSitio(String sitio) {
        this.sitio = sitio;
    }

    public Atributo getAtributo() {
        return atributo;
    }

    public void setAtributo(Atributo atributo) {
        this.atributo = atributo;
    }

    public String getAtributosxComa() {
        String cad = "";
        for (Atributo at : this.getArrAtri()) {
            cad+=at.getNombre()+",";
        }
        return cad.substring(0, cad.length()-1);
    }
}
