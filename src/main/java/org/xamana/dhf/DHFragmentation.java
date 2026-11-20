/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.dhf;

import org.xamana.dhf.model.MCRUD;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.primefaces.model.file.UploadedFiles;
import org.xamana.db.bd;
import org.xamana.dhf.FOP.FragmentationOperations;
import org.xamana.dhf.FOP.FragmentationOperationsCreator;
import org.xamana.dhf.schema.Schema;
import org.xamana.filter.Filter;
import org.xamana.filter.FilterCreator;
import org.xamana.log.LogReader;
import org.xamana.log.LogReaderCreator;

/**
 * Clase que encapsula todo el comportamiento para realizar una fragmentacion
 * horizontal dinamica
 *
 * @author abryn
 */
public class DHFragmentation implements Serializable {

    private LogReader logReader;
    private Filter filter;
    private FragmentationOperations FO;
    private BuilderMCRUD MCRUD;
    private List<MCRUD> alp;
    private Schema schema;
    AccessDataXamana xamana;
    private int percent;
    private long token;
    
    public DHFragmentation(bd bd) throws Exception {
        this.logReader = new LogReaderCreator().createLogReader(bd.getTipoBD(), bd.getDirbd());
        this.filter = new FilterCreator().createFilter(bd.getTipoBD(), bd.getDirbd());
        this.FO = FragmentationOperationsCreator.createFragmentationOperations(bd);
        this.MCRUD = new BuilderMCRUD();
        this.schema = new Schema(bd);
        this.xamana = new AccessDataXamana(bd);

    }

    public void implementMethod(UploadedFiles files, String database, String table) throws Exception {
        List<String> operations = this.logReader.determineOperations(files, database, table);
        this.FO.initialize();
        this.filter.setFO(FO);
        this.filter.determineOperations(operations);
        this.MCRUD.buildMCRUD(this.filter.getOperations());
        this.builderALP(this.MCRUD.getMCRUD());
        this.schema.buildDesignSchema(alp, this.MCRUD.getMCRUD(),
                this.FO.getCurrentTuples(),this.MCRUD.getTotalFrecuency(),this.MCRUD.getTotalPerformance());

    }

    private void builderALP(List<MCRUD> mcrud) {
        this.alp = new ArrayList<>();
        int index = -1;
        for (MCRUD item : mcrud) {
            index = this.alp.indexOf(item);
            if (index >= 0) {
                MCRUD currentItem = this.alp.get(index);
                currentItem.setCost(currentItem.getCost() + item.getCost());
            } else {
                MCRUD tempElement = new MCRUD(item.getCost(),
                        item.getPredicate(),
                        item.getAtribute(),
                        item.getSite());

                this.alp.add(tempElement);
            }

        }

    }

    public void buildSchema() throws Exception {
        this.percent =0;
        int unitPercent = 100 / (this.schema.getSchema().size() + 1);
        this.xamana.registerInfo();
        this.percent += unitPercent;
        this.xamana.registerSites(schema.getSchema());

        this.schema.buildSchema((fragment, attribute, type) -> {
          this.xamana.registerFragments(fragment, attribute, type);
          this.percent += unitPercent;
        });
        this.percent =100;
        this.token = xamana.getIdTable();
    }

    public Filter getFilterInfo() {
        return filter;
    }

    public BuilderMCRUD getMCRUD() {
        return MCRUD;
    }

    public List<MCRUD> getAlp() {
        return this.alp;
    }

    public Schema getSchema() {
        return this.schema;
    }

    public int getPercent() {
        return this.percent;
    }
    
    public long getToken(){
     return this.token;
    }


}
