/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.verticalCBIR.model;


import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 *
 * @author aldoo
 */
public class TablaCosto {
    sitio sitio;
    atributo atributo;
    double costo;
    String nombre;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    ArrayList<atributo> arrAtri;

    public ArrayList<atributo> getArrAtri() {
        return arrAtri;
    }

    public void setArrAtri(ArrayList<atributo> arrAtri) {
        this.arrAtri = arrAtri;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }
    

    public sitio getSitio() {
        return sitio;
    }

    public void setSitio(sitio sitio) {
        this.sitio = sitio;
    }

    public atributo getAtributo() {
        return atributo;
    }

    public void setAtributo(atributo atributo) {
        this.atributo = atributo;
    }
    public String getAtributos(){
    ArrayList <String> cad = new ArrayList<>();
    String separador=", ";
    for(atributo at: this.getArrAtri()){
        cad.add(at.getNombre());
    }
    
    return cad.stream().map(String::toLowerCase).collect(Collectors.joining(separador));
    
    }
}