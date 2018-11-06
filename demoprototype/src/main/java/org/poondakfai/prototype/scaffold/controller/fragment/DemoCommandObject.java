package org.poondakfai.prototype.scaffold.controller.fragment;


import org.poondakfai.prototype.scaffold.model.User;
import org.poondakfai.prototype.scaffold.model.Role;
import org.poondakfai.prototype.scaffold.model.Application;
import org.poondakfai.prototype.scaffold.webgui.form.model.ICommandObject;


public class DemoCommandObject implements ICommandObject {
  private String[] actionUrls = null;
  private char actionCode = ' ';
  private String op = "";
  private User user;
  private Role role;
  private Application application;

  // In most case this method is rubbish
  // Thymeleaf set new object property for us
  public void initialize() {
    this.user = new User();
    this.role = new Role();
  }

  // This alias is maldatory because of thymleaf template
  public User getUser() {
    return user;
  }

  // This alias is maldatory because of thymleaf template
  public void setUser(User user) {
    this.user = user;
  }

  // Framework common name - @TODO refactor it to reflection code
  public Object getRoot() {
    return this.user;
  }

  public Class getRootClass() {
    return User.class;
  }

  public Role getAuthorities() {
    return role;
  }

  public Application getAuthoritiesApplications() {
    return this.application;
  }

  public String[] getActionUrls() {
    return this.actionUrls;
  }

  public char getActionCode() {
    return this.actionCode;
  }

  public String getOp() {
    return op;
  }

  public String getFormaction() {
    if (this.op.compareTo("c") == 0) {
      return "create";
    }
    if (this.op.compareTo("u") == 0) {
      return "update";
    }
    return "";
  }

  public void setRoot(Object root) {
    if (root != null && User.class.isAssignableFrom(root.getClass())) {
      this.user = (User)root;
    }
  }

  public void setAuthorities(Role role) {
    this.role = role;
  }

  public void setAuthoritiesApplications(Application application) {
    this.application = application;
  }

  public void setActionUrls(String[] actionUrls) {
    this.actionUrls = actionUrls;
  }

  public void setOp(String op) {
    this.op = op;
  }

  public void setActionCode(char actionCode) {
    this.actionCode = actionCode;
  }
}


