package edu.cornell.jnutella.guice;

import java.util.Map;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.Scope;

public class JnutellaScopes {

  /**
   * A threadlocal scope map for session scopes.
   */
  private static final ThreadLocal<Map<String, Object>> requestScopeContext =
      new ThreadLocal<Map<String, Object>>();

  /** A sentinel attribute value representing null. */
  enum NullObject {
    INSTANCE
  }

  /**
   * Session scope
   */
  public static final Scope SESSION = new Scope() {
    public <T> Provider<T> scope(final Key<T> key, final Provider<T> creator) {
      final String name = key.toString();
      return new Provider<T>() {
        public T get() {

          // NOTE(dhanji): We don't need to synchronize on the scope map
          // unlike the HTTP request because we're the only ones who have
          // a reference to it, and it is only available via a threadlocal.
          Map<String, Object> scopeMap = requestScopeContext.get();
          if (null != scopeMap) {
            @SuppressWarnings("unchecked")
            T t = (T) scopeMap.get(name);

            // Accounts for @Nullable providers.
            if (NullObject.INSTANCE == t) {
              return null;
            }

            if (t == null) {
              t = creator.get();
              // if (!Scopes.isCircularProxy(t)) {
              // Store a sentinel for provider-given null values.
              scopeMap.put(name, t != null ? t : NullObject.INSTANCE);
              // }
            }

            return t;
          }
          throw new OutOfScopeException(
              "Cannot access scoped object. This means we are not inside of a scoped call.");
        }

        @Override
        public String toString() {
          return String.format("%s[%s]", creator, SESSION);
        }
      };
    }

    @Override
    public String toString() {
      return "JnutellaScopes.SESSION";
    }
  };

  /**
   * Scopes the given callable inside a session scope.
   * 
   * @param callable code to be executed which depends on the request scope. Typically in another
   *        thread, but not necessarily so.
   * @param seedMap the initial set of scoped instances for Guice to seed the session scope with. To
   *        seed a key with null, use {@code null} as the value.
   * @return a callable that when called will run inside the a request scope that exposes the
   *         instances in the {@code seedMap} as scoped keys, as well as keeping any objects created
   *         under this session scope as the same instance in this scope.
   */
  public static <T> Callable<T> scopeCall(final Callable<T> callable, Map<Key<?>, Object> seedMap) {
    Preconditions.checkArgument(null != seedMap,
        "Seed map cannot be null, try passing in Collections.emptyMap() instead.");

    // Copy the seed values into our local scope map.
    final Map<String, Object> scopeMap = Maps.newHashMap();
    for (Map.Entry<Key<?>, Object> entry : seedMap.entrySet()) {
      Object value = validateAndCanonicalizeValue(entry.getKey(), entry.getValue());
      scopeMap.put(entry.getKey().toString(), value);
    }

    return new Callable<T>() {
      public T call() throws Exception {
        Preconditions.checkState(null == requestScopeContext.get(),
            "A request scope is already in progress, cannot scope a new request in this thread.");

        requestScopeContext.set(scopeMap);

        try {
          return callable.call();
        } finally {
          requestScopeContext.remove();
        }
      }
    };
  }

  /**
   * Validates the key and object, ensuring the value matches the key type, and canonicalizing null
   * objects to the null sentinel.
   */
  private static Object validateAndCanonicalizeValue(Key<?> key, Object object) {
    if (object == null || object == NullObject.INSTANCE) {
      return NullObject.INSTANCE;
    }

    if (!key.getTypeLiteral().getRawType().isInstance(object)) {
      throw new IllegalArgumentException("Value[" + object + "] of type["
          + object.getClass().getName() + "] is not compatible with key[" + key + "]");
    }

    return object;
  }
}
