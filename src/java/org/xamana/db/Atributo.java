/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.db;

import java.util.HashMap;

/**
 *
 * @author pipe
 */
public class Atributo {
    private String idAtributo;
    private String nombre;
    private String tipo;
    private String tamanio;
    private boolean multimedia;
    private boolean descriptor;
    private HashMap<String, Double> costoAtributoxSitio;
    private String sitioMasCostoso;
    private boolean pk;
    
    public Atributo(){
        costoAtributoxSitio=new HashMap<>();
    }
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
     * @return the tipo
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * @param tipo the tipo to set
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /**
     * @return the tamanio
     */
    public String getTamanio() {
        return tamanio;
    }

    /**
     * @param tamanio the tamanio to set
     */
    public void setTamanio(String tamanio) {
        this.tamanio = tamanio;
    }

    /**
     * @return the idAtributo
     */
    public String getIdAtributo() {
        return idAtributo;
    }

    /**
     * @param idAtributo the idAtributo to set
     */
    public void setIdAtributo(String idAtributo) {
        this.idAtributo = idAtributo;
    }

    /**
     * @return the multimedia
     */
    public boolean isMultimedia() {
        return multimedia;
    }

    /**
     * @param multimedia the multimedia to set
     */
    public void setMultimedia(boolean multimedia) {
        this.multimedia = multimedia;
    }

    /**
     * @return the descriptor
     */
    public boolean isDescriptor() {
        return descriptor;
    }

    /**
     * @param descriptor the descriptor to set
     */
    public void setDescriptor(boolean descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * @return the pk
     */
    public boolean isPk() {
        return pk;
    }

    /**
     * @param pk the pk to set
     */
    public void setPk(boolean pk) {
        this.pk = pk;
    }

    /**
     * @return the costoAtributoxSitio
     */
    public HashMap<String, Double> getCostoAtributoxSitio() {
        return costoAtributoxSitio;
    }

    /**
     * @param costoAtributoxSitio the costoAtributoxSitio to set
     */
    public void setCostoAtributoxSitio(HashMap<String, Double> costoAtributoxSitio) {
        this.costoAtributoxSitio = costoAtributoxSitio;
    }

    /**
     * @return the sitioMasCostoso
     */
    public String getSitioMasCostoso() {
        return sitioMasCostoso;
    }

    /**
     * @param sitioMasCostoso the sitioMasCostoso to set
     */
    public void setSitioMasCostoso(String sitioMasCostoso) {
        this.sitioMasCostoso = sitioMasCostoso;
    }
    
}
