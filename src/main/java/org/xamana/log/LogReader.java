/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.log;

import java.io.Serializable;
import java.util.List;
import org.primefaces.model.file.UploadedFiles;

/**
 * Clase que define el comportamiento para obtener las operaciones de un archivo
 * log
 *
 * @author abryn
 */
public abstract class LogReader implements Serializable {

    protected String addrees;

    public LogReader(String address) {
        this.addrees = address;
    }

    public abstract List<String> determineOperations(UploadedFiles files, String database, String table) throws Exception;


    public String getAddrees() {
        return addrees;
    }

}
