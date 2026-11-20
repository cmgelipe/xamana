package org.xamana.db;

import java.util.ArrayList;
import java.util.List;
import jakarta.faces.model.SelectItem;

/**
 *
 * @author pipe
 */
public class Tabla {
    private String tipoFragment;
    private boolean CBIR=false;
    private String nombre;
    private Double umbralOP;
    private Double umbralCO;
    private String atributoMultimedia;
    private Atributo atributoLlave;
    private ArrayList<Atributo> Atributos;
    private String idTabla; //mi tabla que se llama tabla, no la remota
    

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
     * @return the tipoFragment
     */
    public String getTipoFragment() {
        return tipoFragment;
    }

    /**
     * @param tipoFragment the tipoFragment to set
     */
    public void setTipoFragment(String tipoFragment) {
        this.tipoFragment = tipoFragment;
    }

    /**
     * @return the CBIR
     */
    public boolean isCBIR() {
        return CBIR;
    }

    /**
     * @param CBIR the CBIR to set
     */
    public void setCBIR(boolean CBIR) {
        this.CBIR = CBIR;
    }

    /**
     * @return the umbralOP
     */
    public Double getUmbralOP() {
        return umbralOP;
    }

    /**
     * @param umbralOP the umbralOP to set
     */
    public void setUmbralOP(Double umbralOP) {
        this.umbralOP = umbralOP;
    }

    /**
     * @return the umbralCO
     */
    public Double getUmbralCO() {
        return umbralCO;
    }

    /**
     * @param umbralCO the umbralCO to set
     */
    public void setUmbralCO(Double umbralCO) {
        this.umbralCO = umbralCO;
    }

    /**
     * @return the atributoMultimedia
     */
    public String getAtributoMultimedia() {
        return atributoMultimedia;
    }

    /**
     * @param atributoMultimedia the atributoMultimedia to set
     */
    public void setAtributoMultimedia(String atributoMultimedia) {
        this.atributoMultimedia = atributoMultimedia;
    }

    /**
     * @return the Atributos
     */
    public ArrayList<Atributo> getAtributos() {
        return Atributos;
    }

    /**
     * @param Atributos the Atributos to set
     */
    public void setAtributos(ArrayList<Atributo> Atributos) {
        this.Atributos = Atributos;
    }
    /**
     * @return the atributoLlave
     */
    public Atributo getAtributoLlave() {
        return atributoLlave;
    }

    /**
     * @param atributoLlave the atributoLlave to set
     */
    public void setAtributoLlave(Atributo atributoLlave) {
        this.atributoLlave = atributoLlave;
    }

    /**
     * @return the idTabla
     */
    public String getIdTabla() {
        return idTabla;
    }

    /**
     * @param idTabla the idTabla to set
     */
    public void setIdTabla(String idTabla) {
        this.idTabla = idTabla;
    }
    
    public int buscaIndiceAttxNombre(String nombre){
        int r=-1;
        for(int i=0;i<this.getAtributos().size();i++){
            if(this.getAtributos().get(i).getNombre().compareTo(nombre)==0){
                r=i;
            }
        }
        return r;
    }
}