/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.dbJB;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.servlet.ServletContext;
import org.bson.Document;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFiles;
import org.xamana.datos.AccesoDatos;
import org.xamana.datos.AccesoDatosPG;
import org.xamana.db.Atributo;
import org.xamana.db.bd;
import org.xamana.db.esquemaHorMultimedia;
import org.xamana.dhf.DHFragmentation;
import org.xamana.verticalCBIR.log.archivos;
import org.xamana.verticalCBIR.model.Query;
import org.xamana.verticalCBIR.model.TablaCosto;

/**
 *
 * @author pipe
 */

@ManagedBean(name = "bd")
@SessionScoped
public class BDJB implements Serializable {

    private bd obj = new bd();
    private boolean bconfig = false;
    private boolean bbotonfragmentar = false;
    private boolean bdatosincorrectos = false;
    private boolean blog = true;
    private boolean dcolumna = false;
    private boolean dcolumnas = false;
    private UploadedFiles files;
    private List<SelectItem> atributos;
    private ArrayList<esquemaHorMultimedia> valoresColumna;
    private boolean infoEsquema1 = true;
    private StreamedContent Descarga;
    String MensajeWARN = "";
    private boolean bToken;
    private String nombreNuevoFrag;
    private String seleccion1;
    private String seleccion2;
    private String seleccion3;
    private ArrayList<String> attsMultimediaDescriptor;
    private ArrayList<String> esquemaVerticalCBIR;
    private DHFragmentation DHF;
    private ArrayList<TablaCosto> esquemaVerticalCBIRPostgres;
    private boolean visible;
    ArrayList<TablaCosto> TablasCostos;
    private ArrayList<TablaCosto> esquemaVertical;
    ArrayList<Query> consultasV;
    ArrayList<TablaCosto> AsignacionSitio;
    
    /**
     * Creates a new instance of BD
     */
    public BDJB() {
        attsMultimediaDescriptor=new ArrayList<>();
    }

    public void configuracion() {
        bconfig = true;

    }

    public void cancelarConf() {
        bconfig = false;
    }

    public String probarConexion() {

        if (obj.conexionexterna()) {
            this.bbotonfragmentar = true;
            this.bconfig = false;
            this.bdatosincorrectos = false;
        } else {
            this.bbotonfragmentar = false;
            this.bconfig = false;
            this.bdatosincorrectos = true;
        }
        return "index.xhtml";
    }

    public void onCBIRchange() {
        try {
            if (this.obj.getTabla().isCBIR()
                    && this.obj.getTabla().getTipoFragment().compareTo("a") == 0) {
                if (this.obj.getTipoBD().compareTo("a") == 0) {
                    atributos = this.obj.listaAtributosMySQL();
                } else if (this.obj.getTipoBD().compareTo("b") == 0 || this.obj.getTipoBD().compareTo("c") == 0) {
                    atributos = this.obj.listaAtributosPostgreSQL();
                } else if (this.obj.getTipoBD().compareTo("d") == 0) {
                    atributos = this.obj.listaAtributosMongoDB();
                }
                this.dcolumna = true;
                this.blog = true;
            } else if(this.obj.getTabla().isCBIR()
                    && this.obj.getTabla().getTipoFragment().compareTo("b") == 0){
                if (this.obj.getTipoBD().compareTo("a") == 0) {
                    atributos=this.obj.listaAtributosMySQL();
                } else if (this.obj.getTipoBD().compareTo("b") == 0 || this.obj.getTipoBD().compareTo("c") == 0) {
                    atributos=this.obj.listaAtributosPostgreSQL();
                } else if (this.obj.getTipoBD().compareTo("d") == 0) {
                    atributos=this.obj.listaAtributosMongoDB();
                }
                
                this.dcolumnas=true;  
            } else {
                this.dcolumna = false;
                this.blog = false;
                this.dcolumnas=false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String btnesquema() {
        String tipoEsquema="";
        if (this.obj.getTabla().isCBIR()
                && this.obj.getTabla().getTipoFragment().compareTo("a") == 0) {
            try {
                valoresColumna = this.obj.valoresDistintosColMultimedia(files);
                if (this.getObj().getTipoBD().compareTo("a") == 0) {
                    this.Descarga = DefaultStreamedContent.builder()
                            .name("HorizontalMySQLCBIR.rar")
                            .contentType("application/x-rar-compressed")
                            .stream(() -> FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/descargas/HorizontalMySQLCBIR.rar"))
                            .build();
                } else if (this.getObj().getTipoBD().compareTo("b") == 0 || this.getObj().getTipoBD().compareTo("c") == 0) {
                    String ruta = ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getRealPath("/");
                    String a = stringArchivoA();
                    String b = stringArchivoB();
                    String c = stringArchivoC();
                    String d = stringArchivoD();
                    String e = stringArchivoE();
                    String f = stringArchivoF();
                    File existe = new File(ruta + "resources/descargas/FuncionDinamicaCrear.sql");
                    if (existe.exists()) {
                        existe.delete();
                    }
                    existe = new File(ruta + "resources/descargas/Crear.sql");
                    if (existe.exists()) {
                        existe.delete();
                    }
                    existe = new File(ruta + "resources/descargas/FuncionDinamicaEliminar.sql");
                    if (existe.exists()) {
                        existe.delete();
                    }
                    existe = new File(ruta + "resources/descargas/Eliminar.sql");
                    if (existe.exists()) {
                        existe.delete();
                    }
                    existe = new File(ruta + "resources/descargas/FuncionDinamicaModificar.sql");
                    if (existe.exists()) {
                        existe.delete();
                    }
                    existe = new File(ruta + "resources/descargas/Modificar.sql");
                    if (existe.exists()) {
                        existe.delete();
                    }
                    existe = new File(ruta + "resources/descargas/Archivos.zip");
                    if (existe.exists()) {
                        existe.delete();
                    }

                    RandomAccessFile ran = new RandomAccessFile(ruta + "resources/descargas/FuncionDinamicaCrear.sql", "rw");
                    ran.writeBytes(a);
                    ran.close();
                    ran = new RandomAccessFile(ruta + "resources/descargas/Crear.sql", "rw");
                    ran.writeBytes(b);
                    ran.close();
                    ran = new RandomAccessFile(ruta + "resources/descargas/FuncionDinamicaEliminar.sql", "rw");
                    ran.writeBytes(c);
                    ran.close();
                    ran = new RandomAccessFile(ruta + "resources/descargas/Eliminar.sql", "rw");
                    ran.writeBytes(d);
                    ran.close();
                    ran = new RandomAccessFile(ruta + "resources/descargas/FuncionDinamicaModificar.sql", "rw");
                    ran.writeBytes(e);
                    ran.close();
                    ran = new RandomAccessFile(ruta + "resources/descargas/Modificar.sql", "rw");
                    ran.writeBytes(f);
                    ran.close();

                    File w = new File(ruta + "resources/descargas/FuncionDinamicaCrear.sql");
                    InputStream u = new FileInputStream(w);
                    ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(new File(ruta + "resources/descargas/Archivos.zip"))));
                    zos.putNextEntry(new ZipEntry("FuncionDinamicaCrear.sql"));
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = u.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }
                    u.close();

                    w = new File(ruta + "resources/descargas/Crear.sql");
                    u = new FileInputStream(w);
                    zos.putNextEntry(new ZipEntry("Crear.sql"));
                    bytes = new byte[1024];
                    while ((length = u.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }
                    u.close();

                    w = new File(ruta + "resources/descargas/FuncionDinamicaEliminar.sql");
                    u = new FileInputStream(w);
                    zos.putNextEntry(new ZipEntry("FuncionDinamicaEliminar.sql"));
                    bytes = new byte[1024];
                    while ((length = u.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }
                    u.close();

                    w = new File(ruta + "resources/descargas/Eliminar.sql");
                    u = new FileInputStream(w);
                    zos.putNextEntry(new ZipEntry("Eliminar.sql"));
                    bytes = new byte[1024];
                    while ((length = u.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }
                    u.close();

                    w = new File(ruta + "resources/descargas/FuncionDinamicaModificar.sql");
                    u = new FileInputStream(w);
                    zos.putNextEntry(new ZipEntry("FuncionDinamicaModificar.sql"));
                    bytes = new byte[1024];
                    while ((length = u.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }
                    u.close();

                    w = new File(ruta + "resources/descargas/Modificar.sql");
                    u = new FileInputStream(w);
                    zos.putNextEntry(new ZipEntry("Modificar.sql"));
                    bytes = new byte[1024];
                    while ((length = u.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }
                    u.close();

                    zos.close();
                    this.Descarga = DefaultStreamedContent.builder()
                            .name("Archivos.zip")
                            .contentType("application/zip")
                            .stream(() -> FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/descargas/Archivos.zip"))
                            .build();
                } else if (this.getObj().getTipoBD().compareTo("d") == 0) {
                    this.Descarga = DefaultStreamedContent.builder()
                            .name("HorizontalMongoCBIR.rar")
                            .contentType("application/x-rar-compressed")
                            .stream(() -> FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/descargas/HorizontalMongoCBIR.rar"))
                            .build();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            tipoEsquema="esquema.xhtml";
        } else if (this.obj.getTabla().isCBIR()
                    && this.obj.getTabla().getTipoFragment().compareTo("b") == 0){
            //this.setEsquemaVerticalCBIR(this.obj.esquemaVertical());
            this.setEsquemaVerticalCBIRPostgres(this.obj.esquemaVerticalCBIRPostgres(getAttsMultimediaDescriptor(), this.files));
            this.setConsultasV(this.obj.ConsultasVerLog(this.files));
            this.setAsignacionSitio(this.obj.AsignacionSitio1(getAttsMultimediaDescriptor(), this.files));
            tipoEsquema="esquemaVerticalCBIR.xhtml";
        } else if(!this.obj.getTabla().isCBIR() 
                && this.obj.getTabla().getTipoFragment().compareTo("b") == 0){
           this.setEsquemaVertical(this.obj.esquemaVertical(this.files));
            this.setConsultasV(this.obj.ConsultasVerLog(this.files));
            this.setAsignacionSitio(this.obj.AsignacionSitio(this.files));
            tipoEsquema = "esquemaVertical.xhtml";
        }else if (!this.obj.getTabla().isCBIR() && this.obj.getTabla().getTipoFragment().equals("a")) {
            // Fragmentacion Horizontal Dinamica                
            try {
                DHF = new DHFragmentation(this.obj);
                DHF.implementMethod(files, obj.getNombd(), obj.getTabla().getNombre());
                this.infoEsquema1 = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            tipoEsquema= "esquemaFHD.xhtml";

        }
        return tipoEsquema;
    }

    public DHFragmentation getDHF() {
        return DHF;
    }
    
    public String configurarFragmentacion() {
        this.bbotonfragmentar = false;
        return "config.xhtml";
    }

    public void revisarDatos() {
        this.bdatosincorrectos = false;
    }

    /**
     * @return the bconfig
     */
    public boolean isBconfig() {
        return bconfig;
    }

    /**
     * @param bconfig the bconfig to set
     */
    public void setBconfig(boolean bconfig) {
        this.bconfig = bconfig;
    }

    /**
     * @return the bbotonfragmentar
     */
    public boolean isBbotonfragmentar() {
        return bbotonfragmentar;
    }

    /**
     * @param bbotonfragmentar the bbotonfragmentar to set
     */
    public void setBbotonfragmentar(boolean bbotonfragmentar) {
        this.bbotonfragmentar = bbotonfragmentar;
    }

    /**
     * @return the obj
     */
    public bd getObj() {
        return obj;
    }

    /**
     * @param obj the obj to set
     */
    public void setObj(bd obj) {
        this.obj = obj;
    }

    /**
     * @return the bdatosincorrectos
     */
    public boolean isBdatosincorrectos() {
        return bdatosincorrectos;
    }

    /**
     * @param bdatosincorrectos the bdatosincorrectos to set
     */
    public void setBdatosincorrectos(boolean bdatosincorrectos) {
        this.bdatosincorrectos = bdatosincorrectos;
    }

    /**
     * @return the files
     */
    public UploadedFiles getFiles() {
        return files;
    }

    /**
     * @param files the files to set
     */
    public void setFiles(UploadedFiles files) {
        this.files = files;
    }

    /**
     * @return the blog
     */
    public boolean isBlog() {
        return blog;
    }

    /**
     * @param blog the blog to set
     */
    public void setBlog(boolean blog) {
        this.blog = blog;
    }

    /**
     * @return the dcolumna
     */
    public boolean isDcolumna() {
        return dcolumna;
    }

    /**
     * @param dcolumna the dcolumna to set
     */
    public void setDcolumna(boolean dcolumna) {
        this.dcolumna = dcolumna;
    }

    /**
     * @return the atributos
     */
    public List<SelectItem> getAtributos() {
        return atributos;
    }

    /**
     * @param atributos the atributos to set
     */
    public void setAtributos(List<SelectItem> atributos) {
        this.atributos = atributos;
    }

    public void esquemaCBIRHorizontal() {
        this.dcolumna = false;
        this.dcolumnas=false;
    }

    /**
     * @return the valoresColumna
     */
    public ArrayList<esquemaHorMultimedia> getValoresColumna() {
        return valoresColumna;
    }

    /**
     * @param valoresColumna the valoresColumna to set
     */
    public void setValoresColumna(ArrayList<esquemaHorMultimedia> valoresColumna) {
        this.valoresColumna = valoresColumna;
    }

    /**
     * @return the infoEsquema1
     */
    public boolean isInfoEsquema1() {
        return infoEsquema1;
    }

    /**
     * @param infoEsquema1 the infoEsquema1 to set
     */
    public void setInfoEsquema1(boolean infoEsquema1) {
        this.infoEsquema1 = infoEsquema1;
    }

    public void fragEstatica() {
        if (this.obj.isHorizontalCBIR()) {
            boolean ban = false;
            for (esquemaHorMultimedia t : valoresColumna) {
                if (t.getNOperacionesAprox().compareTo("0") == 0) {
                    ban = true;
                }
            }
            if (ban) {
                this.MensajeWARN = "No se detectaron consultas en algún fragmento, se utilizará 1 como valor para realizar la primera "
                        + "fragmentación pero se recomienda no poner en marcha la fragmentación dinámica hasta que se detecten operaciones";
            }
            if (this.obj.getTipoBD().compareTo("a") == 0) {
                this.obj.fragmentarHorCBIRMySQL(valoresColumna);
            } else if (this.obj.getTipoBD().compareTo("b") == 0 || this.obj.getTipoBD().compareTo("c") == 0) {
                this.obj.fragmentarHorCBIRPostgres(valoresColumna);
            } else {
                this.obj.fragmentarHorCBIRMongoDB(valoresColumna);
            }

            this.setBToken(true);
        }else if(this.obj.isVerticalCBIR()){
            if (this.obj.getTipoBD().compareTo("a") == 0) {
                this.obj.fragmentarVerCBIRMySQL(this.esquemaVerticalCBIR);
            } else if (this.obj.getTipoBD().compareTo("b") == 0 || this.obj.getTipoBD().compareTo("c") == 0) {
                this.obj.setProgress1(0);
                this.visible = false;
                this.obj.fragmentarVerCBIRPostgres(this.AsignacionSitio);
                this.visible = true;
            } else {
                this.obj.fragmentarVerCBIRMongoDB(this.esquemaVerticalCBIR);
            }
        }else if (!this.obj.isHorizontalCBIR() && !this.obj.isVerticalCBIR()) {
            if (this.obj.getTipoBD().compareTo("b") == 0 || this.obj.getTipoBD().compareTo("c") == 0) {
                this.obj.setProgress1(0);
                this.visible = false;
                this.obj.fragmentarVerCBIRPostgres(this.AsignacionSitio);
                this.visible = true;
            }
        }
    }

    /**
     * @return the Descarga
     */
    public StreamedContent getDescarga() {
        return Descarga;
    }

    /**
     * @param Descarga the Descarga to set
     */
    public void setDescarga(StreamedContent Descarga) {
        this.Descarga = Descarga;
    }

    public void onComplete() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Fragmentación realizada", this.MensajeWARN));
    }

    /**
     * @return the bToken
     */
    public boolean isBToken() {
        return bToken;
    }

    /**
     * @param bToken the bToken to set
     */
    public void setBToken(boolean bToken) {
        this.bToken = bToken;
    }

    public String nuevasTuplas() {
        String ret = "";
        if (this.obj.getTipoBD().compareTo("a") == 0
                && this.obj.getTabla().getTipoFragment().compareTo("a") == 0
                && this.obj.getTabla().isCBIR()) {

            try {
                AccesoDatos a = new AccesoDatos(this.obj.getDirbd() + ":" + this.obj.getPortbd(), this.obj.getNombd(), this.obj.getUsubd(), this.obj.getPassbd());
                this.obj.listaAtributosMySQL();
                String att_tipo = "";
                for (Atributo r : this.obj.getTabla().getAtributos()) {
                    if (this.obj.transTipoDato(r.getTipo()) != 0) {
                        att_tipo += r.getNombre() + " " + r.getTipo() + ", ";
                    } else {
                        if (r.getTipo().compareTo("BIT") != 0) {
                            att_tipo += r.getNombre() + " " + r.getTipo() + "(" + r.getTamanio() + "), ";
                        } else {
                            att_tipo += r.getNombre() + " TINYINT, ";
                        }
                    }
                }
                att_tipo = att_tipo.substring(0, att_tipo.length() - 2);
                if (a.conectar()) {
                    a.ejecutarComando("create table " + this.nombreNuevoFrag + " (" + att_tipo + ");");
                    a.desconectar();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            ret = "NuevasTuplasHorMySQL.xhtml";
        } else if ((this.obj.getTipoBD().compareTo("b") == 0 || this.obj.getTipoBD().compareTo("c") == 0)
                && this.obj.getTabla().getTipoFragment().compareTo("a") == 0
                && this.obj.getTabla().isCBIR()) {
            try {
                AccesoDatosPG a = new AccesoDatosPG(this.obj.getDirbd() + ":" + this.obj.getPortbd(), this.obj.getNombd(), this.obj.getUsubd(), this.obj.getPassbd());
                System.out.println("contra" + this.obj.getPassbd());
                this.obj.getTabla().setAtributoLlave(this.obj.llavePostgres(a, this.obj.getNombd(), this.obj.getTabla().getNombre()));
                this.obj.conexionexterna();
                this.obj.listaAtributosPostgreSQL();
                String att_tipo = "";
                for (Atributo r : this.obj.getTabla().getAtributos()) {
                    if (this.obj.transTipoDato(r.getTipo()) != 0) {
                        if (r.getNombre().compareTo(this.obj.getTabla().getAtributoLlave().getNombre()) == 0) {
                            att_tipo += r.getNombre() + " " + r.getTipo() + " PRIMARY KEY, ";
                        } else {
                            att_tipo += r.getNombre() + " " + r.getTipo() + ", ";
                        }

                    } else {
                        if (r.getTipo().compareTo("BIT") != 0) {
                            if (r.getNombre().compareTo(this.obj.getTabla().getAtributoLlave().getNombre()) == 0) {
                                att_tipo += r.getNombre() + " " + r.getTipo() + "(" + r.getTamanio() + ") PRIMARY KEY, ";
                            } else {
                                att_tipo += r.getNombre() + " " + r.getTipo() + "(" + r.getTamanio() + "), ";
                            }
                        } else {
                            if (r.getNombre().compareTo(this.obj.getTabla().getAtributoLlave().getNombre()) == 0) {
                                att_tipo += r.getNombre() + " TINYINT PRIMARY KEY, ";
                            } else {
                                att_tipo += r.getNombre() + " TINYINT, ";
                            }
                        }
                    }
                }
                att_tipo = att_tipo.substring(0, att_tipo.length() - 2);
                if (a.conectar()) {
                    a.ejecutarComando("create table " + this.nombreNuevoFrag + " (" + att_tipo + ");");
                    a.desconectar();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            ret = "NuevasTuplasHorMySQL.xhtml";
        } else if (this.obj.getTipoBD().compareTo("d") == 0
                && this.obj.getTabla().getTipoFragment().compareTo("a") == 0
                && this.obj.getTabla().isCBIR()) {
            try {
                this.obj.conexionexterna();
                MongoDatabase db = this.obj.getAccexternomongo().getMongoClient().getDatabase(this.obj.getNombd());
                db.createCollection(this.nombreNuevoFrag);
                ret = "NuevasTuplasHorMySQL.xhtml";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * @return the nombreNuevoFrag
     */
    public String getNombreNuevoFrag() {
        return nombreNuevoFrag;
    }

    /**
     * @param nombreNuevoFrag the nombreNuevoFrag to set
     */
    public void setNombreNuevoFrag(String nombreNuevoFrag) {
        this.nombreNuevoFrag = nombreNuevoFrag;
    }

    public String confNuevoFrag() {
        System.out.println("contra" + this.obj.getNombd());
        return "confNuevoFragmento.xhtml";
    }

    String stringArchivoA() {
        ArrayList<String> ips = new ArrayList<>();
        for (String value : this.obj.getNombreIp().values()) {
            if (!ips.contains(value)) {
                ips.add(value);
            }
        }
        String a1 = "CREATE or replace FUNCTION public.trg_alter_test()\n"
                + "    RETURNS event_trigger\n"
                + "    LANGUAGE 'plpgsql'\n"
                + "    COST 100\n"
                + "    VOLATILE NOT LEAKPROOF\n"
                + "AS $BODY$\n"
                + "DECLARE\n"
                + "     obj record;\n"
                + "	 nActuales record;\n"
                + "	 nTablas name;\n"
                + "	 cActuales bigint;\n"
                + "	 cOtras bigint;\n"
                + "	 nombreAtributo name;\n"
                + "	 nombreAtributoFragmento name;\n"
                + "	 faltante record;\n"
                + "	 prueba record;\n"
                + "	 bandera boolean;\n"
                + "	 consulta text;\n"
                + "	 consultaRemota text;\n"
                + "	 dblinkExt bigint;\n"
                + "BEGIN\n"
                + "	dblinkExt=(select count(extname) from pg_extension where extname='dblink');\n"
                + "	if dblinkExt=0 then\n"
                + "		CREATE EXTENSION dblink;\n"
                + "	end if;\n"
                + "    FOR obj in SELECT * FROM pg_event_trigger_ddl_commands()\n"
                + "         LOOP\n"
                + "              IF obj.objid = '" + this.obj.getTabla().getNombre() + "'::regclass THEN \n"
                +//--tabla1 es nombre de la tabla que se fragmentó
                "			  	cActuales=(SELECT count(column_name) FROM information_schema.columns \n"
                + "						   WHERE table_schema = 'public' AND table_name='" + this.obj.getTabla().getNombre() + "');\n"
                + "				bandera=false;\n"
                + "                for nTablas in SELECT table_name FROM information_schema.tables \n"
                + "					WHERE table_schema='public' AND table_type='BASE TABLE' and table_name like '" + this.obj.getTabla().getNombre() + "_%'\n"
                + "					LOOP\n"
                + "					cOtras=(SELECT count(column_name) FROM information_schema.columns \n"
                + "						   WHERE table_schema = 'public' AND table_name=nTablas);\n"
                + "					IF cActuales>cOtras then\n"
                + "						for faltante in SELECT t0.column_name, t0.data_type \n"
                + "						FROM information_schema.columns as t0\n"
                + "						left join (SELECT column_name \n"
                + "	  						FROM information_schema.columns \n"
                + "							WHERE table_schema = 'public' \n"
                + "	  						AND table_name=nTablas) as t1\n"
                + "						on t0.column_name=t1.column_name\n"
                + "						WHERE table_schema = 'public' \n"
                + "						AND table_name='" + this.obj.getTabla().getNombre() + "'\n"
                + "						and t1 is null\n"
                + "						loop\n"
                + "							begin\n"
                + "								consulta='alter table '|| nTablas ||' add column '||faltante.column_name||' '||faltante.data_type;\n"
                + "								perform dblink_exec('dbname=" + this.obj.getNombd() + " user=" + this.obj.getUsubd() + " password=" + this.obj.getPassbd() + "',consulta);\n"
                +//Agregar ip local
                "								if bandera=false then\n"
                + "									consultaRemota='select crear('''||faltante.column_name||'::'||faltante.data_type||''')';	\n"
                + "									--perform dblink('dbname=Prueba1 user=postgres password=sunny_4teamo',consultaRemota) as tt;--agregar cada ip remota\n";
        String a2 = "";
        for (String r : ips) {
            a2 += "select crear into prueba from dblink('dbname=" + this.obj.getNombd() + " user=" + this.obj.getUsubd() + " password=" + this.obj.getPassbd() + " hostaddr=" + r + " port=5432',consultaRemota) as t1(crear int);\n";//--agregar cada ip remota
        }

        String a3 = "									bandera=true;\n"
                + "								end if;\n"
                + "								RAISE NOTICE '% %',faltante.column_name, faltante.data_type;\n"
                + "							end;\n"
                + "						end loop;\n"
                + "					end if;\n"
                + "						   RAISE NOTICE '% %',cOtras, nTablas::text;\n"
                + "					END LOOP;\n"
                + "				END IF;\n"
                + "         END LOOP;\n"
                + "END\n"
                + "$BODY$;\n"
                + "\n"
                + "CREATE EVENT TRIGGER test_trigger ON ddl_command_end WHEN TAG IN ('ALTER TABLE')\n"
                + "EXECUTE FUNCTION trg_alter_test();";
        return a1 + a2 + a3;
    }

    String stringArchivoB() {
        String b = "create or replace function crear(original varchar) RETURNS int as\n"
                + "$$\n"
                + "declare\n"
                + "	nTablas record;\n"
                + "	contador int;\n"
                + "	res2 varchar;\n"
                + "	res3 varchar;\n"
                + "	consulta text;\n"
                + "	dblinkExt bigint;\n"
                + "begin\n"
                + "	dblinkExt=(select count(extname) from pg_extension where extname='dblink');\n"
                + "	if dblinkExt=0 then\n"
                + "		CREATE EXTENSION dblink;\n"
                + "	end if;\n"
                + "	res2=(select split_part(original,'::',1));\n"
                + "	res3=(select split_part(original,'::',2));\n"
                + "	for nTablas in SELECT table_name FROM information_schema.tables \n"
                + "					WHERE table_schema='public' AND table_type='BASE TABLE' and table_name like '" + this.obj.getTabla().getNombre() + "_%'\n"
                + "		loop\n"
                + "			consulta='alter table '|| nTablas.table_name ||' add column '||res2||' '||res3;\n"
                + "			raise notice '%',consulta;\n"
                + "			perform dblink_exec('dbname=" + this.obj.getNombd() + " user=" + this.obj.getUsubd() + " password=" + this.obj.getPassbd() + "',consulta);\n"
                + "		end loop;\n"
                + "	raise notice '%-%',res2,res3;\n"
                + "	return 1;\n"
                + "end;\n"
                + "$$ language plpgsql;";
        return b;
    }

    String stringArchivoC() {
        ArrayList<String> ips = new ArrayList<>();
        for (String value : this.obj.getNombreIp().values()) {
            if (!ips.contains(value)) {
                ips.add(value);
            }
        }
        String a1 = "CREATE FUNCTION public.trg_alter_test_eliminar()\n"
                + "    RETURNS event_trigger\n"
                + "    LANGUAGE 'plpgsql'\n"
                + "    COST 100\n"
                + "    VOLATILE NOT LEAKPROOF\n"
                + "AS $BODY$\n"
                + "DECLARE\n"
                + "     obj record;\n"
                + "	 nActuales record;\n"
                + "	 nTablas name;\n"
                + "	 cActuales bigint;\n"
                + "	 cOtras bigint;\n"
                + "	 nombreAtributo name;\n"
                + "	 nombreAtributoFragmento name;\n"
                + "	 faltante record;\n"
                + "	 consulta text;\n"
                + "	 prueba record;\n"
                + "	 bandera boolean;\n"
                + "	 consultaRemota text;\n"
                + "	 dblinkExt bigint;\n"
                + "BEGIN\n"
                + "	dblinkExt=(select count(extname) from pg_extension where extname='dblink');\n"
                + "	if dblinkExt=0 then\n"
                + "		CREATE EXTENSION dblink;\n"
                + "	end if;\n"
                + "     FOR obj in SELECT * FROM pg_event_trigger_ddl_commands()\n"
                + "         LOOP\n"
                + "              IF obj.objid = '" + this.obj.getTabla().getNombre() + "'::regclass THEN \n"
                +//tabla1 es nombre de la tabla que se fragmentó
                "			  	cActuales=(SELECT count(column_name) FROM information_schema.columns \n"
                + "						   WHERE table_schema = 'public' AND table_name='" + this.obj.getTabla().getNombre() + "');\n"
                + "                bandera=false;\n"
                + "				for nTablas in SELECT table_name FROM information_schema.tables \n"
                + "					WHERE table_schema='public' AND table_type='BASE TABLE' and table_name like '" + this.obj.getTabla().getNombre() + "_%'\n"
                + "					LOOP\n"
                + "					cOtras=(SELECT count(column_name) FROM information_schema.columns \n"
                + "						   WHERE table_schema = 'public' AND table_name=nTablas);\n"
                + "					IF cActuales<cOtras then\n"
                + "						for faltante in select t0.column_name, t0.data_type \n"
                + "						from information_schema.columns as t0 \n"
                + "						left join (SELECT column_name \n"
                + "								   FROM information_schema.columns \n"
                + "								   WHERE table_schema = 'public' \n"
                + "								   AND table_name='" + this.obj.getTabla().getNombre() + "') as t1 \n"
                + "									on t0.column_name=t1.column_name \n"
                + "									where table_schema = 'public' AND table_name=nTablas and t1 is null\n"
                + "						loop\n"
                + "							begin\n"
                + "								consulta='alter table '|| nTablas ||' drop column '||faltante.column_name;\n"
                + "								perform dblink_exec('dbname=" + this.obj.getNombd() + " user=" + this.obj.getUsubd() + " password=" + this.obj.getPassbd() + "',consulta);\n"
                + //--sitio principal
                "								if bandera=false then\n"
                + "									consultaRemota='select eliminar('''||faltante.column_name||''')';\n";
        String a2 = "";
        for (String f : ips) {
            a2 += "									select eliminar into prueba from dblink('dbname=" + this.obj.getNombd() + " user=" + this.obj.getUsubd() + " password=" + this.obj.getPassbd() + " hostaddr=" + f + "',consultaRemota) as t1(eliminar int);--agregar cada ip remota\n";
        }

        String a3 = "									bandera=true;\n"
                + "								end if;\n"
                + "								RAISE NOTICE '% %',faltante.column_name, faltante.data_type;\n"
                + "							end;\n"
                + "						end loop;\n"
                + "					end if;\n"
                + "						   RAISE NOTICE '% %',cOtras, nTablas::text;\n"
                + "					END LOOP;\n"
                + "				END IF;\n"
                + "         END LOOP;\n"
                + "END\n"
                + "$BODY$;\n"
                + "\n"
                + "CREATE EVENT TRIGGER test_trigger_eliminar ON ddl_command_end WHEN TAG IN ('ALTER TABLE')\n"
                + "EXECUTE FUNCTION trg_alter_test_eliminar();";
        return a1 + a2 + a3;
    }

    String stringArchivoD() {
        String D = "create or replace function eliminar(original varchar) RETURNS int as\n"
                + "$$\n"
                + "declare\n"
                + "	nTablas record;\n"
                + "	contador int;\n"
                + "	consulta text;\n"
                + "	dblinkExt bigint;\n"
                + "begin\n"
                + "	dblinkExt=(select count(extname) from pg_extension where extname='dblink');\n"
                + "	if dblinkExt=0 then\n"
                + "		CREATE EXTENSION dblink;\n"
                + "	end if;\n"
                + "	for nTablas in SELECT table_name FROM information_schema.tables \n"
                + "					WHERE table_schema='public' AND table_type='BASE TABLE' and table_name like '" + this.obj.getTabla().getNombre() + "_%'\n"
                + "		loop\n"
                + "			consulta='alter table '|| nTablas.table_name ||' drop column '||original;\n"
                + "			perform dblink_exec('dbname=" + this.obj.getNombd() + " user=" + this.obj.getUsubd() + " password=" + this.obj.getPassbd() + "',consulta);\n"
                + "		end loop;\n"
                + "	return 1;\n"
                + "end;\n"
                + "$$ language plpgsql;";
        return D;
    }

    String stringArchivoE() {
        ArrayList<String> ips = new ArrayList<>();
        for (String value : this.obj.getNombreIp().values()) {
            if (!ips.contains(value)) {
                ips.add(value);
            }
        }
        String a1 = "CREATE FUNCTION public.trg_alter_test_modificar()\n"
                + "    RETURNS event_trigger\n"
                + "    LANGUAGE 'plpgsql'\n"
                + "    COST 100\n"
                + "    VOLATILE NOT LEAKPROOF\n"
                + "AS $BODY$\n"
                + "DECLARE\n"
                + "     obj record;\n"
                + "	 nActuales record;\n"
                + "	 nTablas name;\n"
                + "	 cActuales bigint;\n"
                + "	 cOtras bigint;\n"
                + "	 nombreAtributo name;\n"
                + "	 nombreAtributoFragmento name;\n"
                + "	 attOriginal record;\n"
                + "	 attOtro record;\n"
                + "	 consulta text;\n"
                + "	 con1 int;\n"
                + "	 con2 int;\n"
                + "	 prueba record;\n"
                + "	 consultaRemota text;\n"
                + "	 bandera boolean;\n"
                + "	 dblinkExt bigint;\n"
                + "BEGIN\n"
                + "	dblinkExt=(select count(extname) from pg_extension where extname='dblink');\n"
                + "	if dblinkExt=0 then\n"
                + "		CREATE EXTENSION dblink;\n"
                + "	end if;\n"
                + "    FOR obj in SELECT * FROM pg_event_trigger_ddl_commands()\n"
                + "         LOOP\n"
                + "              IF obj.objid = '" + this.obj.getTabla().getNombre() + "'::regclass THEN \n"//--tabla1 es nombre de la tabla que se fragmentó
                + "			  	cActuales=(SELECT count(column_name) FROM information_schema.columns \n"
                + "						   WHERE table_schema = 'public' AND table_name='" + this.obj.getTabla().getNombre() + "');\n"
                + "				bandera=false;\n"
                + "                for nTablas in SELECT table_name FROM information_schema.tables \n"
                + "					WHERE table_schema='public' AND table_type='BASE TABLE' and table_name like '" + this.obj.getTabla().getNombre() + "_%'\n"
                + "					LOOP\n"
                + "					cOtras=(SELECT count(column_name) FROM information_schema.columns \n"
                + "						   WHERE table_schema = 'public' AND table_name=nTablas);\n"
                + "					IF cActuales=cOtras then\n"
                + "					con1=0;\n"
                + "						for attOriginal in select t0.column_name, t0.data_type\n"
                + "											from information_schema.columns as t0 \n"
                + "											where table_schema = 'public' AND table_name='" + this.obj.getTabla().getNombre() + "' \n"
                + "						loop\n"
                + "							con1=con1+1;\n"
                + "							con2=0;\n"
                + "							for attOtro in select t0.column_name, t0.data_type\n"
                + "											from information_schema.columns as t0 \n"
                + "											where table_schema = 'public' AND table_name=nTablas\n"
                + "							loop\n"
                + "								con2=con2+1;\n"
                + "								if con2=con1 then\n"
                + "									if attOtro.column_name<>attOriginal.column_name then \n"
                + "										--RAISE NOTICE 'DIFERENTE % %',attOtro.column_name, attOriginal.column_name;\n"
                + "										consulta='alter table '|| nTablas ||' rename column '||attOtro.column_name||' to '||attOriginal.column_name;\n"
                + "										perform dblink_exec('dbname=" + this.obj.getNombd() + " user=" + this.obj.getUsubd() + " password=" + this.obj.getPassbd() + "',consulta);	\n"
                + "										if bandera=false then\n"
                + "											consultaRemota='select modificar('''||attOtro.column_name||'::'||attOriginal.column_name||''',1)';\n";
        String a2 = "";
        for (String f : ips) {
            a2 += "											select modificar into prueba from dblink('dbname=" + this.obj.getNombd() + " user=" + this.obj.getUsubd() + " password=" + this.obj.getPassbd() + " hostaddr=" + f + "',consultaRemota) as t1(modificar int);\n";//--agregar cada ip remota
        }

        String a3 = "											bandera=true;\n"
                + "										end if;\n"
                + "									end if;\n"
                + "									if attOtro.data_type<>attOriginal.data_type then\n"
                + "										RAISE NOTICE 'DIFERENTE % %',attOtro.data_type, attOriginal.data_type;\n"
                + "										consulta='alter table '|| nTablas||' alter column '||attOtro.column_name||' set data type '||attOriginal.data_type;\n"
                + "										perform dblink_exec('dbname=" + this.obj.getNombd() + " user=" + this.obj.getUsubd() + " password=" + this.obj.getPassbd() + "',consulta);		\n"
                + "										if bandera=false then\n"
                + "											consultaRemota='select modificar('''||attOtro.column_name||'::'||attOriginal.data_type||''',2)';\n";
        String a4 = "";
        for (String f : ips) {
            a4 += "											select modificar into prueba from dblink('dbname=" + this.obj.getNombd() + " user=" + this.obj.getUsubd() + " password=" + this.obj.getPassbd() + " hostaddr=" + f + "',consultaRemota) as t1(modificar int);\n";//--agregar cada ip remota
        }
        String a5 = "											bandera=true;\n"
                + "										end if;\n"
                + "									end if;\n"
                + "								end if;\n"
                + "							end loop;\n"
                + "						end loop;\n"
                + "					end if;\n"
                + "					END LOOP;\n"
                + "				END IF;\n"
                + "         END LOOP;\n"
                + "END\n"
                + "$BODY$;\n"
                + "\n"
                + "CREATE EVENT TRIGGER test_trigger_modificar ON ddl_command_end WHEN TAG IN ('ALTER TABLE')\n"
                + "EXECUTE FUNCTION trg_alter_test_modificar();";
        return a1 + a2 + a3+a4+a5;
    }

    String stringArchivoF() {
        String f = "create or replace function modificar(original varchar,modo int) RETURNS int as\n"
                + "$$\n"
                + "declare\n"
                + "	nTablas record;\n"
                + "	contador int;\n"
                + "	res2 varchar;\n"
                + "	res3 varchar;\n"
                + "	consulta text;\n"
                + "	dblinkExt bigint;\n"
                + "begin\n"
                + "	dblinkExt=(select count(extname) from pg_extension where extname='dblink');\n"
                + "	if dblinkExt=0 then\n"
                + "		CREATE EXTENSION dblink;\n"
                + "	end if;\n"
                + "	res2=(select split_part(original,'::',1));\n"
                + "	res3=(select split_part(original,'::',2));\n"
                + "	for nTablas in SELECT table_name FROM information_schema.tables \n"
                + "					WHERE table_schema='public' AND table_type='BASE TABLE' and table_name like '"+this.obj.getTabla().getNombre()+"_%'\n"
                + "		loop\n"
                + "			if modo=1 then\n"
                + "				consulta='alter table '|| nTablas.table_name ||' rename column '||res2||' to '||res3;\n"
                + "				perform dblink_exec('dbname="+this.obj.getNombd()+" user="+this.obj.getUsubd()+" password="+this.obj.getPassbd()+"',consulta);\n"
                + "			end if;\n"
                + "			if modo=2 then\n"
                + "				consulta='alter table '|| nTablas.table_name ||' alter column '||res2||' set data type '||res3;\n"
                + "				perform dblink_exec('dbname="+this.obj.getNombd()+" user="+this.obj.getUsubd()+" password="+this.obj.getPassbd()+"',consulta);\n"
                + "			end if;\n"
                + "		end loop;\n"
                + "	raise notice '%-%',res2,res3;\n"
                + "	return 1;\n"
                + "end;\n"
                + "$$ language plpgsql;";
        return f;
    }

    /**
     * @return the seleccion
     */
    public String getSeleccion1() {
        return seleccion1;
    }

    /**
     * @param seleccion the seleccion to set
     */
    public void setSeleccion1(String seleccion) {
        this.seleccion1 = seleccion;
    }

    /**
     * @return the attsMultimediaDescriptor
     */
    public ArrayList<String> getAttsMultimediaDescriptor() {
        return attsMultimediaDescriptor;
    }

    /**
     * @param attsMultimediaDescriptor the attsMultimediaDescriptor to set
     */
    public void setAttsMultimediaDescriptor(ArrayList<String> attsMultimediaDescriptor) {
        this.attsMultimediaDescriptor = attsMultimediaDescriptor;
    }

    /**
     * @return the dcolumnas
     */
    public boolean isDcolumnas() {
        return dcolumnas;
    }

    /**
     * @param dcolumnas the dcolumnas to set
     */
    public void setDcolumnas(boolean dcolumnas) {
        this.dcolumnas = dcolumnas;
    }

    /**
     * @return the seleccion2
     */
    public String getSeleccion2() {
        return seleccion2;
    }

    /**
     * @param seleccion2 the seleccion2 to set
     */
    public void setSeleccion2(String seleccion2) {
        this.seleccion2 = seleccion2;
    }
    public void agregarAtts(){
        this.attsMultimediaDescriptor.add(seleccion1+","+seleccion2);
    }
    public void eliminarAtts(){
        this.attsMultimediaDescriptor.removeIf(x->(x.compareTo(this.seleccion3)==0));
    }

    /**
     * @return the seleccion3
     */
    public String getSeleccion3() {
        return seleccion3;
    }

    /**
     * @param seleccion3 the seleccion3 to set
     */
    public void setSeleccion3(String seleccion3) {
        this.seleccion3 = seleccion3;
    }

    /**
     * @return the esquemaVerticalCBIR
     */
    public ArrayList<String> getEsquemaVerticalCBIR() {
        return esquemaVerticalCBIR;
    }

    /**
     * @param esquemaVerticalCBIR the esquemaVerticalCBIR to set
     */
    public void setEsquemaVerticalCBIR(ArrayList<String> esquemaVerticalCBIR) {
        this.esquemaVerticalCBIR = esquemaVerticalCBIR;
    }

    public ArrayList<TablaCosto> getTablasCostos() {
        return TablasCostos;
    }

    public void setTablasCostos(ArrayList<TablaCosto> TablasCostos) {
        this.TablasCostos = TablasCostos;
    }

    public ArrayList<TablaCosto> getEsquemaVerticalCBIRPostgres() {
        return esquemaVerticalCBIRPostgres;
    }
    public ArrayList<TablaCosto> getAsignacionSitio() {
        return AsignacionSitio;
    }

    public void setAsignacionSitio(ArrayList<TablaCosto> AsignacionSitio) {
        this.AsignacionSitio = AsignacionSitio;
    }

    public ArrayList<Query> getConsultasV() {
        return consultasV;
    }

    public void setConsultasV(ArrayList<Query> consultasV) {
        this.consultasV = consultasV;
    }

    public ArrayList<TablaCosto> getEsquemaVertical() {
        return esquemaVertical;
    }

    public void setEsquemaVertical(ArrayList<TablaCosto> esquemaVertical) {
        this.esquemaVertical = esquemaVertical;
    }
    /**
     * @param esquemaVerticalCBIR the esquemaVerticalCBIR to set
     */
    public void setEsquemaVerticalCBIRPostgres(ArrayList<TablaCosto> esquemaVerticalCBIR) {
        this.esquemaVerticalCBIRPostgres = esquemaVerticalCBIR;
    }
}
