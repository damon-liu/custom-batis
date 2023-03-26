package com.lhx.frame.openapi;

import java.util.List;

/**
 * Description:对外暴露SqlSession接口
 *  接口中定义常见的JDBC操作：增删改查
 *
 * @author damon.liu
 * Date 2022-12-06 4:17
 */
public interface SqlSession {
    /**
     * 查询所有用户
     * T 代表泛型类型，T(type缩写)
     */
    <T> List<T> selectList(String sql) throws Exception;
}
