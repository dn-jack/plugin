package com.dongnao.jack.createdao;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dongnao.jack.createbean.CreateBean;
import com.dongnao.jack.util.ReadConfig;

public class CreateDao {
    
    static String rt = "\r\n";
    
    public static void init() {
        boolean iscreatedao = Boolean.valueOf(ReadConfig.getValue("iscreatedao"));
        
        if (!iscreatedao) {
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
        String daolocation = ReadConfig.getValue("daolocation");
        String beanlocation = ReadConfig.getValue("beanlocation");
        String rootPath = CreateBean.class.getResource("/").getFile();
        rootPath = rootPath.substring(0, rootPath.indexOf("target"));
        String FileLocation = rootPath + "src/main/java/"
                + replaceTo(daolocation) + "/"
                + tableName.substring(0, 1).toUpperCase()
                + tableName.substring(1) + "Mapper" + ".java";
        System.out.println(FileLocation);
        
        createDirectory(rootPath + "src/main/java/", daolocation);
        
        String fileStr = getFileStr(set, tableName, daolocation, beanlocation);
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
    
    private static String getFileStr(ResultSet set, String tableName,
            String daopackageName, String beanPackageName) {
        
        String packageStr = "package " + daopackageName + ";" + rt;
        String classStr = "public interface "
                + tableName.substring(0, 1).toUpperCase()
                + tableName.substring(1) + "Mapper" + "{" + rt;
        String importStr = "import " + beanPackageName + "."
                + tableName.substring(0, 1).toUpperCase()
                + tableName.substring(1) + ";" + rt + "import java.util.List;"
                + rt + "import java.util.Map;" + rt;
        String selectmethodStr = "List<"
                + tableName.substring(0, 1).toUpperCase()
                + tableName.substring(1) + "> select(Map param);" + rt;
        String insertStr = "int insert("
                + tableName.substring(0, 1).toUpperCase()
                + tableName.substring(1) + " " + tableName + ");" + rt;
        String updateStr = "int update("
                + tableName.substring(0, 1).toUpperCase()
                + tableName.substring(1) + " " + tableName + ");" + rt;
        String deleteStr = "int delete("
                + tableName.substring(0, 1).toUpperCase()
                + tableName.substring(1) + " " + tableName + ");" + rt;
        String fileStr = packageStr + importStr + classStr + selectmethodStr
                + insertStr + updateStr + deleteStr + "}";
        System.out.println(fileStr);
        return fileStr;
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
    
    public static String replaceTo(String path) {
        return path.replaceAll("\\.", "/");
    }
    
    public static void main(String[] args) {
        init();
        
    }
    
}
