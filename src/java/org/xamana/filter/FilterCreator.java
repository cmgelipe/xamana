/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.filter;

import java.io.Serializable;

/**
 *
 * @author abryn
 */
public class FilterCreator implements Serializable {

    public Filter createFilter(String optionManager,String site) {
        Filter filter = null;

        switch (optionManager) {
            case "a": //MySQL
                filter = new FilterMySQL(site);
                break;
            case "b": //Postgres-XL
                break;
            case "c": //PostgresSQL
                filter = new FilterPostgreSQL(site);
                break;
            case "d": //MongoDB
                filter = new FilterMongoDB(site);
                break;
        }

        return filter;

    }

}
