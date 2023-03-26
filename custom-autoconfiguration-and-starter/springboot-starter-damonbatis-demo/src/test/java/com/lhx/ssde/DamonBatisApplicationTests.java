package com.lhx.ssde;

import com.lhx.frame.core.factory.SqlSessionFactory;
import com.lhx.frame.openapi.SqlSession;
import com.lhx.ssde.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class DamonBatisApplicationTests {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test
    void selectList() throws Exception {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        List<User> users = sqlSession.selectList("com.lhx.ssde.dao.UserMapper.findAll");
        users.stream().forEach(System.out::println);
    }


}
