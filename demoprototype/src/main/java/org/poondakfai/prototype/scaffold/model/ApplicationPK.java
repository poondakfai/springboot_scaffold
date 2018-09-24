package org.poondakfai.prototype.scaffold.model;


import java.io.Serializable;


public class ApplicationPK implements Serializable {
  static final long serialVersionUID = 4L;

  protected int id;
  protected Role role;

  public ApplicationPK() {
  }

  public ApplicationPK(int id, Role role) {
    this.id = id;
    this.role = role;
  }

  @Override
  public int hashCode() {
    if (this.role.getUser() == null) {
      return 0;
    }
    if (this.role.getUser().getUsername() == null) {
      return 0;
    }
    return this.role.getUser().getUsername().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (o instanceof ApplicationPK) {
      ApplicationPK pk = (ApplicationPK) o;
      boolean roleEqual = (pk.role == null && this.role == null) ? true : false;
      if (roleEqual) {
        return this.id == pk.id;
      }
      if (pk.role == null || this.role == null) {
        return false;
      }
      roleEqual = (pk.role.getUser() == null && this.role.getUser() == null)
        ? true : false;
      if (roleEqual) {
        return this.id == pk.id;
      }
      if (pk.role.getUser() == null || this.role.getUser() == null) {
        return false;
      }
      roleEqual = (pk.role.getUser().getUsername() == null
        && this.role.getUser().getUsername() == null) ? true : false;
      if (roleEqual) {
        return this.id == pk.id;
      }
      if (pk.role.getUser().getUsername() == null
        || this.role.getUser().getUsername() == null) {
        return false;
      }
      roleEqual = pk.role.getUser().getUsername().compareTo(
          this.role.getUser().getUsername()) == 0;
      return roleEqual && (this.id == pk.id);
    }
    return false;
  }
}


