/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.log;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author abryn
 */
public class LogReaderCreator implements Serializable{

    public LogReader createLogReader(String optionManager, String addrees) {
        LogReader logReader = null;

        switch (optionManager) {
            case "a": //MySQL
                logReader = new LogReaderMySQL(addrees);
                break;
            case "b": //Postgres-XL
                break;
            case "c": //PostgresSQL
                logReader = new LogReaderPostgreSQL(addrees);
                break;
            case "d": //MongoDB
                logReader = new LogReaderMongoDB(addrees);
                break;
        }

        return logReader;
    }

}
