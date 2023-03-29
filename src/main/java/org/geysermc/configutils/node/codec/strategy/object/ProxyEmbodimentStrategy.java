package org.geysermc.configutils.node.codec.strategy.object;

import io.leangen.geantyref.GenericTypeReflector;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Map;
import org.geysermc.configutils.util.ReflectionUtils;

public final class ProxyEmbodimentStrategy implements ObjectEmbodimentStrategy {
  @Override
  public Object embody(AnnotatedType type, Map<String, Object> input) {
    Class<?> toProxy = GenericTypeReflector.erase(type.getType());
    return Proxy.newProxyInstance(
        ProxyEmbodimentStrategy.class.getClassLoader(),
        new Class[] {toProxy},
        new ProxyInvocationHandler(type.getType(), input)
    );
  }

  @Override
  public Map<String, Object> disembody(Object embodiment) {
    if (!Proxy.isProxyClass(embodiment.getClass())) {
      throw new IllegalStateException(
          "A ProxyEmbodimentStrategy cannot disembody non-proxied type " + embodiment.getClass()
      );
    }

    InvocationHandler invocationHandler = Proxy.getInvocationHandler(embodiment);
    if (!(invocationHandler instanceof ProxyInvocationHandler)) {
      throw new IllegalStateException(
          "Cannot disembody custom proxy type " + embodiment.getClass()
      );
    }

    return ((ProxyInvocationHandler) invocationHandler).content;
  }

  private static final class ProxyInvocationHandler implements InvocationHandler {
    private final Type proxiedType;
    private final Map<String, Object> content;

    private ProxyInvocationHandler(Type proxiedType, Map<String, Object> content) {
      this.content = content;
      this.proxiedType = proxiedType;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      // methods implemented in every class
      if ("equals".equals(method.getName()) && method.getParameterCount() == 1 &&
          method.getReturnType() == boolean.class) {
        return args != null && args.length == 1 &&
               Proxy.isProxyClass(args[0].getClass()) &&
               equals(Proxy.getInvocationHandler(args[0]));
      }
      if ("hashCode".equals(method.getName()) && method.getParameterCount() == 0 &&
          method.getReturnType() == int.class) {
        return content.hashCode();
      }
      if ("toString".equals(method.getName()) && method.getParameterCount() == 0 &&
          method.getReturnType() == String.class) {
        return GenericTypeReflector.getTypeName(proxiedType) + '@' + content.hashCode();
      }

      //todo add Configuration / DataHolder class
      // which allows config values to be changed

      if (method.getParameterCount() == 0) {
        Object value = content.get(method.getName());
        if (value != null) {
          return value;
        }
      }

      if (method.isDefault()) {
        MethodHandle handle = ReflectionUtils.handleFor(method, proxy);
        if (args == null || args.length == 0) {
          return handle.invoke();
        }
        return handle.invokeWithArguments(args);
      }

      throw new NoSuchMethodException();
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) {
        return true;
      }
      if (!(object instanceof ProxyInvocationHandler)) {
        return false;
      }
      return this.content.equals(((ProxyInvocationHandler) object).content);
    }
  }
}
