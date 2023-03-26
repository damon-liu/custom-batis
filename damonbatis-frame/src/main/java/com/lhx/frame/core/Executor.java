package com.lhx.frame.core;

import com.lhx.frame.core.entity.Configuration;
import com.lhx.frame.core.entity.SqlSource;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description
 *
 * @author damon.liu
 * Date 2022-12-06 4:25
 */
public class Executor {

    private Configuration configuration;

    public Executor(Configuration configuration) {
        this.configuration = configuration;
    }

    public List executeQuery(String statement) throws Exception {
        /**
         * 数据库连接信息硬编码
         */
        String driver = configuration.getDriver();
        String url = configuration.getUrl();
        String username = configuration.getUsername();
        String password = configuration.getPassword();
        Map<String, SqlSource> map = configuration.getSqlSourceMap();
        // 获取sql信息
        SqlSource mapper = map.get(statement);
        String sqlStr = mapper.getSql();
        String resultType = mapper.getResultType();

        //1.注册MySQL驱动
        Class.forName(driver);
        Connection conn = DriverManager.getConnection(url, username, password);
        //3.创建SQL语句对象Statement，填写SQL语句
        PreparedStatement ps = conn.prepareStatement(sqlStr);
        //4.执行查询SQL，返回结果集ResultSet
        ResultSet rs = ps.executeQuery();
        //5.解析结果集，获取查询用户list集合
        //获取结果集元数据
        ResultSetMetaData metaData = rs.getMetaData();
        //获取总列数5
        int columnCount = metaData.getColumnCount();
        //获取所有列的list集合
        List<String> columnNames = new ArrayList<String>();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnName(i));
        }
        List list = new ArrayList();
        while (rs.next()) {
            //在代码运行的时候，为创建指定类的对象，并且可以调用对象的方法，而且无视权限修饰符！
            //在汽车奔跑的时候，为汽车更换轮子！
            //通过反射获取类的字节码对象，传入的参数是类的全限定名称
            Class<?> clazz = Class.forName(resultType);
            //反射创建对象
            Object user = clazz.newInstance();
            //反射获取当前类的所有方法
            Method[] methods = clazz.getMethods();
            //循环 遍历所有列名
            for (String columnName : columnNames) {
                for (Method method : methods) {
                    String methodName = method.getName();
                    //判断方法的名称，与set+列名相等，那么就把列名对应的值设置到当前对象的set方法中
                    if (("set"+columnName).equalsIgnoreCase(methodName)){
                        //把列名column对应的值,设置到对象的set方法中,给属性赋值
                        method.invoke(user,rs.getObject(columnName));
                    }
                }
            }
            //将用户存入集合中
            list.add(user);
        }
        //关闭连接，释放资源
        rs.close();
        ps.close();
        conn.close();
        return list;
    }

}
