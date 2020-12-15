package org.xamana.db;

/**
 *
 * @author pipe
 */
public class Tabla {
    private String tipoFragment;
    private boolean CBIR;
    private String nombre;

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
}
