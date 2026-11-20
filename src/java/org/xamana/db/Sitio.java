package org.xamana.db;

import java.io.Serializable;

/**
 * Registro de conexión de un sitio remoto: su propia dirección, puerto,
 * base de datos, gestor, usuario y contraseña. Reemplaza el supuesto de
 * que todos los sitios comparten las credenciales y el puerto del sitio
 * local de XAMANA.
 *
 * @author pipe
 */
public class Sitio implements Serializable {

    private String direccion;
    private String puerto;
    private String basedatos;
    private String gestor;
    private String usuario;
    private String password;

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPuerto() {
        return puerto;
    }

    public void setPuerto(String puerto) {
        this.puerto = puerto;
    }

    public String getBasedatos() {
        return basedatos;
    }

    public void setBasedatos(String basedatos) {
        this.basedatos = basedatos;
    }

    public String getGestor() {
        return gestor;
    }

    public void setGestor(String gestor) {
        this.gestor = gestor;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
