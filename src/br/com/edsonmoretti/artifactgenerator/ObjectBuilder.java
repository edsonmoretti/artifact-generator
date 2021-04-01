/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.edsonmoretti.artifactgenerator;

import java.io.File;
import java.io.PrintWriter;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/**
 *
 * @author Edson Moretti
 */
public class ObjectBuilder {

    public String build(String table) {
        return this.build(table, null);
    }

    public String build(String table, File directory) {
        System.out.println("Opning connection...");
        java.sql.Connection con = DatabaseConnection.getConnection();
        String attributes = "";
        String getAndSets = "";
        try {
            DatabaseMetaData meta = con.getMetaData();
            ResultSet res = meta.getColumns(null, null, table, null);

            boolean containBigDecimal = false;
            boolean containBigInteger = false;
            boolean containDate = false;
            System.out.println("Find table...");
            while (res.next()) {
                String attribute = getObjectAttributeNameGenerator(res.getString("COLUMN_NAME"));
                String mysqlDataType = res.getString("TYPE_NAME");
                String comment = "Tamanho: " + res.getString("COLUMN_SIZE") + ("0".equals(res.getString("NULLABLE")) ? " - NULLABLE" : "");
                if (mysqlDataType.toUpperCase().contains("UNSIGNED")) {
                    mysqlDataType = mysqlDataType.toUpperCase().replace("UNSIGNED", "").trim();
                }
                String type = getDataType(mysqlDataType);
                if (!containBigDecimal) {
                    containBigDecimal = type.equals("BigDecimal");
                }
                if (!containBigInteger) {
                    containBigInteger = type.equals("BigInteger");
                }
                if (!containDate) {
                    containDate = type.equals("Date");
                }
                attributes += ("    private " + type + " " + attribute + ";" + " /* " + comment + " */\n");
                String metodeName = String.valueOf(attribute.charAt(0)).toUpperCase() + attribute.substring(1);
                getAndSets += ("    public " + type + " get" + metodeName + "() {\n"
                        + "        return this." + attribute + ";\n"
                        + "    }\n"
                        + "\n");
                getAndSets += ("    public void set" + metodeName + "(" + type + " " + attribute + ") {\n"
                        + "        this." + attribute + " = " + attribute + ";\n"
                        + "    }\n"
                        + "\n");
            }
            res.close();
//            con.close();
            System.out.println("Generate string...");
            String packageName = Configuration.packageName + ";\n\n";
            String imports = (containBigDecimal ? "import java.math.BigDecimal;\n" : "") + (containBigInteger ? "import java.math.BigInteger;\n" : "") + (containDate ? "import java.util.Date;\n" : "") + (containBigInteger || containBigDecimal || containDate ? "\n" : "");
            String className = getObjectNameGenerator(table);
            String returnS = ("/*\n"
                    + " * Generated by Artfact Generator.\n"
                    + " * Written by Edson Moretti\n"
                    + " * Available in https://github.com/edsonmoretti/artifact-generator.\n"
                    + " */\n");
            returnS += "package " + packageName;
            returnS += imports;
            returnS += "/**\n"
                    + " *\n"
                    + " * @author Edson Moretti\n"
                    + " */\n";
            returnS += "public class " + className + " {\n\n";
            returnS += attributes;
            returnS += "\n";
            returnS += getAndSets;

            returnS += "}";

            if (directory == null) {
                directory = new File("src" + File.separator + (Configuration.packageName.replace(".", File.separator)));
            }

            try {
                directory.mkdirs();
                if (!directory.isDirectory()) {
                    throw new Exception("This object (directory) is not a Directory valid!!!");
                }
                File f = null;
                PrintWriter pw = new PrintWriter(f = new File(directory.getAbsolutePath() + File.separator + className + ".java"));
                pw.print(returnS);
                pw.close();
                System.out.println("Generate file: " + f.getAbsolutePath());
            } catch (Exception e) {
                System.out.println("erro: " + e.getMessage());
                e.printStackTrace();
            }

            return returnS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getDataType(String name) {
        switch (name.toUpperCase()) {
            case "DECIMAL":
            case "FLOAT":
                return "BigDecimal";
            case "BIGINT":
                return "BigInteger";
            case "INT":
            case "MEDIUMINT":
            case "SMALLINT":
                return "int";
            case "BOOL":
            case "BOOLEAN":
            case "TINYINT":
                return "booelan";
            case "BLOB":
            case "MEDIUMBLOB":
            case "TINYBLOB":
                return "byte[]";
            case "DATE":
            case "DATETIME":
            case "TIMESTAMP":
                return "Date";
            case "TIME":
            case "MEDIUMTEXT":
            case "LONGTEXT":
            case "VARCHAR":
            default:
                return "String";
        }
    }

    private String getObjectNameGenerator(String name) {
        String nameOfObject = "";
        for (String string : name.toLowerCase().split("_")) {
            nameOfObject += (string.substring(0, 1).toUpperCase() + string.substring(1));
        }
        return nameOfObject;
    }

    private String getObjectAttributeNameGenerator(String name) {
        String nameOfAttribute = "";
        int index = 0;
        for (String string : name.toLowerCase().split("_")) {
            if (index++ == 0) {
                nameOfAttribute += string;
            } else {
                nameOfAttribute += (string.substring(0, 1).toUpperCase() + string.substring(1));
            }
        }
        return nameOfAttribute;
    }
}
