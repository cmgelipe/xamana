/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.db;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;

/**
 *
 * @author pipe
 */
@ManagedBean(name="bd")
@SessionScoped
public class BD implements Serializable {
private String TipoBD="";
private boolean bconfig=false;
private boolean bbotonfragmentar=false;
    /**
     * Creates a new instance of BD
     */
    public BD() {
    }
    
    public void configuracion(){
        bconfig=true;
    }
    public void cancelarConf(){
        bconfig=false;
    }
    public void probarConexion(){
        
    }
    public void generarEsquema(){
        
    }
    /**
     * @return the TipoBD
     */
    public String getTipoBD() {
        return TipoBD;
    }

    /**
     * @param TipoBD the TipoBD to set
     */
    public void setTipoBD(String TipoBD) {
        this.TipoBD = TipoBD;
    }

    /**
     * @return the bconfig
     */
    public boolean isBconfig() {
        return bconfig;
    }

    /**
     * @param bconfig the bconfig to set
     */
    public void setBconfig(boolean bconfig) {
        this.bconfig = bconfig;
    }

    /**
     * @return the bbotonfragmentar
     */
    public boolean isBbotonfragmentar() {
        return bbotonfragmentar;
    }

    /**
     * @param bbotonfragmentar the bbotonfragmentar to set
     */
    public void setBbotonfragmentar(boolean bbotonfragmentar) {
        this.bbotonfragmentar = bbotonfragmentar;
    }
    
}
