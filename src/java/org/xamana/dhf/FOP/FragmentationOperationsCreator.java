/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.dhf.FOP;

import org.xamana.db.bd;

/**
 *
 * @author abryn
 */
public class FragmentationOperationsCreator {

    public static FragmentationOperations createFragmentationOperations(bd bd) {
        FragmentationOperations FO = null;
        switch (bd.getTipoBD()) {
            case "a": //MySQL
                FO = new FragmentationOperationsMySQL(bd);
                break;
            case "b": //Postgres-XL
                break;
            case "c": //PostgresSQL
                FO = new FragmentationOperationsPostgreSQL(bd);
                break;
            case "d": //MongoDB
                FO = new FragmentationOperationsMongo(bd);
                break;
        }
        return FO;

    }

}
