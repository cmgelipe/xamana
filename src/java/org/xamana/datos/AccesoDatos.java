package org.xamana.datos;

import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author felipe
 */
public class AccesoDatos implements Serializable {

    private static String sUrl = null;
    private static String sUsr = null;
    private static String sPwd = null;
    private java.sql.Connection oConexion;

    public AccesoDatos() throws Exception {
        sUrl = "jdbc:mysql://localhost/xamana";
        sUsr = "root";
        sPwd = "";
    }
    
    public AccesoDatos(String url, String usr, String pwd) throws Exception {
        sUrl = "jdbc:mysql://"+url;//"jdbc:mysql://localhost/Master";
        sUsr = usr;//"root";
        sPwd = pwd;//"sunny_4teamo";
    }

    public boolean conectar() throws Exception {
        boolean bRet = false;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            oConexion = DriverManager.getConnection(sUrl, sUsr, sPwd);
            bRet = true;
        } catch (SQLException e) {
            throw e;
        }
        return bRet;
    }

    public void desconectar() throws Exception {

        oConexion.close();
    }
    /**
     * Código que se ejecuta cuando este objeto es colectado.
     */
    @Override
    public void finalize() throws Exception, Throwable {
        try {
            oConexion.close();
            oConexion = null;
        } finally {
            super.finalize();
        }
    }

    public synchronized ArrayList ejecutarConsulta(String psQuery) throws Exception {

        Statement stmt = null;
        ResultSet rset = null;
        ArrayList vrset = null;
        ResultSetMetaData rsmd = null;
        int nNumCols = 0;
        try {
            stmt = oConexion.createStatement();
            rset = stmt.executeQuery(psQuery);
            rsmd = rset.getMetaData();
            nNumCols = rsmd.getColumnCount();
            vrset = convierteALista(rset, rsmd, nNumCols);
        } finally {
            if (rset != null) {
                rset.close();
                if (stmt != null) {
                    stmt.close();
                }
            }
            rset = null;
            stmt = null;
        }

        return vrset;
    }

    public synchronized int ejecutarComando(String psStatement)
            throws Exception {

        int ret = 0;
        ArrayList vTransaction = new ArrayList();

        vTransaction.add(psStatement);
        ret = ejecutarComando(vTransaction);

        return ret;
    }

    public synchronized int ejecutarComando(ArrayList pvStatement)
            throws Exception {

        int ret = 0, i = 0;
        Statement stmt = null;
        String temp = "";

        try {
            oConexion.setAutoCommit(false);
            stmt = oConexion.createStatement();
            for (i = 0; i < pvStatement.size(); i++) {
                temp = (String) pvStatement.get(i);
                ret += stmt.executeUpdate(temp);
            }
            oConexion.commit();
        } catch (SQLException e) {
            oConexion.rollback();
            throw e;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            stmt = null;
        }

        return ret;
    }

    private synchronized ArrayList convierteALista(ResultSet rset,
            ResultSetMetaData rsmd,
            int nNumCols)
            throws Exception {
        ArrayList vrset = new ArrayList();
        ArrayList vrsettmp = null;
        int i = 0;

        while (rset.next()) {
            vrsettmp = new ArrayList();
            for (i = 1; i <= nNumCols; i++) {
                switch (rsmd.getColumnType(i)) {
                    case Types.CHAR:
                    case Types.VARCHAR:
                        String varchar = "" + doubleQuote(rset.getString(i));
                        vrsettmp.add(varchar);
                        break;
                    case Types.INTEGER:
                        vrsettmp.add(new Double(rset.getLong(i)));
                        break;
                    case Types.SMALLINT:
                        vrsettmp.add(new Double(rset.getInt(i)));
                        break;
                    case Types.BIGINT:
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                    case Types.FLOAT:
                    case Types.DOUBLE:
                        vrsettmp.add(new Double(rset.getDouble(i)));
                        break;
                    case Types.DATE:
                    case Types.TIME:
                    case Types.TIMESTAMP:
                        vrsettmp.add((rset.getTimestamp(i) == null ? null : new Date(rset.getTimestamp(i).getTime())));
                        break;
                    default:
                        String str = "" + rset.getString(i);
                        vrsettmp.add(str);
                } //switch  
            }  //for
            vrset.add(vrsettmp);
        } //while
        return vrset;
    }

    @Override
    public String toString() {
        String s = "";
        s = "Class = AccesoDatos \n"
                + "    static sUrl  = " + sUrl + "\n"
                + "    static sUsr = " + sUsr + "\n"
                + "    static sPwd  = " + sPwd + "\n"
                + "    oConexion = " + oConexion + "\n";
        return s;
    }

    private String doubleQuote(String psCadena) {
        if (psCadena == null) {
            psCadena = "";
        }
        String CadenaEntrada = "";
        if (psCadena.equals("")) {
            return psCadena;
        } else if (psCadena.equals("\"")) {
            return "&quot;";
        } else {
            int indice = -2;
            CadenaEntrada = psCadena;
            while ((indice = CadenaEntrada.indexOf("\"", indice + 2)) != -1) {
                CadenaEntrada = CadenaEntrada.substring(0, CadenaEntrada.indexOf("\"", indice)) + "&quot;" + CadenaEntrada.substring(CadenaEntrada.indexOf("\"", indice) + 1);
            }
        }
        return CadenaEntrada;
    }
    public synchronized ArrayList<String> tablas(String base){
        ArrayList<String> ret=new ArrayList<String>();
        try {
            DatabaseMetaData metadata=oConexion.getMetaData();
            ResultSet tablas=metadata.getTables(base, null, null, new String[]{"TABLE","VIEW"});
            while(tablas.next()){
                ret.add(tablas.getString("TABLE_NAME"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(AccesoDatos.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public synchronized ArrayList<String> atributos(String nombreBase, String nombre) {
        ArrayList<String> ret=new ArrayList<String>();
        try {
            DatabaseMetaData metadata=oConexion.getMetaData();
            ResultSet atributos=metadata.getColumns(null, null, nombre, null);
            while(atributos.next())
                ret.add(atributos.getString("COLUMN_NAME")+":"+atributos.getString("TYPE_NAME")+":"+atributos.getString("COLUMN_SIZE"));
        } catch (SQLException ex) {
            Logger.getLogger(AccesoDatos.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
}
