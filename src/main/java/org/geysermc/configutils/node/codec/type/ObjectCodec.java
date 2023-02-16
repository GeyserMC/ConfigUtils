package org.geysermc.configutils.node.codec.type;

import io.leangen.geantyref.GenericTypeReflector;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.geysermc.configutils.node.Node;
import org.geysermc.configutils.node.context.MetaOptions;
import org.geysermc.configutils.node.context.NodeContext;

public final class ObjectCodec extends TypeCodec<Object> {
  public static final ObjectCodec INSTANCE = new ObjectCodec();

  private ObjectCodec() {
    super(Object.class);
  }

  @Override
  public Node<Object> deserialize(AnnotatedType type, Object value, NodeContext context) {
    if (GenericTypeReflector.erase(type.getType()).isInterface()) {
      return ObjectCodecProxied.INSTANCE.deserialize(type, value, context);
    }
    throw new IllegalStateException("Cannot deserialize " + type);
  }

  @Override
  public Object serialize(AnnotatedType type, Object value, NodeContext context) {
    if (GenericTypeReflector.erase(type.getType()).isInterface()) {
      return ObjectCodecProxied.INSTANCE.serialize(type, value, context);
    }
    throw new IllegalStateException("Cannot serialize " + type);
  }

  private static final class ObjectCodecProxied extends TypeCodec<Object> {
    public static final ObjectCodecProxied INSTANCE = new ObjectCodecProxied();

    private ObjectCodecProxied() {
      super(Object.class);
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

        if (method.getParameterCount() == 0) {
          return content.get(method.getName());
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

    @Override
    public Node<Object> deserialize(AnnotatedType type, Object value, NodeContext context) {
      if (!(value instanceof Map<?, ?>)) {
        throw new IllegalStateException("An object is serialized from a map");
      }
      Map<?, ?> valueAsMap = (Map<?, ?>) value;

      Class<?> clazz = GenericTypeReflector.erase(type.getType());
      Map<String, Node<?>> mappings = new HashMap<>();
      for (Method method : clazz.getMethods()) {
        if (method.getParameterCount() != 0) {
          continue;
        }

        AnnotatedType returnType = GenericTypeReflector.getReturnType(method, type);
        MetaOptions meta = context.createMeta(returnType);

        //todo instead of failing when there is something missing:
        // mark the node as changed (add that as a field)
        // note: that's actually not possible since we want to apply all annotations later
        // instead we should prob make a StrategyState for the ObjectStrategy that marks them as absent

        String name = method.getName();
        Object entry = valueAsMap.get(context.options().nameEncoder().apply(name));
        if (entry == null) {
          //todo add option to fail when there are missing keys
          //-- fail by default, maybe add an @Optional??
          continue;
        }

        TypeCodec<?> codec = context.codecFor(returnType);
        if (codec == null) {
          throw new IllegalStateException("No codec registered for type " + returnType);
        }

        mappings.put(name, codec.deserialize(returnType, entry, context));
      }

      return Proxy.newProxyInstance(
          ObjectCodec.class.getClassLoader(),
          new Class[] {clazz},
          new ProxyInvocationHandler(type.getType(), mappings)
      );
    }

    @Override
    public Object serialize(AnnotatedType type, Object value, NodeContext context) {
      if (!Proxy.isProxyClass(value.getClass())) {
        throw new IllegalStateException(
            "An ObjectCodecProxied cannot serialize non-proxied type " + value.getClass()
        );
      }

      InvocationHandler invocationHandler = Proxy.getInvocationHandler(value);
      if (!(invocationHandler instanceof ProxyInvocationHandler)) {
        throw new IllegalStateException("Cannot serialize custom proxy type " + value.getClass());
      }

      Map<?, ?> valueAsMap = ((ProxyInvocationHandler) invocationHandler).content;

      Method[] methods = GenericTypeReflector.erase(type.getType()).getMethods();
      Map<String, Object> mappings = new HashMap<>();
      for (Method method : methods) {
        if (method.getParameterCount() != 0) {
          continue;
        }

        String name = method.getName();
        Object entry = valueAsMap.get(name);
        if (entry == null) {
          continue;
        }

        AnnotatedType returnType = GenericTypeReflector.getReturnType(method, type);
        TypeCodec<?> codec = context.codecFor(returnType);
        if (codec == null) {
          throw new IllegalStateException("No codec registered for type " + returnType);
        }

        mappings.put(
            context.options().nameEncoder().apply(name),
            codec.deserialize(returnType, entry, context)
        );
      }
      return mappings;
    }
  }
}
