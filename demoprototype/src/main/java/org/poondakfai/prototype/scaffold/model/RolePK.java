package org.poondakfai.prototype.scaffold.model;


import java.io.Serializable;


public class RolePK implements Serializable {
  static final long serialVersionUID = 3L;

  protected int id;
  protected User user;


  public RolePK() {
  }

  public RolePK(int id, User user) {
    this.id = id;
    this.user = user;
  }

  @Override
  public int hashCode() {
    if (this.user.getUsername() == null) {
      return 0;
    }
    return this.user.getUsername().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (o instanceof RolePK) {
      RolePK rolePk = (RolePK) o;
      return (this.user.getUsername().compareTo(rolePk.user.getUsername()) == 0
        && this.id == rolePk.id);
    }
    return false;
  }
}


