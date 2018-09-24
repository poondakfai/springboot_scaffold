package org.poondakfai.prototype.scaffold.model;


import java.io.Serializable;


public class DomainPK implements Serializable {
  static final long serialVersionUID = 4L;

  protected int id;
  protected Application application;

  public DomainPK() {
  }

  public DomainPK(int id, Application application) {
    this.id = id;
    this.application = application;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (o instanceof DomainPK) {
      DomainPK pk = (DomainPK) o;
      boolean appEqual = (pk.application == null
        && this.application == null) ? true : false;
      if (appEqual) {
        return this.id == pk.id;
      }
      if (pk.application == null || this.application == null) {
        return false;
      }
      appEqual = (pk.application.getRole() == null
        && this.application.getRole() == null) ? true : false;
      if (appEqual) {
        return this.id == pk.id;
      }
      if (pk.application.getRole() == null
        || this.application.getRole() == null) {
        return false;
      }
      ApplicationPK me = new ApplicationPK(this.id, this.application.getRole());
      ApplicationPK other = new ApplicationPK(pk.id, pk.application.getRole());
      return me.equals(other);
    }
    return false;
  }
}


