package com.dongnao.jack.createbean;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dongnao.jack.util.ReadConfig;

public class CreateBean {
    
    static String rt = "\r\n";
    
    public static void init() {
        boolean iscreatebean = Boolean.valueOf(ReadConfig.getValue("iscreatebean"));
        
        if (!iscreatebean) {
            return;
        }
        
        Connection conn = ReadConfig.getConnection();
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tableSet = metaData.getTables(null,
                    "%",
                    "%",
                    new String[] {"TABLE"});
            System.out.println(tableSet);
            
            while (tableSet.next()) {
                String tableName = tableSet.getString("TABLE_NAME");
                System.out.println(tableName);
                
                ResultSet columnSet = metaData.getColumns(null,
                        "%",
                        tableName,
                        "%");
                
                createFile(columnSet, tableName);
            }
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    public static void createFile(ResultSet set, String tableName) {
        String beanlocation = ReadConfig.getValue("beanlocation");
        String rootPath = CreateBean.class.getResource("/").getFile();
        rootPath = rootPath.substring(0, rootPath.indexOf("target"));
        String FileLocation = rootPath + "src/main/java/"
                + replaceTo(beanlocation) + "/"
                + tableName.substring(0, 1).toUpperCase()
                + tableName.substring(1) + ".java";
        System.out.println(FileLocation);
        
        createDirectory(rootPath + "src/main/java/", beanlocation);
        
        String fileStr = getFileStr(set, tableName, beanlocation);
        File f = new File(FileLocation);
        FileWriter fw;
        try {
            fw = new FileWriter(f);
            fw.write(fileStr);
            fw.flush();
            fw.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    private static void createDirectory(String root, String beanlocation) {
        String[] directorys = beanlocation.split("\\.");
        String path = "";
        for (String directory : directorys) {
            path += directory + ".";
            File each = new File(root
                    + replaceTo(path.substring(0, path.lastIndexOf("."))));
            if (!each.exists() && !each.isDirectory()) {
                each.mkdir();
            }
        }
    }
    
    private static String getFileStr(ResultSet set, String tableName,
            String packageName) {
        try {
            String packageStr = "package " + packageName + ";" + rt;
            String classStr = "public class "
                    + tableName.substring(0, 1).toUpperCase()
                    + tableName.substring(1) + "{" + rt;
            
            String fieldStr = "";
            String dateImport = "";
            String getSetStr = "";
            while (set.next()) {
                String columnName = set.getString("COLUMN_NAME");
                String columnType = set.getString("TYPE_NAME");
                System.out.println(tableName + ":" + columnType + " "
                        + columnName);
                
                if ("VARCHAR".equals(columnType)) {
                    fieldStr += "public String " + columnName + ";" + rt;
                    String setStr = "public void set"
                            + columnName.substring(0, 1).toUpperCase()
                            + columnName.substring(1) + "(String " + columnName
                            + ") {" + rt + "this." + columnName + " = "
                            + columnName + ";" + rt + "}" + rt;
                    String getStr = "public String get"
                            + columnName.substring(0, 1).toUpperCase()
                            + columnName.substring(1) + "() {" + rt + "return "
                            + columnName + ";" + rt + "}" + rt;
                    getSetStr += setStr + getStr;
                }
                else if ("INT".equals(columnType)) {
                    fieldStr += "public Integer " + columnName + ";" + rt;
                    String setStr = "public void set"
                            + columnName.substring(0, 1).toUpperCase()
                            + columnName.substring(1) + "(Integer "
                            + columnName + ") {" + rt + "this." + columnName
                            + " = " + columnName + ";" + rt + "}" + rt;
                    String getStr = "public Integer get"
                            + columnName.substring(0, 1).toUpperCase()
                            + columnName.substring(1) + "() {" + rt + "return "
                            + columnName + ";" + rt + "}" + rt;
                    getSetStr += setStr + getStr;
                }
                else if ("DATETIME".equals(columnType)) {
                    fieldStr += "public Date " + columnName + ";" + rt;
                    dateImport = "import java.util.Date;" + rt;
                    String setStr = "public void set"
                            + columnName.substring(0, 1).toUpperCase()
                            + columnName.substring(1) + "(Date " + columnName
                            + ") {" + rt + "this." + columnName + " = "
                            + columnName + ";" + rt + "}" + rt;
                    String getStr = "public Date get"
                            + columnName.substring(0, 1).toUpperCase()
                            + columnName.substring(1) + "() {" + rt + "return "
                            + columnName + ";" + rt + "}" + rt;
                    getSetStr += setStr + getStr;
                }
            }
            
            String fileStr = packageStr + dateImport + classStr + fieldStr
                    + getSetStr + "}";
            System.out.println(fileStr);
            return fileStr;
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static String replaceTo(String path) {
        return path.replaceAll("\\.", "/");
    }
    
    public static void main(String[] args) {
        init();
    }
}
