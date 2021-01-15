package org.xamana.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Accumulators.sum;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.xamana.datos.AccesoDatos;
import org.xamana.datos.AccesoDatosMG;
import org.xamana.datos.AccesoDatosPG;
import static com.mongodb.client.model.Aggregates.*;
import java.util.Arrays;
import org.json.JSONObject;

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

    public List<SelectItem> listaAtributosMySQL() throws Exception {
        ArrayList<String> arr;
        List<SelectItem> res = new ArrayList<>();
        accexternomysql = new AccesoDatos(dirbd, nombd, usubd, passbd);
        if (accexternomysql.conectar()) {
            arr = accexternomysql.atributos(nombd, this.getTabla().getNombre());
            accexternomysql.desconectar();
            if (arr != null) {
                arr.forEach((g) -> {
                    res.add(new SelectItem(g, g));
                });
            }
        } else {
            System.out.println("Conexión incorrecta listaatributosmysql");
        }
        return res;
    }

    public List<SelectItem> listaAtributosMongoDB() throws Exception {
        ArrayList<String> arr;
        List<SelectItem> res = new ArrayList<>();
        //this.accexternomongo = new AccesoDatosMG(dirbd, nombd, this.tabla.getNombre(), usubd, passbd);
        if (this.accexternomongo.conectar()) {
            arr = this.accexternomongo.atributos(nombd, this.getTabla().getNombre());
            arr.forEach((h) -> {
                res.add(new SelectItem(h, h));
            });
        }
        return res;
    }

    public List<SelectItem> listaAtributosPostgreSQL() throws Exception {
        ArrayList<String> arr;
        List<SelectItem> res = new ArrayList<>();
        //this.accexternopost=new AccesoDatosPG(dirbd, nombd, usubd, passbd);
        if(this.accexternopost.conectar()){
            arr=this.accexternopost.atributos(nombd, this.getTabla().getNombre());
            this.accexternopost.desconectar();
             arr.forEach((h) -> {
                res.add(new SelectItem(h, h));
            });
        }
        return res;
    }

    public ArrayList<esquemaHorMultimedia> valoresDistintosColMultimedia() throws Exception {
        ArrayList<esquemaHorMultimedia> res=null;
        ArrayList arr=null;
        String nombreColumna=this.getTabla().getAtributoMultimedia().split(":")[0];
        String queryRelacional="select "+nombreColumna+", count("+nombreColumna+") as num from "+this.tabla.getNombre()+" group by "+nombreColumna;
        if(this.TipoBD.compareTo("a")==0){
            if(this.accexternomysql.conectar()){
                arr=this.accexternomysql.ejecutarConsulta(queryRelacional);
                this.accexternomysql.desconectar();
            }
            if(arr!=null){
                res=new ArrayList<>();
                for(int t=0;t<arr.size();t++){
                    esquemaHorMultimedia tupla=new esquemaHorMultimedia();
                    tupla.setNombre(this.tabla.getNombre()+"_f"+(t+1));
                    tupla.setValor(""+((ArrayList)arr.get(t)).get(0));
                    tupla.setNTuplas(""+((ArrayList)arr.get(t)).get(1));
                    res.add(tupla);
                }
            }
        }else if(this.TipoBD.compareTo("b")==0 || this.TipoBD.compareTo("c")==0){
            if(this.accexternopost.conectar()){
                arr=this.accexternopost.ejecutarConsulta(queryRelacional);
                this.accexternopost.desconectar();
            }
            if(arr!=null){
                res=new ArrayList<>();
                for(int t=0;t<arr.size();t++){
                    esquemaHorMultimedia tupla=new esquemaHorMultimedia();
                    tupla.setNombre(this.tabla.getNombre()+"_f"+(t+1));
                    tupla.setValor(""+((ArrayList)arr.get(t)).get(0));
                    tupla.setNTuplas(""+((ArrayList)arr.get(t)).get(1));
                    res.add(tupla);
                }
            }
        }else{
            if(this.accexternomongo.conectar()){
                MongoDatabase db=this.accexternomongo.getMongoClient().getDatabase(nombd);
                MongoCollection<Document> colec=db.getCollection(this.tabla.getNombre());
                Bson group=group("$"+nombreColumna,sum("num",1L));
                List<Document> r=colec.aggregate(Arrays.asList(group)).into(new ArrayList<>());
                if(!r.isEmpty()){
                    res=new ArrayList<>();
                    int t=0;
                    for(Document d:r){
                        t++;
                        esquemaHorMultimedia tupla=new esquemaHorMultimedia();
                        tupla.setNombre(this.tabla.getNombre()+"_f"+(t));
                        tupla.setValor(""+(new JSONObject(d.toJson())).get("_id"));
                        tupla.setNTuplas(""+(new JSONObject(d.toJson())).get("num"));
                        res.add(tupla);
                    }
                }
            }
        }
        return res;
    }
    public boolean isHorizontalCBIR(){
        return this.tabla.isCBIR() && this.tabla.getTipoFragment().compareTo("a")==0;
    }
    public boolean isRelacional(){
        return this.TipoBD.compareTo("a")==0 || this.TipoBD.compareTo("b")==0 || this.TipoBD.compareTo("c")==0;
    }

    public void guardarDatos() {
        try {
            AccesoDatos acc=new AccesoDatos();
            if(acc.conectar()){
                
                acc.desconectar();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void fragmentarHorCBIRMySQL(){
        
    }

    public void fragmentarHorCBIRPostgres() {
        
    }

    public void fragmentarHorCBIRMongoDB() {
        
    }

}
