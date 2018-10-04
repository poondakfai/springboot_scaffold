package org.poondakfai.prototype.scaffold.model;


import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;
import javax.persistence.FetchType;
import javax.persistence.CascadeType;
import java.util.Set;
import java.util.HashSet;


@Entity
@IdClass(RolePK.class)
public class Role {
  static final long serialVersionUID = 2L;

  @Id
  private int id;

  @Id
  @ManyToOne
  @JoinColumn(name = "uid")
  private User user;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "role",
    cascade = {CascadeType.ALL}, orphanRemoval = true)
  private Set<Application> applications;


  public Role(Roles role) {
    this.id = role.getId();
    this.applications = new HashSet<Application>();
  }

  public Role() {
    this(Roles.UNKNOWN);
  }

  public int getId() {
    return this.id;
  }

  public User getUser() {
    return this.user;
  }

  public Set<Application> getApplications() {
    return this.applications;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setUser(User user) {
    this.user = user;
    if (this.user != null) {
      this.user.getAuthorities().add(this);
    }
  }

  public void setApplications(Set<Application> applications) {
    this.applications.retainAll(applications);
    this.applications.addAll(applications);
    for (Application application : this.applications) {
      if (application.getRole() != this) {
        application.setRole(this);
      }
    }
  }

  @Override
  public int hashCode() {
    return this.id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (o instanceof Role) {
      Role role = (Role) o;
      if (role.id == this.id) 
        return true;

      if (this.getUser() == null && role.getUser() == null) {
        return this.id == role.id;
      }
      if (this.getUser() == null || role.getUser() == null) {
        return false;
      }
      if (this.getUser().getUsername() == null
        && role.getUser().getUsername() == null) {
        return this.id == role.id;
      }
      if (this.getUser().getUsername() == null
        || role.getUser().getUsername() == null) {
        return false;
      }
      return this.getUser().getUsername().compareTo(
        role.getUser().getUsername()) == 0 && (this.id == role.id);
    }
    return false;
  }
}


