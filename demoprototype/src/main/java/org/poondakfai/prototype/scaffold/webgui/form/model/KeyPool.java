package org.poondakfai.prototype.scaffold.webgui.form.model;


import java.util.HashMap;
import java.util.Map;


public class KeyPool {
  private Map<Class, KeyFactory> pool;


  public KeyPool() {
    this.pool = new HashMap<Class, KeyFactory>();
  }

  public boolean create(Class clazz) {
    // @TODO pool constructor / destruction invoking reduction by
    // leveraging reset method

    // Test if this clazz has a KeyFactory instance
    KeyFactory keyFactory = this.pool.get(clazz);
    if (keyFactory != null) {
      return false;
    }
    // Create new one in the case of nonexistence
    this.pool.put(clazz, new KeyFactory<Object>());
    return true;
  }

  public boolean remove(Class clazz) {
    return this.pool.remove(clazz) == null ? false : true;
  }

  public KeyFactory get(Class clazz) {
    return this.pool.get(clazz);
  }

  public Object lookup(Class clazz, Integer key) {
    // Search for its KeyFactory instance
    KeyFactory keyFactory = this.pool.get(clazz);
    if (keyFactory == null) {
      return null;
    }
    // Lookup one in the case of existence
    return keyFactory.lookup(key);
  }

  public Integer searchOrCreateNewKey(Object obj) {
    return obj == null ? null  : searchOrCreateNewKey(obj.getClass(), obj);
  }

  public Integer searchOrCreateNewKey(Class clazz, Object obj) {
    // Search for its KeyFactory instance, if it does not exist, create one
    KeyFactory keyFactory = this.pool.get(clazz);
    if (keyFactory == null) {
      this.create(clazz);
    }
    // Get the keyFactory instance
    keyFactory = this.pool.get(clazz);
    if (keyFactory == null) {
      return null;
    }
    // delegate keyFactory for search or create new key behavior
    return keyFactory.put(obj);
  }
}


