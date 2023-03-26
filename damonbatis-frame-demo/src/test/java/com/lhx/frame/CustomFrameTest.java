package com.lhx.frame;


import com.lhx.frame.core.factory.SqlSessionFactory;
import com.lhx.frame.core.factory.SqlSessionFactoryBuilder;
import com.lhx.frame.openapi.SqlSession;
import com.lhx.frame.pojo.User;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

public class CustomFrameTest {

    SqlSession sqlSession;
    @Before
    public void init() throws Exception{
        //1.创建SqlSessionFactoryBuilder对象
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        //2.builder对象构建工厂对象
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("SqlMapConfig.xml");
        SqlSessionFactory sqlSessionFactory = builder.build(inputStream);
        //3.打开SqlSession会话
        sqlSession = sqlSessionFactory.openSession();
    }
    @Test
    public void test() throws Exception {
        //4.执行查询Sql语句
        List<User> users = sqlSession.selectList("com.lhx.frame.dao.UserMapper.findAll");
        //5.循环打印
        for (User u : users) {
            System.out.println(u);
        }
    }
}
