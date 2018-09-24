package org.poondakfai.prototype.scaffold.model;


import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumns;
import javax.persistence.JoinColumn;
import javax.persistence.Column;


@Entity
@IdClass(DomainPK.class)
public class Domain {
  static final long serialVersionUID = 2L;

  @Id
  private int id;

  @Column(unique = false)
  private String note;

  @Id
  @ManyToOne
  @JoinColumns({
    @JoinColumn(name = "aid", referencedColumnName="id"),
    @JoinColumn(name = "rid", referencedColumnName="rid"),
    @JoinColumn(name = "uid", referencedColumnName="uid")
  })
  private Application application;


  public int getId() {
    return this.id;
  }

  public String getNote() {
    return this.note;
  }

  public Application getApplication() {
    return this.application;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public void setApplication(Application application) {
    this.application = application;
    this.application.getDomains().add(this);
  }
}


