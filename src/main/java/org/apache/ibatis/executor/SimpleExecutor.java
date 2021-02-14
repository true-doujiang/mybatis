/*
 *    Copyright 2009-2014 the original author or authors.
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
package org.apache.ibatis.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

/**
 * @author Clinton Begin
 */
public class SimpleExecutor extends BaseExecutor {


  /**
   * 构造器
   */
  public SimpleExecutor(Configuration configuration, Transaction transaction) {
    super(configuration, transaction);
  }



  public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
    Statement stmt = null;
    try {
      Configuration configuration = ms.getConfiguration();

      // configuration对象创建StatementHandler
      StatementHandler handler = configuration.newStatementHandler(
              this, ms, parameter, RowBounds.DEFAULT, null, null);

      // 创建 jdbc Statement
      stmt = prepareStatement(handler, ms.getStatementLog());
      return handler.update(stmt);
    } finally {
      closeStatement(stmt);
    }
  }

  /**
   * 创建 mybatis StatementHandler 执行查询
   */
  public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds,
                             ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
    // jdbc stmt
    Statement stmt = null;
    try {
      Configuration configuration = ms.getConfiguration();

      // 这样不行嘛
      //this.configuration.newStatementHandler()

      // configuration对象创建mybatis StatementHandler
      StatementHandler statementHandler =
              configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);

      // 创建jdbc Statement 并设置参数
      stmt = prepareStatement(statementHandler, ms.getStatementLog());
      // 执行jdbc
      return statementHandler.<E>query(stmt, resultHandler);
    } finally {
      closeStatement(stmt);
    }
  }

  /**
   *  1创建 jdbc Statement   2设置jdbc参数
   * @param handler mybatis StatementHandler
   * @param statementLog
   */
  private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
    Statement stmt;
    // 调用父类方法 通过事务管理器获取connection  日志:Opening JDBC Connection
    Connection connection = getConnection(statementLog);
    // 创建 jdbc Statement
    stmt = handler.prepare(connection);
    // 设置jdbc参数
    handler.parameterize(stmt);
    return stmt;
  }


  public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
    return Collections.emptyList();
  }


}
