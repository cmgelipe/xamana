/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.dhf.model;

/**
 * Clase que encapsula toda la informacion relacionada a un fragmento de un
 * esquema propuesto obtenido a partir de un log
 *
 * @author abryn
 */
public class Fragment {

    private String name;
    private String predicate;
    private String site;
    private long tuples;
    private String percent;
    private double approximateOperations;
    private double performance;
    private String predicadoTraslape;

    public Fragment() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public long getTuples() {
        return tuples;
    }

    public void setTuples(long tuples) {
        this.tuples = tuples;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public double getApproximateOperations() {
        return approximateOperations;
    }

    public void setApproximateOperations(double approximateOperations) {
        this.approximateOperations = approximateOperations;
    }

    public double getPerformance() {
        return performance;
    }

    public void setPerformance(double performance) {
        this.performance = performance;
    }

    
    @Override
    public String toString() {
      return "{name:"+this.name+",predicate:"+this.predicate+""
              + ",site:"+this.site+", tuples"+this.tuples+"}";
    }

    /**
     * @return the predicadoTraslape
     */
    public String getPredicadoTraslape() {
        return predicadoTraslape;
    }

    /**
     * @param predicadoTraslape the predicadoTraslape to set
     */
    public void setPredicadoTraslape(String predicadoTraslape) {
        this.predicadoTraslape = predicadoTraslape;
    }
      
}
