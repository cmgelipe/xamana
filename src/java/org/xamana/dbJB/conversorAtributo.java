/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.xamana.dbJB;

import java.util.ArrayList;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 *
 * @author pipe
 */

public class conversorAtributo implements javax.faces.convert.Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        String uno=value.split(",")[0];
        String dos=value.split(",")[1];
        //tomarlo de bd.tabla.getAtributo;
        uno=uno.substring(1, uno.length());
        dos=dos.substring(0,dos.length()-1);
        return new Object();
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        return o.toString();
    }
    
}
