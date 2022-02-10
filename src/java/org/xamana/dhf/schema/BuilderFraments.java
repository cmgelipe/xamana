/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.dhf.schema;

import java.util.List;
import org.xamana.dhf.model.Fragment;
import org.xamana.dhf.model.MCRUD;

/**
 *
 * @author abryn
 */
public interface BuilderFraments {
   
   List<Fragment> buildDesignSchema(List<MCRUD> mcrud, MCRUD item)throws Exception;
   
   void buildSchema(List<Fragment> schema, Register register)throws Exception;

}
