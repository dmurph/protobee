package org.protobee.guice;

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
  private static final ThreadLocal<Map<String, Object>> sessionScopeContext =
      new ThreadLocal<Map<String, Object>>();

  private static final ThreadLocal<Map<String, Object>> identityScopeContext =
      new ThreadLocal<Map<String, Object>>();

  /** A sentinel attribute value representing null. */
  public static enum NullObject {
    INSTANCE
  }

  /**
   * Session scope
   */
  public static final Scope SESSION = new Scope() {
    public <T> Provider<T> scope(final Key<T> key, final Provider<T> creator) {
      final String name = key.toString();
      return new Provider<T>() {
        @SuppressWarnings("unchecked")
        public T get() {

          Map<String, Object> scopeMap = sessionScopeContext.get();
          if (scopeMap != null) {
            T t;
            synchronized (name.intern()) {
              t = (T) scopeMap.get(name);

              if (t == null) {
                t = creator.get();
                // if (!Scopes.isCircularProxy(t)) {
                // Store a sentinel for provider-given null values.
                scopeMap.put(name, t != null ? t : NullObject.INSTANCE);
                // }
              }
            }
            // Accounts for @Nullable providers.
            if (NullObject.INSTANCE == t) {
              return null;
            }

            return t;
          }
          throw new OutOfScopeException(
              "Cannot access session scoped object. This means we are not inside of a scoped call.");
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
   * Session scope
   */
  public static final Scope IDENTITY = new Scope() {
    public <T> Provider<T> scope(final Key<T> key, final Provider<T> creator) {
      final String name = key.toString();
      return new Provider<T>() {
        @SuppressWarnings("unchecked")
        public T get() {

          Map<String, Object> scopeMap = identityScopeContext.get();
          if (scopeMap != null) {
            T t;
            synchronized (name.intern()) {
              t = (T) scopeMap.get(name);

              if (t == null) {
                t = creator.get();
                // if (!Scopes.isCircularProxy(t)) {
                // Store a sentinel for provider-given null values.
                scopeMap.put(name, t != null ? t : NullObject.INSTANCE);
                // }
              }
            }
            // Accounts for @Nullable providers.
            if (NullObject.INSTANCE == t) {
              return null;
            }

            return t;
          }
          throw new OutOfScopeException(
              "Cannot access session scoped object. This means we are not inside of a scoped call.");
        }

        @Override
        public String toString() {
          return String.format("%s[%s]", creator, IDENTITY);
        }
      };
    }

    @Override
    public String toString() {
      return "JnutellaScopes.IDENTITY";
    }
  };

  public static void enterSessionScope(Map<String, Object> seedMap) {
    Preconditions.checkArgument(null != seedMap,
        "Seed map cannot be null, try passing in Collections.emptyMap() instead.");
    Preconditions.checkState(sessionScopeContext.get() == null,
        "Scope already entered on this thread");
    sessionScopeContext.set(seedMap);
  }

  public static boolean isInSessionScope() {
    return sessionScopeContext.get() != null;
  }

  public static boolean isInSessionScope(Map<String, Object> seedMap) {
    return sessionScopeContext.get() == seedMap;
  }

  public static void exitSessionScope() {
    sessionScopeContext.set(null);
  }

  public static void enterIdentityScope(Map<String, Object> seedMap) {
    Preconditions.checkArgument(null != seedMap,
        "Seed map cannot be null, try passing in Collections.emptyMap() instead.");
    Preconditions.checkState(identityScopeContext.get() == null,
        "Scope already entered on this thread");
    identityScopeContext.set(seedMap);
  }

  public static boolean isInIdentityScope() {
    return identityScopeContext.get() != null;
  }

  public static boolean isInIdentityScope(Map<String, Object> seedMap) {
    return identityScopeContext.get() == seedMap;
  }

  public static void exitIdentityScope() {
    identityScopeContext.set(null);
  }

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
        Preconditions.checkState(null == sessionScopeContext.get(),
            "A request scope is already in progress, cannot scope a new request in this thread.");

        sessionScopeContext.set(scopeMap);

        try {
          return callable.call();
        } finally {
          sessionScopeContext.remove();
        }
      }
    };
  }

  public static void putObjectInScope(Key<?> key, Object object, Map<String, Object> map) {
    map.put(key.toString(), validateAndCanonicalizeValue(key, object));
  }

  /**
   * Validates the key and object, ensuring the value matches the key type, and canonicalizing null
   * objects to the null sentinel.
   */
  public static Object validateAndCanonicalizeValue(Key<?> key, Object object) {
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
