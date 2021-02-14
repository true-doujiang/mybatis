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
package org.apache.ibatis.binding;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 *
 * jdk 事务处理器  所有xxxMapper执行方法都会调用我的invoke()
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {


  private static final Log log = LogFactory.getLog(MapperProxy.class);

  private static final long serialVersionUID = -6424540398559729838L;

  // session
  private final SqlSession sqlSession;
  // target xxxMapper接口
  private final Class<T> mapperInterface;
  // key: 反射方法   value: 具体的方法签名
  private final Map<Method, MapperMethod> methodCache;


  /**
   * 构造器
   */
  public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
    this.sqlSession = sqlSession;
    this.mapperInterface = mapperInterface;
    this.methodCache = methodCache;
  }

  /**
   *  jdk 事务处理器  所有xxxMapper执行方法都会调用我的invoke()
   * @param proxy
   * @param method 被代理类 执行的方法
   */
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    // 为毛打印 proxy 就报StackOverflowError
//    log.debug("MapperProxy invoke.  proxy: " + proxy);
    log.debug("MapperProxy invoke.  method: " + method.getName());
    log.debug("MapperProxy invoke.  args: " + Arrays.toString(args));


    if (Object.class.equals(method.getDeclaringClass())) {
      try {
        return method.invoke(this, args);
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    }

    // 创建一个 MapperMethod
    final MapperMethod mapperMethod = cachedMapperMethod(method);
    // 入口方法
    return mapperMethod.execute(sqlSession, args);
  }

  /**
   * 创建一个 MapperMethod
   */
  private MapperMethod cachedMapperMethod(Method method) {
    MapperMethod mapperMethod = methodCache.get(method);
    if (mapperMethod == null) {
      Configuration configuration = sqlSession.getConfiguration();
      // 创建一个 MapperMethod
      mapperMethod = new MapperMethod(mapperInterface, method, configuration);
      methodCache.put(method, mapperMethod);
    }
    return mapperMethod;
  }

}
