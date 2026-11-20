package org.xamana.db;

import java.util.ArrayList;
import org.xamana.datos.AccesoDatos;

/**
 * Registro de sitios de XAMANA: un sitio remoto ya no comparte el puerto,
 * usuario y contraseña del sitio local. Cada fila de {@code sitio_conexion}
 * trae su propia línea de conexión (puerto, base de datos, gestor, usuario,
 * contraseña), identificada por la dirección (IP/host) del sitio, igual que
 * hoy la usa {@code sitio.direccion} (y por lo tanto {@code fragmento.id_sitio}
 * a través de ella) en los algoritmos de fragmentación. Es una tabla aparte
 * de {@code sitio} para no alterar esa tabla ni sus filas duplicadas por IP
 * que ya existen hoy.
 *
 * {@link #obtenerConexion(String)} es el único punto por el que pasa la
 * contraseña de un sitio al salir de la base de datos; hoy se devuelve en
 * texto plano, pero deja aislado el lugar donde conectar un cifrado más
 * adelante sin tocar a quienes llaman a este DAO.
 *
 * @author pipe
 */
public class SitioDAO {

    public ArrayList<Sitio> listarSitios() throws Exception {
        ArrayList<Sitio> sitios = new ArrayList<>();
        AccesoDatos acc = new AccesoDatos();
        if (acc.conectar()) {
            try {
                ArrayList res = acc.ejecutarConsulta(
                        "select direccion, puerto, basedatos, gestor, usuario, password from sitio_conexion order by direccion");
                for (Object fila : res) {
                    sitios.add(filaASitio((ArrayList) fila));
                }
            } finally {
                acc.desconectar();
            }
        }
        return sitios;
    }

    /**
     * Devuelve la línea de conexión del sitio con esa dirección, o
     * {@code null} si el sitio no está registrado.
     */
    public Sitio obtenerConexion(String direccion) throws Exception {
        Sitio sitio = null;
        AccesoDatos acc = new AccesoDatos();
        if (acc.conectar()) {
            try {
                ArrayList res = acc.ejecutarConsultaParam(
                        "select direccion, puerto, basedatos, gestor, usuario, password from sitio_conexion where direccion=?",
                        direccion);
                if (res != null && !res.isEmpty()) {
                    sitio = filaASitio((ArrayList) res.get(0));
                }
            } finally {
                acc.desconectar();
            }
        }
        return sitio;
    }

    public boolean existeSitio(String direccion) throws Exception {
        return obtenerConexion(direccion) != null;
    }

    /**
     * Registra un sitio nuevo, o completa su línea de conexión si ya
     * estaba registrado.
     */
    public void guardarSitio(Sitio sitio) throws Exception {
        AccesoDatos acc = new AccesoDatos();
        if (acc.conectar()) {
            try {
                ArrayList res = acc.ejecutarConsultaParam(
                        "select direccion from sitio_conexion where direccion=?", sitio.getDireccion());
                if (res != null && !res.isEmpty()) {
                    actualizarSitioConexion(acc, sitio);
                } else {
                    acc.ejecutarComandoParam(
                            "insert into sitio_conexion (direccion, puerto, basedatos, gestor, usuario, password) values (?,?,?,?,?,?)",
                            sitio.getDireccion(), sitio.getPuerto(), sitio.getBasedatos(),
                            sitio.getGestor(), sitio.getUsuario(), sitio.getPassword());
                }
            } finally {
                acc.desconectar();
            }
        }
    }

    public void actualizarSitio(Sitio sitio) throws Exception {
        AccesoDatos acc = new AccesoDatos();
        if (acc.conectar()) {
            try {
                actualizarSitioConexion(acc, sitio);
            } finally {
                acc.desconectar();
            }
        }
    }

    private void actualizarSitioConexion(AccesoDatos acc, Sitio sitio) throws Exception {
        acc.ejecutarComandoParam(
                "update sitio_conexion set puerto=?, basedatos=?, gestor=?, usuario=?, password=? where direccion=?",
                sitio.getPuerto(), sitio.getBasedatos(), sitio.getGestor(),
                sitio.getUsuario(), sitio.getPassword(), sitio.getDireccion());
    }

    /**
     * Elimina un sitio del registro de conexión. No borra sus fragmentos ya
     * asignados (tabla {@code fragmento}) ni su fila en {@code sitio}.
     */
    public void eliminarSitio(String direccion) throws Exception {
        AccesoDatos acc = new AccesoDatos();
        if (acc.conectar()) {
            try {
                acc.ejecutarComandoParam("delete from sitio_conexion where direccion=?", direccion);
            } finally {
                acc.desconectar();
            }
        }
    }

    private Sitio filaASitio(ArrayList fila) {
        Sitio sitio = new Sitio();
        sitio.setDireccion(valor(fila, 0));
        sitio.setPuerto(valor(fila, 1));
        sitio.setBasedatos(valor(fila, 2));
        sitio.setGestor(valor(fila, 3));
        sitio.setUsuario(valor(fila, 4));
        sitio.setPassword(valor(fila, 5));
        return sitio;
    }

    private String valor(ArrayList fila, int indice) {
        Object v = fila.get(indice);
        return v == null ? null : ("" + v);
    }
}
