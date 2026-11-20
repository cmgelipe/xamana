package org.xamana.dbJB;

import java.io.Serializable;
import java.util.ArrayList;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import org.xamana.db.Sitio;
import org.xamana.db.SitioDAO;

/**
 * Bean JSF para la pantalla de configuración de sitios: registrar, editar
 * y eliminar la línea de conexión (puerto, base de datos, usuario y
 * contraseña) de cada sitio remoto, en vez de reutilizar la del sitio local.
 * El gestor (DBMS) de un sitio remoto nunca se elige: siempre es el mismo
 * gestor configurado para el sitio local, porque la fragmentación distribuida
 * de esta aplicación no mezcla motores distintos entre sitios.
 *
 * @author pipe
 */
@Named(value = "sitioBean")
@SessionScoped
public class SitioJB implements Serializable {

    @Inject
    private BDJB bd;

    private final SitioDAO dao = new SitioDAO();
    private ArrayList<Sitio> sitios = new ArrayList<>();
    private Sitio sitioEdicion = new Sitio();
    private boolean dsitio = false;
    private boolean dsitioForm = false;
    private boolean edicion = false;

    public void abrirRegistro() {
        cargarSitios();
        this.dsitioForm = false;
        this.dsitio = true;
    }

    public void cerrarRegistro() {
        this.dsitio = false;
        this.dsitioForm = false;
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
        copia.setGestor(getGestorLocal());
        copia.setUsuario(s.getUsuario());
        copia.setPassword(s.getPassword());
        this.sitioEdicion = copia;
        this.edicion = true;
        this.dsitioForm = true;
    }

    public void nuevo() {
        this.sitioEdicion = new Sitio();
        this.sitioEdicion.setGestor(getGestorLocal());
        this.edicion = false;
        this.dsitioForm = true;
    }

    public void cerrarFormulario() {
        this.dsitioForm = false;
    }

    public void guardar() {
        sitioEdicion.setGestor(getGestorLocal());
        if (isNullOrEmpty(sitioEdicion.getDireccion()) || isNullOrEmpty(sitioEdicion.getPuerto())
                || isNullOrEmpty(sitioEdicion.getBasedatos())) {
            agregarMensaje(FacesMessage.SEVERITY_WARN, "Sitios", "Dirección, puerto y base de datos son obligatorios");
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

    public boolean isDsitioForm() {
        return dsitioForm;
    }

    public void setDsitioForm(boolean dsitioForm) {
        this.dsitioForm = dsitioForm;
    }

    public String getFormHeader() {
        return edicion ? "Edit site" : "New site";
    }

    /**
     * El DBMS de un sitio remoto siempre es el mismo gestor configurado
     * para el sitio local: la fragmentación distribuida no mezcla motores
     * distintos entre sitios.
     */
    public String getGestorLocal() {
        String tipo = bd.getObj().getTipoBD();
        return tipo == null ? "" : tipo;
    }

    public String getGestorLocalLabel() {
        switch (getGestorLocal()) {
            case "a":
                return "MySQL";
            case "b":
                return "Postgres-XL";
            case "c":
                return "PostgreSQL";
            case "d":
                return "MongoDB";
            default:
                return "";
        }
    }
}
