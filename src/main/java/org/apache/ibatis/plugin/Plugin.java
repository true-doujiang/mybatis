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
package org.apache.ibatis.plugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.reflection.ExceptionUtil;

/**
 * @author Clinton Begin
 *
 * jdk 动态代理 事务处理器
 */
public class Plugin implements InvocationHandler {

  // 被代理对象
  private Object target;
  // mybatis拦截器
  private Interceptor interceptor;
  //存放指定的方法(添加了拦截器)
  private Map<Class<?>, Set<Method>> signatureMap;


  /**
   * 构造器
   */
  private Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
    this.target = target;
    this.interceptor = interceptor;
    this.signatureMap = signatureMap;
  }

  /**
   * 工具方法
   * @param target 被代理对象
   * @param interceptor
   * @return
   */
  public static Object wrap(Object target, Interceptor interceptor) {

    Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
    Class<?> type = target.getClass();

    Class<?>[] interfaces = getAllInterfaces(type, signatureMap);

    if (interfaces.length > 0) {
      // 创建一个jdk动态代理事务处理器
      Plugin plugin = new Plugin(target, interceptor, signatureMap);
      return Proxy.newProxyInstance(type.getClassLoader(), interfaces, plugin);
    }

    return target;
  }

  /**
   * 动态代理方法
   */
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      // 获取被拦截器拦截的方法
      Set<Method> methods = signatureMap.get(method.getDeclaringClass());
      // 可以对指定方法配置拦截器
      if (methods != null && methods.contains(method)) {
        // 执行拦截器方法
        return interceptor.intercept(new Invocation(target, method, args));
      }

      return method.invoke(target, args);
    } catch (Exception e) {
      throw ExceptionUtil.unwrapThrowable(e);
    }
  }

  /**
   * 解析@Intercepts注解
   * @param interceptor
   * @return
   */
  private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
    Intercepts interceptsAnnotation = interceptor.getClass().getAnnotation(Intercepts.class);
    if (interceptsAnnotation == null) { // issue #251
      throw new PluginException("No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());      
    }
    Signature[] sigs = interceptsAnnotation.value();
    Map<Class<?>, Set<Method>> signatureMap = new HashMap<Class<?>, Set<Method>>();

    for (Signature sig : sigs) {
      Set<Method> methods = signatureMap.get(sig.type());
      if (methods == null) {
        methods = new HashSet<Method>();
        signatureMap.put(sig.type(), methods);
      }
      try {
        Method method = sig.type().getMethod(sig.method(), sig.args());
        methods.add(method);
      } catch (NoSuchMethodException e) {
        throw new PluginException("Could not find method on " + sig.type() + " named " + sig.method() + ". Cause: " + e, e);
      }
    }

    return signatureMap;
  }


  private static Class<?>[] getAllInterfaces(Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
    Set<Class<?>> interfaces = new HashSet<Class<?>>();

    while (type != null) {
      for (Class<?> c : type.getInterfaces()) {
        if (signatureMap.containsKey(c)) {
          interfaces.add(c);
        }
      }
      type = type.getSuperclass();
    }

    return interfaces.toArray(new Class<?>[interfaces.size()]);
  }

}
