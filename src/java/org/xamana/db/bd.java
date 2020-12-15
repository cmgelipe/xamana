package org.xamana.db;

import java.io.Serializable;
import org.xamana.datos.AccesoDatos;
import org.xamana.datos.AccesoDatosMG;
import org.xamana.datos.AccesoDatosPG;

/**
 *
 * @author pipe
 */
public class bd implements Serializable {

    AccesoDatos accexternomysql;
    AccesoDatosMG accexternomongo;
    AccesoDatosPG accexternopost;
    private String dirbd;
    private String nombd;
    private String usubd;
    private String passbd;
    private String TipoBD = "";
    private Tabla tabla = new Tabla();

    public boolean conexionexterna() {
        boolean res = false;
        
        if (TipoBD.compareTo("a") == 0) {
            try {
                accexternomysql = new AccesoDatos(dirbd, nombd, usubd, passbd);

                if (accexternomysql.conectar()) {
                    res = true;
                    accexternomysql.desconectar();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (TipoBD.compareTo("b") == 0 || TipoBD.compareTo("c") == 0) {
            try {
                accexternopost = new AccesoDatosPG(dirbd, nombd, usubd, passbd);
                if (accexternopost.conectar()) {
                    res = true;
                    accexternopost.desconectar();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (TipoBD.compareTo("d") == 0) {
            try {
                accexternomongo = new AccesoDatosMG(dirbd, nombd, this.tabla.getNombre(), usubd, passbd);
                if (accexternomongo.conectar()) {
                    res = true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return res;
    }

    /**
     * @return the dirbd
     */
    public String getDirbd() {
        return dirbd;
    }

    /**
     * @param dirbd the dirbd to set
     */
    public void setDirbd(String dirbd) {
        this.dirbd = dirbd;
    }

    /**
     * @return the usubd
     */
    public String getUsubd() {
        return usubd;
    }

    /**
     * @param usubd the usubd to set
     */
    public void setUsubd(String usubd) {
        this.usubd = usubd;
    }

    /**
     * @return the passbd
     */
    public String getPassbd() {
        return passbd;
    }

    /**
     * @param passbd the passbd to set
     */
    public void setPassbd(String passbd) {
        this.passbd = passbd;
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
     * @return the tabla
     */
    public Tabla getTabla() {
        return tabla;
    }

    /**
     * @param tabla the tabla to set
     */
    public void setTabla(Tabla tabla) {
        this.tabla = tabla;
    }

    /**
     * @return the nombd
     */
    public String getNombd() {
        return nombd;
    }

    /**
     * @param nombd the nombd to set
     */
    public void setNombd(String nombd) {
        this.nombd = nombd;
    }

}
