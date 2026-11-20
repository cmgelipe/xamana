/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.db;

/**
 *
 * @author pipe
 */
public class esquemaHorMultimedia {
    private String nombre;
    private String valor;
    private String nTuplas;
    private String porcentaje;
    private String nOperacionesAprox;
    private String ndesempenio;
    

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
     * @return the valor
     */
    public String getValor() {
        return valor;
    }

    /**
     * @param valor the valor to set
     */
    public void setValor(String valor) {
        this.valor = valor;
    }

    /**
     * @return the nTuplas
     */
    public String getNTuplas() {
        return getnTuplas();
    }

    /**
     * @param nTuplas the nTuplas to set
     */
    public void setNTuplas(String nTuplas) {
        this.setnTuplas(nTuplas);
    }

    /**
     * @return the nTuplas
     */
    public String getnTuplas() {
        return nTuplas;
    }

    /**
     * @param nTuplas the nTuplas to set
     */
    public void setnTuplas(String nTuplas) {
        this.nTuplas = nTuplas;
    }

    /**
     * @return the porcentaje
     */
    public String getPorcentaje() {
        return porcentaje;
    }

    /**
     * @param porcentaje the porcentaje to set
     */
    public void setPorcentaje(String porcentaje) {
        this.porcentaje = porcentaje;
    }

    /**
     * @return the nOperacionesAprox
     */
    public String getNOperacionesAprox() {
        return getnOperacionesAprox();
    }

    /**
     * @param nOperacionesAprox the nOperacionesAprox to set
     */
    public void setNOperacionesAprox(String nOperacionesAprox) {
        this.nOperacionesAprox = nOperacionesAprox;
    }

    /**
     * @return the nOperacionesAprox
     */
    public String getnOperacionesAprox() {
        return nOperacionesAprox;
    }

    /**
     * @return the ndesempenio
     */
    public String getNdesempenio() {
        return ndesempenio;
    }

    /**
     * @param ndesempenio the ndesempenio to set
     */
    public void setNdesempenio(String ndesempenio) {
        this.ndesempenio = ndesempenio;
    }
    
}
