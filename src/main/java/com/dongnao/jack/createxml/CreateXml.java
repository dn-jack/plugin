package com.dongnao.jack.createxml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dongnao.jack.createbean.CreateBean;
import com.dongnao.jack.util.ReadConfig;

public class CreateXml {
    
    static String rt = "\r\n";
    
    public static void init() {
        boolean iscreatexml = Boolean.valueOf(ReadConfig.getValue("iscreatexml"));
        
        if (!iscreatexml) {
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
                
                ResultSet primaryKeySet = metaData.getPrimaryKeys(null,
                        null,
                        tableName);
                
                createFile(columnSet, tableName, primaryKeySet);
            }
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static void createFile(ResultSet set, String tableName,
            ResultSet primaryKeySet) {
        String xmllocation = ReadConfig.getValue("xmllocation");
        String daolocation = ReadConfig.getValue("daolocation");
        String beanlocation = ReadConfig.getValue("beanlocation");
        String rootPath = CreateBean.class.getResource("/").getFile();
        rootPath = rootPath.substring(0, rootPath.indexOf("target"));
        String FileLocation = rootPath + "src/main/java/"
                + replaceTo(xmllocation) + "/" + tableName + ".xml";
        System.out.println(FileLocation);
        
        createDirectory(rootPath + "src/main/java/", xmllocation);
        
        String fileStr = getFileStr(set,
                tableName,
                daolocation,
                beanlocation,
                primaryKeySet);
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
            String daopackageName, String beanpackageName,
            ResultSet primaryKeySet) {
        
        String doctype = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                + rt
                + "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\" >"
                + rt + "<mapper namespace=\"" + daopackageName + "."
                + tableName.substring(0, 1).toUpperCase()
                + tableName.substring(1) + "Mapper" + "\" >" + rt;
        
        String resultMapStr = "<resultMap id=\"BaseResultMap\" type=\""
                + beanpackageName + "."
                + tableName.substring(0, 1).toUpperCase()
                + tableName.substring(1) + "\" >" + rt;
        String selectStr = "<select id=\"select\" parameterType=\"java.util.Map\" resultMap=\"BaseResultMap\">"
                + rt + "select ";
        String insertStr = "<insert id=\"insert\" parameterType=\""
                + beanpackageName + "."
                + tableName.substring(0, 1).toUpperCase()
                + tableName.substring(1) + "\">" + rt + "insert into "
                + tableName + "(";
        String updateStr = "<update id=\"update\"  parameterType=\""
                + beanpackageName + "."
                + tableName.substring(0, 1).toUpperCase()
                + tableName.substring(1) + "\">" + rt + "update " + tableName
                + rt + "<set>" + rt;
        String deleteStr = "<delete id=\"delete\" parameterType=\""
                + beanpackageName + "."
                + tableName.substring(0, 1).toUpperCase()
                + tableName.substring(1) + "\">" + rt + "delete from ";
        String valuesStr = "";
        String primaryKeyName = getPrimaryKey(primaryKeySet);
        try {
            if (primaryKeyName != null) {
                resultMapStr += "<id column=\"" + primaryKeyName
                        + "\" property=\"" + primaryKeyName + "\"/>" + rt;
            }
            while (set.next()) {
                String columnName = set.getString("COLUMN_NAME");
                String columnType = set.getString("TYPE_NAME");
                System.out.println(tableName + ":" + columnType + " "
                        + columnName);
                
                if (primaryKeyName.equals(columnName)) {
                    continue;
                }
                else {
                    resultMapStr += "<result column=\"" + columnName
                            + "\" property=\"" + columnName + "\"/>" + rt;
                }
                
                selectStr += columnName + ",";
                insertStr += columnName + ",";
                valuesStr += "#{" + columnName + ",jdbcType=" + columnType
                        + "}" + ",";
                
                updateStr += "<if test=\"" + columnName + " != null\" >" + rt;
                updateStr += columnName + " = #{" + columnName + ",jdbcType="
                        + columnType + "}," + rt;
                updateStr += "</if>";
            }
            resultMapStr += "</resultMap>" + rt;
            selectStr = selectStr.substring(0, selectStr.lastIndexOf(","))
                    + " from " + tableName + rt + "</select>" + rt;
            insertStr = insertStr.substring(0, insertStr.lastIndexOf(","))
                    + ") values("
                    + valuesStr.substring(0, valuesStr.lastIndexOf(",")) + ")"
                    + rt + "</insert>" + rt;
            updateStr += "</set> " + rt + "where " + primaryKeyName + " = #{"
                    + primaryKeyName + "}" + rt + "</update>" + rt;
            deleteStr += tableName + " where " + primaryKeyName + " = #{"
                    + primaryKeyName + "}" + rt + "</delete>" + rt;
            
            String fileStr = doctype + resultMapStr + selectStr + insertStr
                    + updateStr + deleteStr + "</mapper>";
            System.out.println(fileStr);
            return fileStr;
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    private static String getPrimaryKey(ResultSet primaryKeySet) {
        try {
            while (primaryKeySet.next()) {
                String primaryKeyColumnName = primaryKeySet.getString("COLUMN_NAME");
                return primaryKeyColumnName;
            }
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
