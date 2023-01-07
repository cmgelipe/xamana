/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.xamana.hybrid;

import java.util.ArrayList;
import java.util.List;
import org.primefaces.model.file.UploadedFiles;
import org.xamana.db.Atributo;
import org.xamana.dhf.model.Fragment;
import org.xamana.dhf.schema.Schema;
import org.xamana.verticalCBIR.model.TablaCosto;

/**
 *
 * @author pipe
 */
public class HybridDesign {
    double HVCost;
    double VHCost;
    ArrayList<HybridFragment> fragments;
    
    public HybridDesign(){
        fragments=new ArrayList<>();
    }
    
    public void mix(Schema schema, ArrayList<TablaCosto> esquemaVertical) {
        List<Fragment> hfs=schema.getSchema();
        for(Fragment hf:hfs){
            for(int i=0;i<esquemaVertical.size();i++){
                TablaCosto vf=esquemaVertical.get(i);
                HybridFragment newFragment=new HybridFragment();
                newFragment.setNombre(hf.getName()+"_"+i);
                newFragment.setPredicado(hf.getPredicate());
                newFragment.setAtts(vf.getArrAtri());
                newFragment.setSitioh(hf.getSite());
                newFragment.setSitiov(vf.getSitio());
                fragments.add(newFragment);
            }
        }
    }

    public void costs(UploadedFiles files) {
        
    }
    
}
