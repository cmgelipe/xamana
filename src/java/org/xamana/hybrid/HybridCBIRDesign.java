/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.xamana.hybrid;

import java.util.ArrayList;
import org.xamana.db.bd;
import org.xamana.db.esquemaHorMultimedia;
import org.xamana.verticalCBIR.model.TablaCosto;

/**
 *
 * @author pipe
 */
public class HybridCBIRDesign {
    private ArrayList<HybridFragment> fragments;
    private bd oBD;

    
    public HybridCBIRDesign(bd oBD) {
        fragments = new ArrayList<>();
        this.oBD = oBD;
    }
    
    public void mix(ArrayList<esquemaHorMultimedia> hfs, ArrayList<TablaCosto> esquemaVertical) {
        for (esquemaHorMultimedia hf : hfs) {
            for (int i = 0; i < esquemaVertical.size(); i++) {
                TablaCosto vf = esquemaVertical.get(i);
                HybridFragment newFragment = new HybridFragment();
                newFragment.setNombre(hf.getNombre() + "_" + i);
                newFragment.setPredicado(hf.getValor());
                newFragment.setAtts(vf.getArrAtri());
                newFragment.setSitioh(this.oBD.getDirbd());
                newFragment.setSitiov(vf.getSitio());
                getFragments().add(newFragment);
            }
        }
    }
   
    /**
     * @return the fragments
     */
    public ArrayList<HybridFragment> getFragments() {
        return fragments;
    }

    /**
     * @param fragments the fragments to set
     */
    public void setFragments(ArrayList<HybridFragment> fragments) {
        this.fragments = fragments;
    }

    
    /**
     * @return the oBD
     */
    public bd getoBD() {
        return oBD;
    }

    /**
     * @param oBD the oBD to set
     */
    public void setoBD(bd oBD) {
        this.oBD = oBD;
    }
    
    public ArrayList<HybridFragment> adaptarPredicadosMongo() {
        String atributo=this.oBD.getTabla().getAtributoMultimedia();
        for(HybridFragment hf:this.getFragments()){
            hf.setPredicado("{\""+atributo.split(":")[0]+"\":{$eq:\""+hf.getPredicado()+"\"}}");
        }
        return this.fragments;
    }

    public ArrayList<HybridFragment> adaptarPredicadosRelacional() {
        String atributo=this.oBD.getTabla().getAtributoMultimedia();
        for(HybridFragment hf:this.getFragments()){
            hf.setPredicado(atributo.split(":")[0]+"=\""+hf.getPredicado()+"\"");
        }
        return this.getFragments();
    }
    
}
