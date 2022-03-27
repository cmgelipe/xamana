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
public class Query {
    sitio sitio;
    ArrayList <Atributo> atr;
    String tipoOpera;
    double frecuencia; 

    public double getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(double frecuencia) {
        this.frecuencia = frecuencia;
    }
    public Query() {
      
    }
    public ArrayList<Atributo> getAtr() {
        return atr;
    }

    public void setAtr(ArrayList<Atributo> atr) {
        this.atr = atr;
    }

    public String getTipoOpera() {
        return tipoOpera;
    }

    public void setTipoOpera(String tipoOpera) {
        this.tipoOpera = tipoOpera;
    }

    
    public sitio getSitio() {
        return sitio;
    }

    public void setSitio(sitio sitio) {
        this.sitio = sitio;
    }
    public String getAtributos(){
    ArrayList <String> cad = new ArrayList<>();
    String separador=", ";
    this.getAtr().forEach((at) -> {
        cad.add(at.getNombre());
        });
    
    return cad.stream().map(String::toUpperCase).collect(Collectors.joining(separador));
    
    }
    
    
}
