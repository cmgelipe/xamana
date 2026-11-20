/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.dhf.schema;

import org.xamana.dhf.model.Fragment;
import org.xamana.dhf.model.MCRUD;

/**
 *
 * @author abryn
 */
public interface Register {

    void fragment(Fragment fragment,MCRUD attribute,String type) throws Exception;

}
