package org.poondakfai.prototype.scaffold.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.FetchType;
import javax.persistence.CascadeType;
import java.util.Set;
import java.util.HashSet;


@Entity
@IdClass(ApplicationPK.class)
public class Application {
  static final long serialVersionUID = 2L;

  @Column(unique = false)
  private int startMileStone;

  @Column(unique = false)
  private int endMileStone;

  @Column(unique = false)
  private String note;

  @Id
  private int id;

  @Id
  @ManyToOne
  @JoinColumns({
    @JoinColumn(name = "rid", referencedColumnName="id"),
    @JoinColumn(name = "uid", referencedColumnName="uid")
  })
  private Role role;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "application",
    cascade = {CascadeType.ALL}, orphanRemoval = true)
  private Set<Domain> domains;


  public Application() {
    this.domains = new HashSet<Domain>();
  }

  public int getStartMileStone() {
    return this.startMileStone;
  }

  public int getEndMileStone() {
    return this.endMileStone;
  }

  public String getNote() {
    return this.note;
  }

  public int getId() {
    return this.id;
  }

  public Role getRole() {
    return this.role;
  }

  public Set<Domain> getDomains() {
    return this.domains;
  }

  public void setStartMileStone(int startMileStone) {
    this.startMileStone = startMileStone;
  }

  public void setEndMileStone(int endMileStone) {
    this.endMileStone = endMileStone;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setRole(Role role) {
    this.role = role;
    this.role.getApplications().add(this);
  }

  public void setDomains(Set<Domain> domains) {
    this.domains.retainAll(domains);
    this.domains.addAll(domains);
    for (Domain domain : this.domains) {
      if (domain.getApplication() != this) {
        domain.setApplication(this);
      }
    }
  }
}


