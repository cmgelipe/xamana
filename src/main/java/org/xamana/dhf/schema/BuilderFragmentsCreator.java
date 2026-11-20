/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.dhf.schema;

import org.xamana.db.bd;

/**
 *
 * @author abryn
 */
public class BuilderFragmentsCreator {

    public BuilderFraments createBuilderFragments(bd bd) {
        BuilderFraments BF = null;

        switch (bd.getTipoBD()) {
            case "a": //MySQL
                BF = new BuilderFragmentsMySQL(bd);
                break;
            case "b": //Postgres-XL
                break;
            case "c": //PostgresSQL
                BF = new BuilderFragmentsPostgreSQL(bd);
                break;
            case "d": //MongoDB
                BF = new BuilderFragmentsMongoDB(bd);
                break;
        }

        return BF;
    }


}
