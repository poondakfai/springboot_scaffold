package org.poondakfai.prototype.scaffold.webgui.form.model;


public class SessionCommandObject {
  private ICommandObject cmdobj;
  private KeyPool keyPool;


  public SessionCommandObject(ICommandObject cmdobj, KeyPool keyPool) {
    this.cmdobj = cmdobj;
    this.keyPool = keyPool;
  }

  public ICommandObject getCmdobj() {
    return this.cmdobj;
  }

  public KeyPool getKeyPool() {
    return this.keyPool;
  }
}


