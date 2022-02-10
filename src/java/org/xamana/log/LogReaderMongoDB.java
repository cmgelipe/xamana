/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import org.primefaces.shaded.commons.io.IOUtils;

/**
 *
 * @author abryn
 */
public class LogReaderMongoDB extends LogReader implements Serializable {

    public LogReaderMongoDB(String address) {
        super(address);
    }

    @Override
    public List<String> determineOperations(UploadedFiles files, String database, String table) throws Exception {
        List<String> lines = new ArrayList<>();
        for (UploadedFile uf : files.getFiles()) {
            lines.addAll(IOUtils.readLines(uf.getInputStream(), "UTF-8"));
        }

        return lines.stream()
                .filter(l -> l.contains(database + "." + table))
                .map(l -> {
                    return l.replaceAll("\"", "");
                })
                .collect(Collectors.toList());
    }

}
