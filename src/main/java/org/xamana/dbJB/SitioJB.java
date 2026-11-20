package org.xamana.dbJB;

import java.io.Serializable;
import java.util.ArrayList;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import org.xamana.db.Sitio;
import org.xamana.db.SitioDAO;

/**
 * Bean JSF para la pantalla de configuración de sitios: registrar, editar
 * y eliminar la línea de conexión (puerto, base de datos, gestor, usuario y
 * contraseña) de cada sitio remoto, en vez de reutilizar la del sitio local.
 *
 * @author pipe
 */
@Named(value = "sitioBean")
@SessionScoped
public class SitioJB implements Serializable {

    private final SitioDAO dao = new SitioDAO();
    private ArrayList<Sitio> sitios = new ArrayList<>();
    private Sitio sitioEdicion = new Sitio();
    private boolean dsitio = false;

    public void abrirRegistro() {
        cargarSitios();
        this.sitioEdicion = new Sitio();
        this.dsitio = true;
    }

    public void cerrarRegistro() {
        this.dsitio = false;
    }

    public void cargarSitios() {
        try {
            this.sitios = dao.listarSitios();
        } catch (Exception ex) {
            ex.printStackTrace();
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "Sitios", "No se pudo consultar el registro de sitios");
        }
    }

    public void editar(Sitio s) {
        Sitio copia = new Sitio();
        copia.setDireccion(s.getDireccion());
        copia.setPuerto(s.getPuerto());
        copia.setBasedatos(s.getBasedatos());
        copia.setGestor(s.getGestor());
        copia.setUsuario(s.getUsuario());
        copia.setPassword(s.getPassword());
        this.sitioEdicion = copia;
    }

    public void nuevo() {
        this.sitioEdicion = new Sitio();
    }

    public void guardar() {
        if (isNullOrEmpty(sitioEdicion.getDireccion()) || isNullOrEmpty(sitioEdicion.getPuerto())
                || isNullOrEmpty(sitioEdicion.getBasedatos()) || isNullOrEmpty(sitioEdicion.getGestor())) {
            agregarMensaje(FacesMessage.SEVERITY_WARN, "Sitios", "Dirección, puerto, base de datos y gestor son obligatorios");
            return;
        }
        try {
            dao.guardarSitio(sitioEdicion);
            agregarMensaje(FacesMessage.SEVERITY_INFO, "Sitios", "Sitio " + sitioEdicion.getDireccion() + " guardado");
            this.sitioEdicion = new Sitio();
            cargarSitios();
        } catch (Exception ex) {
            ex.printStackTrace();
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "Sitios", "No se pudo guardar el sitio");
        }
    }

    public void eliminar(Sitio s) {
        try {
            dao.eliminarSitio(s.getDireccion());
            agregarMensaje(FacesMessage.SEVERITY_INFO, "Sitios", "Sitio " + s.getDireccion() + " eliminado del registro");
            cargarSitios();
        } catch (Exception ex) {
            ex.printStackTrace();
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "Sitios", "No se pudo eliminar el sitio");
        }
    }

    private void agregarMensaje(FacesMessage.Severity severidad, String titulo, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidad, titulo, detalle));
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public ArrayList<Sitio> getSitios() {
        return sitios;
    }

    public Sitio getSitioEdicion() {
        return sitioEdicion;
    }

    public void setSitioEdicion(Sitio sitioEdicion) {
        this.sitioEdicion = sitioEdicion;
    }

    public boolean isDsitio() {
        return dsitio;
    }

    public void setDsitio(boolean dsitio) {
        this.dsitio = dsitio;
    }

    public ArrayList<SelectItem> getGestores() {
        ArrayList<SelectItem> items = new ArrayList<>();
        items.add(new SelectItem("a", "MySQL"));
        items.add(new SelectItem("b", "Postgres-XL"));
        items.add(new SelectItem("c", "PostgreSQL"));
        items.add(new SelectItem("d", "MongoDB"));
        return items;
    }
}
