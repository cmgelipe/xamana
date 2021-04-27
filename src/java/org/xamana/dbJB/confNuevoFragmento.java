/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.dbJB;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;
import org.xamana.datos.AccesoDatos;
import org.xamana.datos.AccesoDatosMG;
import org.xamana.datos.AccesoDatosPG;
import org.xamana.db.Atributo;
import org.xamana.db.valoresTupla;

/**
 *
 * @author pipe
 */
@ManagedBean(name = "cNuevoFrag")
@SessionScoped
public class confNuevoFragmento implements Serializable {

    private BDJB beanBD;

    private int numeroTuplas;
    private boolean bVisible;
    private int numeroAtributos;
    ArrayList<String> u;
    ArrayList<String> att = new ArrayList<>();
    private valoresTupla val;

    public confNuevoFragmento() {

        Object objeto = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("bd");
        if (objeto != null) {
            beanBD = (BDJB) objeto;
        }
        bVisible = true;
        val = new valoresTupla();

        try {
            if (beanBD.getObj().getTipoBD().compareTo("a") == 0) {
                AccesoDatos acc = new AccesoDatos(beanBD.getObj().getDirbd() + ":" + beanBD.getObj().getPortbd(), beanBD.getObj().getNombd(), beanBD.getObj().getUsubd(), beanBD.getObj().getPassbd());
                if (acc.conectar()) {
                    att = acc.atributos(beanBD.getObj().getNombd(), beanBD.getObj().getTabla().getNombre());
                    acc.desconectar();
                }
            } else if (beanBD.getObj().getTipoBD().compareTo("b") == 0 || beanBD.getObj().getTipoBD().compareTo("c") == 0) {
                AccesoDatosPG acc = new AccesoDatosPG(beanBD.getObj().getDirbd() + ":" + beanBD.getObj().getPortbd(), beanBD.getObj().getNombd(), beanBD.getObj().getUsubd(), beanBD.getObj().getPassbd());
                if (acc.conectar()) {
                    att = acc.atributos(beanBD.getObj().getNombd(), beanBD.getObj().getTabla().getNombre());
                    acc.desconectar();
                }
            } else if (beanBD.getObj().getTipoBD().compareTo("d") == 0) {
                AccesoDatosMG acc = new AccesoDatosMG(beanBD.getObj().getDirbd() + ":" + beanBD.getObj().getPortbd(), beanBD.getObj().getNombd(), beanBD.getObj().getTabla().getNombre(), beanBD.getObj().getUsubd(), beanBD.getObj().getPassbd());
                if (acc.conectar()) {
                    att = acc.atributos(beanBD.getObj().getNombd(), beanBD.getObj().getTabla().getNombre());
                }
            }

            if (!att.isEmpty()) {
                this.numeroAtributos = att.size();
                for (int i = 0; i < att.size(); i++) {

                    switch (i) {
                        case 0:
                            val.setN1(att.get(0).split(":")[0]);
                            break;
                        case 1:
                            val.setN2(att.get(1).split(":")[0]);
                            break;
                        case 2:
                            val.setN3(att.get(2).split(":")[0]);
                            break;
                        case 3:
                            val.setN4(att.get(3).split(":")[0]);
                            break;
                        case 4:
                            val.setN5(att.get(4).split(":")[0]);
                            break;
                        case 5:
                            val.setN6(att.get(5).split(":")[0]);
                            break;
                        case 6:
                            val.setN7(att.get(6).split(":")[0]);
                            break;
                        case 7:
                            val.setN8(att.get(7).split(":")[0]);
                            break;
                        case 8:
                            val.setN9(att.get(8).split(":")[0]);
                            break;
                        case 9:
                            val.setN10(att.get(9).split(":")[0]);
                            break;
                        case 10:
                            val.setN11(att.get(10).split(":")[0]);
                            break;
                        case 11:
                            val.setN12(att.get(11).split(":")[0]);
                            break;
                    }
                }
            } else {
                System.out.println("pasa");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the numeroTuplas
     */
    public int getNumeroTuplas() {
        return numeroTuplas;
    }

    /**
     * @param numeroTuplas the numeroTuplas to set
     */
    public void setNumeroTuplas(int numeroTuplas) {
        this.numeroTuplas = numeroTuplas;
    }

    /**
     * @return the bVisible
     */
    public boolean isBVisible() {
        return bVisible;
    }

    /**
     * @param bVisible the bVisible to set
     */
    public void setBVisible(boolean bVisible) {
        this.bVisible = bVisible;
    }

    public void procesar() {
        String valores = "";
        String tipo = "";
        if (this.beanBD.getObj().getTipoBD().compareTo("a") == 0 || this.beanBD.getObj().getTipoBD().compareTo("b") == 0 || this.beanBD.getObj().getTipoBD().compareTo("c") == 0) {
            for (int i = 0; i < att.size(); i++) {
                switch (i) {
                    case 0:
                        tipo = att.get(i).split(":")[1];
                        if (this.beanBD.getObj().transTipoDato(tipo) == 0 || this.beanBD.getObj().transTipoDato(tipo) == 2) {
                            valores += "\"" + val.getV1() + "\", ";
                        } else {
                            valores += val.getV1() + ", ";
                        }
                        break;
                    case 1:
                        tipo = att.get(i).split(":")[1];
                        if (this.beanBD.getObj().transTipoDato(tipo) == 0 || this.beanBD.getObj().transTipoDato(tipo) == 2) {
                            valores += "\"" + val.getV2() + "\", ";
                        } else {
                            valores += val.getV2() + ", ";
                        }
                        break;
                    case 2:
                        tipo = att.get(i).split(":")[1];
                        if (this.beanBD.getObj().transTipoDato(tipo) == 0 || this.beanBD.getObj().transTipoDato(tipo) == 2) {
                            valores += "\"" + val.getV3() + "\", ";
                        } else {
                            valores += val.getV3() + ", ";
                        }
                        break;
                    case 3:
                        tipo = att.get(i).split(":")[1];
                        if (this.beanBD.getObj().transTipoDato(tipo) == 0 || this.beanBD.getObj().transTipoDato(tipo) == 2) {
                            valores += "\"" + val.getV4() + "\", ";
                        } else {
                            valores += val.getV4() + ", ";
                        }
                        break;
                    case 4:
                        tipo = att.get(i).split(":")[1];
                        if (this.beanBD.getObj().transTipoDato(tipo) == 0 || this.beanBD.getObj().transTipoDato(tipo) == 2) {
                            valores += "\"" + val.getV5() + "\", ";
                        } else {
                            valores += val.getV5() + ", ";
                        }
                        break;
                    case 5:
                        tipo = att.get(i).split(":")[1];
                        if (this.beanBD.getObj().transTipoDato(tipo) == 0 || this.beanBD.getObj().transTipoDato(tipo) == 2) {
                            valores += "\"" + val.getV6() + "\", ";
                        } else {
                            valores += val.getV6() + ", ";
                        }
                        break;
                    case 6:
                        tipo = att.get(i).split(":")[1];
                        if (this.beanBD.getObj().transTipoDato(tipo) == 0 || this.beanBD.getObj().transTipoDato(tipo) == 2) {
                            valores += "\"" + val.getV7() + "\", ";
                        } else {
                            valores += val.getV7() + ", ";
                        }
                        break;
                    case 7:
                        tipo = att.get(i).split(":")[1];
                        if (this.beanBD.getObj().transTipoDato(tipo) == 0 || this.beanBD.getObj().transTipoDato(tipo) == 2) {
                            valores += "\"" + val.getV8() + "\", ";
                        } else {
                            valores += val.getV8() + ", ";
                        }
                        break;
                    case 8:
                        tipo = att.get(i).split(":")[1];
                        if (this.beanBD.getObj().transTipoDato(tipo) == 0 || this.beanBD.getObj().transTipoDato(tipo) == 2) {
                            valores += "\"" + val.getV9() + "\", ";
                        } else {
                            valores += val.getV9() + ", ";
                        }
                        break;
                    case 9:
                        tipo = att.get(i).split(":")[1];
                        if (this.beanBD.getObj().transTipoDato(tipo) == 0 || this.beanBD.getObj().transTipoDato(tipo) == 2) {
                            valores += "\"" + val.getV10() + "\", ";
                        } else {
                            valores += val.getV10() + ", ";
                        }
                        break;
                    case 10:
                        tipo = att.get(i).split(":")[1];
                        if (this.beanBD.getObj().transTipoDato(tipo) == 0 || this.beanBD.getObj().transTipoDato(tipo) == 2) {
                            valores += "\"" + val.getV11() + "\", ";
                        } else {
                            valores += val.getV11() + ", ";
                        }
                        break;
                    case 11:
                        tipo = att.get(i).split(":")[1];
                        if (this.beanBD.getObj().transTipoDato(tipo) == 0 || this.beanBD.getObj().transTipoDato(tipo) == 2) {
                            valores += "\"" + val.getV12() + "\", ";
                        } else {
                            valores += val.getV12() + ", ";
                        }
                        break;
                }
            }
        }else{
            
        }
        valores = valores.substring(0, valores.length() - 2);
        String q = "INSERT INTO " + this.beanBD.getNombreNuevoFrag() + " VALUES (" + valores + ");";
        try {
            AccesoDatos acc = new AccesoDatos(beanBD.getObj().getDirbd() + ":" + beanBD.getObj().getPortbd(), beanBD.getObj().getNombd(), beanBD.getObj().getUsubd(), beanBD.getObj().getPassbd());
            int y = 0;
            if (acc.conectar()) {
                y = acc.ejecutarComando(q);

                acc.desconectar();
            }
            if (y != 0) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Operación realizada", "Se insertó la tupla"));
                this.val.setV1("");
                this.val.setV2("");
                this.val.setV3("");
                this.val.setV4("");
                this.val.setV5("");
                this.val.setV6("");
                this.val.setV7("");
                this.val.setV8("");
                this.val.setV9("");
                this.val.setV10("");
                this.val.setV11("");
                this.val.setV12("");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error", "Error al insertar la tupla"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAgregarCampos(String y) {
        u.add(y);
    }

    public String getAgregarCampos() {
        return "";
    }

    /**
     * @return the val
     */
    public valoresTupla getVal() {
        return val;
    }

    /**
     * @param val the val to set
     */
    public void setVal(valoresTupla val) {
        this.val = val;
    }

    public boolean visible(int n) {
        return n <= this.numeroAtributos;
    }

    /**
     * @return the numeroAtributos
     */
    public int getNumeroAtributos() {
        return numeroAtributos;
    }

    /**
     * @param numeroAtributos the numeroAtributos to set
     */
    public void setNumeroAtributos(int numeroAtributos) {
        this.numeroAtributos = numeroAtributos;
    }
}
