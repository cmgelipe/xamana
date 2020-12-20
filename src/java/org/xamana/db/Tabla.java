package org.xamana.db;

import java.util.List;
import javax.faces.model.SelectItem;

/**
 *
 * @author pipe
 */
public class Tabla {
    private String tipoFragment;
    private boolean CBIR;
    private String nombre;
    private int umbralOP;
    private int umbralCO;
    private String atributoMultimedia;
    

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
    public int getUmbralOP() {
        return umbralOP;
    }

    /**
     * @param umbralOP the umbralOP to set
     */
    public void setUmbralOP(int umbralOP) {
        this.umbralOP = umbralOP;
    }

    /**
     * @return the umbralCO
     */
    public int getUmbralCO() {
        return umbralCO;
    }

    /**
     * @param umbralCO the umbralCO to set
     */
    public void setUmbralCO(int umbralCO) {
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
}