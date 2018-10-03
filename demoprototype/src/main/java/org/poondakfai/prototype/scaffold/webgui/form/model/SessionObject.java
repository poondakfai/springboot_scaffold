package org.poondakfai.prototype.scaffold.webgui.form.model;


public class SessionObject {
  private ICommandObject cmdobj;
  private Utilities utilities;


  public SessionObject(ICommandObject cmdobj, Utilities utilities) {
    this.cmdobj = cmdobj;
    this.utilities = utilities;
  }

  public ICommandObject getCmdobj() {
    return this.cmdobj;
  }

  public Utilities getUtilities() {
    return this.utilities;
  }
}


