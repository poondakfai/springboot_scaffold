package org.poondakfai.prototype.scaffold.webgui.form;


public class ObjectIdentifier {
  private String name;
  private String id;

  public ObjectIdentifier(String name, String id) {
    this.name = name;
    this.id = id;
  }

  public ObjectIdentifier() {
    this("", "");
  }

  public String getName() {
    return this.name;
  }

  public String getId() {
    return this.id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return this.name + "[" + this.id + "]";
  }
}


