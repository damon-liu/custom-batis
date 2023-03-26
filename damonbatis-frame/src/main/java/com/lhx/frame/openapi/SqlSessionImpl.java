package com.lhx.frame.openapi;

import com.lhx.frame.core.Executor;
import com.lhx.frame.core.entity.Configuration;

import java.util.List;

/**
 * Description
 *
 * @author damon.liu
 * Date 2022-12-06 4:18
 */
public class SqlSessionImpl implements SqlSession{

    private Configuration configuration;

    public SqlSessionImpl(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public <T> List<T> selectList(String sql) throws Exception {
        Executor executor = new Executor(configuration);
        return executor.executeQuery(sql);
    }
}
