/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.xml_references;

import java.io.Reader;
import java.sql.Date;
import java.util.Properties;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Test;

import javax.sql.DataSource;

public class EnumWithOgnlTest {
    
    @Test
    public void testConfiguration() {
        Properties dataSourceProperties = new Properties();
        dataSourceProperties.put("driver", "org.hsqldb.jdbcDriver");
        dataSourceProperties.put("url", "jdbc:hsqldb:mem:xml_references");
        dataSourceProperties.put("username", "sa");

        UnpooledDataSourceFactory dataSourceFactory = new UnpooledDataSourceFactory();
        dataSourceFactory.setProperties(dataSourceProperties);

        JdbcTransactionFactory jdbcTransactionFactory = new JdbcTransactionFactory();
        DataSource dataSource = dataSourceFactory.getDataSource();
        Environment environment = new Environment("test", jdbcTransactionFactory, dataSource);

        Configuration configuration = new Configuration();
        // 设置env
        configuration.setEnvironment(environment);
        configuration.getTypeAliasRegistry().registerAlias(Person.class);
        configuration.addMapper(PersonMapper.class);
        configuration.addMapper(PersonMapper2.class);

        DefaultSqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(configuration);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
//        personMapper.selectAllByType(Date.class)
    }
    @Test
    public void testMixedConfiguration() throws Exception {
    	Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/xml_references/ibatisConfig.xml");
    	SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    	sqlSessionFactory.getConfiguration().addMapper(PersonMapper2.class);
    }
}
