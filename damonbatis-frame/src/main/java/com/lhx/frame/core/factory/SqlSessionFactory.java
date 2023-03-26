package com.lhx.frame.core.factory;

import com.lhx.frame.core.entity.Configuration;
import com.lhx.frame.openapi.SqlSession;
import com.lhx.frame.openapi.SqlSessionImpl;

/**
 * Description:SqlSession工厂类，负责创建SqlSession接口实现类
 *
 * @author damon.liu
 * Date 2022-12-06 4:15
 */
public class SqlSessionFactory {

    private Configuration configuration;

    public SqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 创建SqlSession会话
     */
    public SqlSession openSession(){
        return new SqlSessionImpl(configuration);
    }
}
