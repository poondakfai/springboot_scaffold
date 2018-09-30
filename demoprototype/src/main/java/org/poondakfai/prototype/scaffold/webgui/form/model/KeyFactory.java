package org.poondakfai.prototype.scaffold.webgui.form.model;


import java.util.HashMap;
import java.util.Map;


public class KeyFactory<T> {
  private int counter;
  private Map<Integer, T> lookup;
  private Map<T, Integer> searchKey;


  public KeyFactory() {
    this.counter = 1;
    this.lookup = new HashMap<Integer, T>();
    this.searchKey = new HashMap<T, Integer>();
  }

  public int put(T object) {
    // Test if this object is existing
    Integer key = this.searchKey.get(object);
    if (key != null) {
      return key;
    }
    // In the case of not existing, generate one and store it.
    key = generateKey();
    this.lookup.put(key, object);
    this.searchKey.put(object, key);
    return key;
  }

  public T lookup(Integer key) {
    return this.lookup.get(key);
  }

  public Integer searchKey(T obj) {
    return this.searchKey.get(obj);
  }

  public boolean removeObject(T object) {
    // Test if this object is existing
    Integer key = this.searchKey.get(object);
    if (key == null) {
      return false;
    }
    // In the case existing, remove two maps for its instance
    this.searchKey.remove(object);
    if (this.lookup.remove(key) == null) {
      return false;
    }
    return true;
  }

  public boolean removeByKey(Integer key) {
    // Test if this object is existing
    T object = this.lookup.get(key);
    if (object == null) {
      return false;
    }
    // In the case existing, remove two maps for its instance
    this.lookup.remove(key);
    if (this.searchKey.remove(object) == null) {
      return false;
    }
    return true;
  }

  private int generateKey() {
    // @TODO performance improve: keyfactory should have ability to reset
    // its key and its map so that it do not need its destructor invoked in
    // KeyPool
    if (this.counter == Integer.MAX_VALUE) {
      throw new RuntimeException("Key value is overflowed");
    }
    return this.counter++;
  }
}


