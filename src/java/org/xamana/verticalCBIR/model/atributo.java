/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.verticalCBIR.model;

/**
 *
 * @author aldoo
 */
public class atributo {
    private String nombre;
    private String tipo;
    private String tamanio;
    boolean ban;

    public atributo() {
       }

    
    public boolean isBan() {
        return ban;
    }

    public void setBan(boolean ban) {
        this.ban = ban;
    }
    
    public atributo(String nombre, String tipo, String tamanio, boolean is) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.tamanio = tamanio;
        this.ban= is;
    }

   
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTamanio() {
        return tamanio;
    }

    public void setTamanio(String tamanio) {
        this.tamanio = tamanio;
    }

    
}
