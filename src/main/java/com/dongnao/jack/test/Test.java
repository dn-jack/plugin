package com.dongnao.jack.test;

import com.dongnao.jack.createbean.CreateBean;
import com.dongnao.jack.createdao.CreateDao;
import com.dongnao.jack.createxml.CreateXml;

public class Test {
    
    public static void main(String[] args) {
        CreateBean.init();
        CreateDao.init();
        CreateXml.init();
    }
    
}
