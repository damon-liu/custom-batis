package com.damon.mybatis;

import com.damon.jdbc.pojo.User;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class JdbcDemoTest {

    @Test
    public void test() throws Exception {
        //数据库连接地址
        String url = "jdbc:mysql://localhost:3306/sharding_jdbc?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";
        String user = "root";
        String password = "123456";
        // 1.加载数据库驱动
        Class.forName("com.mysql.jdbc.Driver");
        // 2.获取数据库连接对象Connection
        Connection connection = DriverManager.getConnection(url, user, password);
        // 3.创建sql语句对象statement，填写sql语句
        PreparedStatement preparedStatement = connection.prepareStatement("select * from t_user where name=?;");
        // 4.传入参数
        preparedStatement.setString(1, "刘备【1】");
        // 5.执行sql
        ResultSet resultSet = preparedStatement.executeQuery();
        // 6.遍历结果集
        List<User> users = new ArrayList<User>();
        while (resultSet.next()) {
            User u = User.builder().id(resultSet.getInt("id"))
                    .name(resultSet.getString("name"))
                    .age(resultSet.getInt("age"))
                    .address(resultSet.getString("address")).build();
            users.add(u);
        }
        System.out.println("users:  " + users);
        //7.关闭连接，释放资源
        resultSet.close();//关闭结果集对象
        preparedStatement.close();//关闭Sql语句对象
        connection.close();//关闭数据库连接对象
    }
}
