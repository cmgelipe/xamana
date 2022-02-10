/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.dhf.schema;

import java.util.ArrayList;
import java.util.List;
import static org.xamana.dhf.schema.BuildTypesPostgreSQL.type;

/**
 *
 * @author abryn
 */
public class BuildTypesMySQL {

    private static List<String> withValues;

    public static String determineTypes(String currentType, String value) {

        int index = withValues.indexOf(currentType);
        if (!value.equals("") && index >= 0) {
            return currentType + "(" + value + ")";
        }
        if (currentType.contains("ENUM")) {
            return currentType = "CHAR(1)";
        }
        return currentType;
    }

    public static String transformValue(String value, String type) {
        String valueTransform = value;
        switch (type) {
            case "CHAR":
            case "VARCHAR":
            case "BIT":
            case "TINYINT":
            case "DATE":
            case "TIME":
            case "TIMESTAMP":
            case "DATETIME":
            case "ENUM":
                valueTransform = "\"" + value + "\"";
                break;
            case "INTEGER":
            case "SMALLINT":
            case "BIGINT":
            case "NUMERIC":
            case "DECIMAL":
            case "FLOAT":
            case "DOUBLE":
                valueTransform = value;
                break;

        }
        return valueTransform.replaceAll("'", "");
    }

    static {
        withValues = new ArrayList<>();
        withValues.add("CHAR");
        withValues.add("VARCHAR");
        withValues.add("BINARY");
        withValues.add("VARBINARY");
        withValues.add("TEXT");
        withValues.add("BLOB");
        withValues.add("BIT");
        withValues.add("TINYINT");
        withValues.add("SMALLINT");
        withValues.add("MEDIUMINT");
        withValues.add("INT");
        withValues.add("INTEGER");
        withValues.add("BIGINT");
        withValues.add("FLOAT");
        withValues.add("DOUBLE");
        withValues.add("DOUBLE PRECISION");
        withValues.add("DECIMAL");
        withValues.add("DEC");

    }
}
