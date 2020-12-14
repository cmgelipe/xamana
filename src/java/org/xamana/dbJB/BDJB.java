/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.dbJB;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import org.xamana.db.bd;

/**
 *
 * @author pipe
 */
@ManagedBean(name = "bd")
@SessionScoped
public class BDJB implements Serializable {

    private bd obj = new bd();
    private boolean bconfig = false;
    private boolean bbotonfragmentar = false;
    private boolean bdatosincorrectos=false;

    /**
     * Creates a new instance of BD
     */
    public BDJB() {
    }

    public void configuracion() {
        bconfig = true;
    }

    public void cancelarConf() {
        bconfig = false;
    }

    public void probarConexion() {
        System.out.println("prueba " +obj.getTipoBD());
        if(obj.conexionexterna()){
            this.bbotonfragmentar=true;
            this.bconfig=false;
            this.bdatosincorrectos=false;
        }else{
            this.bbotonfragmentar=false;
            this.bconfig=false;
            this.bdatosincorrectos=true;
        }
    }

    public void generarEsquema() {
        this.bbotonfragmentar=false;
    }
    public void revisarDatos() {
        this.bdatosincorrectos=false;
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

    /**
     * @return the obj
     */
    public bd getObj() {
        return obj;
    }

    /**
     * @param obj the obj to set
     */
    public void setObj(bd obj) {
        this.obj = obj;
    }

    /**
     * @return the bdatosincorrectos
     */
    public boolean isBdatosincorrectos() {
        return bdatosincorrectos;
    }

    /**
     * @param bdatosincorrectos the bdatosincorrectos to set
     */
    public void setBdatosincorrectos(boolean bdatosincorrectos) {
        this.bdatosincorrectos = bdatosincorrectos;
    }

}
