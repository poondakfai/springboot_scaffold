package org.poondakfai.prototype.scaffold.webgui.form.model;


public interface ICommandObject {
  public String[] getActionUrls();
  public char getActionCode();
  public String getOp();
  public String getFormaction();
  public Object getRoot();

  public void setRoot(Object root);
  public void setActionUrls(String[] actionUrls);
  public void setOp(String op);
  public void setActionCode(char actionCode);
}


