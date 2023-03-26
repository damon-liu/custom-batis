package com.lhx.cofing;

import com.lhx.frame.core.factory.SqlSessionFactory;
import com.lhx.frame.core.factory.SqlSessionFactoryBuilder;
import org.dom4j.DocumentException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.InputStream;

/**
 * Description
 *
 * @author damon.liu
 * Date 2022-12-06 9:24
 */
@Configuration
@ConditionalOnClass(name = "com.lhx.frame.core.factory.SqlSessionFactory")
@Import(DatabaseProperties.class)
public class DamonbatisAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "damonbatis.frame.enable", havingValue = "true", matchIfMissing = true)
    public SqlSessionFactory initFactory(){
        SqlSessionFactory sqlSessionFactory = null;
        try {
            //1.创建SqlSessionFactoryBuilder对象
            SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
            //2.builder对象构建工厂对象
            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("SqlMapConfig.xml");
            sqlSessionFactory = sqlSessionFactoryBuilder.build(resourceAsStream);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        return sqlSessionFactory;
    }

}
