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
public class sitio {
    String nombre;
    String ip;

    public sitio(String nombre, String ip) {
        this.nombre = nombre;
        this.ip = ip;
    }

    public sitio() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    
    
}
