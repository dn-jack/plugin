package com.dongnao.jack.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ReadConfig {
    private static Properties pro = new Properties();
    
    static {
        readConfig();
    }
    
    public static String getValue(String name) {
        return pro.getProperty(name);
    }
    
    private static void readConfig() {
        FileInputStream in;
        try {
            System.out.println(ReadConfig.class.getResource("/").getFile());
            in = new FileInputStream(ReadConfig.class.getResource("/")
                    .getFile() + File.separator + "config.properties");
            pro.load(in);
            in.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    public static Connection getConnection() {
        Driver driver;
        try {
            driver = (Driver)(Class.forName(ReadConfig.getValue("jdbcDriver")).newInstance());
            DriverManager.registerDriver(driver);
            Connection conn = DriverManager.getConnection(ReadConfig.getValue("dbUrl"),
                    ReadConfig.getValue("dbUsername"),
                    ReadConfig.getValue("dbPassword"));
            return conn;
        }
        catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
