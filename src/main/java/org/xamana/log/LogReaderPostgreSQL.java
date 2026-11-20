/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author abryn
 */
public class LogReaderPostgreSQL extends LogReader implements Serializable {

    public LogReaderPostgreSQL(String address) {
        super(address);
    }

    @Override
    public List<String> determineOperations(UploadedFiles files, String database, String table) throws Exception {
        List<String> lines = new ArrayList<>();
        List<String> tempLines = new ArrayList<>();
        Pattern pattern;
        Matcher match;
        String nameDatabase = "";
        for (UploadedFile uf : files.getFiles()) {
            tempLines.addAll(IOUtils.readLines(uf.getInputStream(), "UTF-8"));
        }
        for (String line : tempLines) {
            String currentLine = line.replaceAll("\\s", "") + ";";

            if (currentLine.contains("conexionautorizada:")) {
                pattern = Pattern.compile("datos=(.*?)application_name=psql;");
                match = pattern.matcher(currentLine);
                if (match.find()) {
                    nameDatabase = "db:" + match.group(1) + ";";
                }
            }
            lines.add(currentLine + nameDatabase);

        }

        return lines.stream()
                .filter(l -> l.contains(database + "." + table) 
                        || l.contains("WHERE") && l.contains(table))
                .map(l -> {
                    return l.replaceAll("\"","'");
                })
                .collect(Collectors.toList());
    }

}
