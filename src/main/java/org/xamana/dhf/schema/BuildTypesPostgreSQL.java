package org.xamana.dhf.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author abryn
 */
public class BuildTypesPostgreSQL {

    private static Map<String, String> aliases;
    private static List<String> withValues;
    static String type = "";

    public static String determineTypes(String currentType, String value) {
        
        type = currentType;
        if (aliases.containsKey(currentType)) {
            type = aliases.get(currentType);
        }
        int index = withValues.indexOf(type);
        if (!value.equals("") && index >= 0) {
            return type + "(" + value + ")";
        }
        return type;
    }

    public static String transformValue(String value, String type) {
        String valueTransform = value;
        switch (type) {
            case "bigint":
            case "bit":
            case "integer":
                Double temp = Double.parseDouble(value);
                valueTransform = "" + temp.intValue();
                break;
        }

        return valueTransform;

    }

    static {
        aliases = new HashMap<>();
        aliases.put("int8", "bigint");
        aliases.put("serial8", "bigserial");
        aliases.put("varbit", "bit varying");
        aliases.put("bool", "boolean");
        aliases.put("char", "character");
        aliases.put("varchar", "character varying");
        aliases.put("float8", "double precision");
        aliases.put("int4", "integer");
        aliases.put("int", "integer");
        aliases.put("decimal", "numeric");
        aliases.put("float4", "real");
        aliases.put("int2", "smallint");
        aliases.put("serial2", "smallserial");
        aliases.put("serial4", "serial");
        aliases.put("timetz", "time");
        aliases.put("timestamptz", "timestamp");
    }

    static {
        withValues = new ArrayList<>();
        withValues.add("bit");
        withValues.add("bit varying");
        withValues.add("character");
        withValues.add("character varying");
        withValues.add("interval");
        withValues.add("time");
        withValues.add("timestamp");

    }

}
