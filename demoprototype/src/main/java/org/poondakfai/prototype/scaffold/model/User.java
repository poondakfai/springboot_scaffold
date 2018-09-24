package org.poondakfai.prototype.scaffold.model;


import java.util.Set;
import java.util.HashSet;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;
import javax.persistence.CascadeType;


@Entity(name = "Account")
public class User {
  static final long serialVersionUID = 1L;

  @Id
  private String username;

  @Column(unique = false)
  private String password;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "user",
    cascade = {CascadeType.ALL}, orphanRemoval = true)
  private Set<Role> authorities;


  public User() {
    this.authorities = new HashSet<Role>();
  }

  public Set<Role> getAuthorities() {
    return this.authorities;
  }

  public String getPassword() {
    return this.password;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setAuthorities(Set<Role> authorities) {
    this.authorities.retainAll(authorities);
    this.authorities.addAll(authorities);
    for (Role role : this.authorities) {
      if (role.getUser() != this) {
        role.setUser(this);
      }
    }
  }
}


