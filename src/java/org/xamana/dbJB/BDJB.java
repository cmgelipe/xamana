/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.dbJB;

import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import javax.faces.model.SelectItem;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFiles;
import org.xamana.db.bd;
import org.xamana.db.esquemaHorMultimedia;

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
    private boolean bdatosincorrectos = false;
    private boolean blog = true;
    private boolean dcolumna = false;
    private UploadedFiles files;
    private List<SelectItem> atributos;
    private ArrayList<esquemaHorMultimedia> valoresColumna;
    private boolean infoEsquema1 = true;
    private StreamedContent Descarga;
    String MensajeWARN="";
    private boolean bToken;
    
    

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

    public String probarConexion() {

        if (obj.conexionexterna()) {
            this.bbotonfragmentar = true;
            this.bconfig = false;
            this.bdatosincorrectos = false;
        } else {
            this.bbotonfragmentar = false;
            this.bconfig = false;
            this.bdatosincorrectos = true;
        }
        return "index.xhtml";
    }

    public void onCBIRchange() {
        try {
            if (this.obj.getTabla().isCBIR()
                    && this.obj.getTabla().getTipoFragment().compareTo("a") == 0) {
                if (this.obj.getTipoBD().compareTo("a") == 0) {
                    atributos = this.obj.listaAtributosMySQL();
                } else if (this.obj.getTipoBD().compareTo("b") == 0 || this.obj.getTipoBD().compareTo("c") == 0) {
                    atributos = this.obj.listaAtributosPostgreSQL();
                } else if (this.obj.getTipoBD().compareTo("d") == 0) {
                    atributos = this.obj.listaAtributosMongoDB();
                }
                this.dcolumna = true;
                this.blog = true;
            } else {
                this.dcolumna = false;
                this.blog = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String btnesquema() {
        if (this.obj.getTabla().isCBIR()
                && this.obj.getTabla().getTipoFragment().compareTo("a") == 0) {
            try {
                valoresColumna = this.obj.valoresDistintosColMultimedia(files);
                if (this.getObj().getTipoBD().compareTo("a") == 0) {
                    this.Descarga = DefaultStreamedContent.builder()
                            .name("HorizontalMySQLCBIR.rar")
                            .contentType("application/x-rar-compressed")
                            .stream(() -> FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/descargas/HorizontalMySQLCBIR.rar"))
                            .build();
                }else if(this.getObj().getTipoBD().compareTo("b") == 0 || this.getObj().getTipoBD().compareTo("c") == 0){
                    this.Descarga = DefaultStreamedContent.builder()
                            .name("HorizontalPostCBIR.rar")
                            .contentType("application/x-rar-compressed")
                            .stream(() -> FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/descargas/HorizontalPostCBIR.rar"))
                            .build();
                }else if(this.getObj().getTipoBD().compareTo("d") == 0){
                    this.Descarga = DefaultStreamedContent.builder()
                            .name("HorizontalMongoCBIR.rar")
                            .contentType("application/x-rar-compressed")
                            .stream(() -> FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/descargas/HorizontalMongoCBIR.rar"))
                            .build();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return "esquema.xhtml";
    }

    public String configurarFragmentacion() {
        this.bbotonfragmentar = false;
        return "config.xhtml";
    }

    public void revisarDatos() {
        this.bdatosincorrectos = false;
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

    /**
     * @return the files
     */
    public UploadedFiles getFiles() {
        return files;
    }

    /**
     * @param files the files to set
     */
    public void setFiles(UploadedFiles files) {
        this.files = files;
    }

    /**
     * @return the blog
     */
    public boolean isBlog() {
        return blog;
    }

    /**
     * @param blog the blog to set
     */
    public void setBlog(boolean blog) {
        this.blog = blog;
    }

    /**
     * @return the dcolumna
     */
    public boolean isDcolumna() {
        return dcolumna;
    }

    /**
     * @param dcolumna the dcolumna to set
     */
    public void setDcolumna(boolean dcolumna) {
        this.dcolumna = dcolumna;
    }

    /**
     * @return the atributos
     */
    public List<SelectItem> getAtributos() {
        return atributos;
    }

    /**
     * @param atributos the atributos to set
     */
    public void setAtributos(List<SelectItem> atributos) {
        this.atributos = atributos;
    }

    public void esquemaCBIRHorizontal() {
        this.dcolumna = false;
    }

    /**
     * @return the valoresColumna
     */
    public ArrayList<esquemaHorMultimedia> getValoresColumna() {
        return valoresColumna;
    }

    /**
     * @param valoresColumna the valoresColumna to set
     */
    public void setValoresColumna(ArrayList<esquemaHorMultimedia> valoresColumna) {
        this.valoresColumna = valoresColumna;
    }

    /**
     * @return the infoEsquema1
     */
    public boolean isInfoEsquema1() {
        return infoEsquema1;
    }

    /**
     * @param infoEsquema1 the infoEsquema1 to set
     */
    public void setInfoEsquema1(boolean infoEsquema1) {
        this.infoEsquema1 = infoEsquema1;
    }

    public void fragEstatica() {
        if (this.obj.isHorizontalCBIR()) {
            boolean ban=false;
            for(esquemaHorMultimedia t:valoresColumna){
                if(t.getNOperacionesAprox().compareTo("0")==0){
                    ban=true;
                }
            }
            if(ban){
                this.MensajeWARN="No se detectaron consultas en algún fragmento, se utilizará 1 como valor para realizar la primera "
                        + "fragmentación pero se recomienda no poner en marcha la fragmentación dinámica hasta que se detecten operaciones";
            }
            if (this.obj.getTipoBD().compareTo("a") == 0) {
                this.obj.fragmentarHorCBIRMySQL(valoresColumna);
            } else if (this.obj.getTipoBD().compareTo("b") == 0 || this.obj.getTipoBD().compareTo("c") == 0) {
                this.obj.fragmentarHorCBIRPostgres(valoresColumna);
            } else {
                this.obj.fragmentarHorCBIRMongoDB(valoresColumna);
            }
            
            this.setBToken(true);
        }
    }

    /**
     * @return the Descarga
     */
    public StreamedContent getDescarga() {
        return Descarga;
    }

    /**
     * @param Descarga the Descarga to set
     */
    public void setDescarga(StreamedContent Descarga) {
        this.Descarga = Descarga;
    }

    
    public void onComplete() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Fragmentación realizada",this.MensajeWARN));
    }

    /**
     * @return the bToken
     */
    public boolean isBToken() {
        return bToken;
    }

    /**
     * @param bToken the bToken to set
     */
    public void setBToken(boolean bToken) {
        this.bToken = bToken;
    }

   
}
