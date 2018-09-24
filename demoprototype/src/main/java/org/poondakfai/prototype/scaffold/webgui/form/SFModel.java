package org.poondakfai.prototype.scaffold.webgui.form;


import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.poondakfai.prototype.scaffold.webgui.form.model.ICommandObject;
import static org.springframework.web.servlet.HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE;


public class SFModel {
  private ObjectIdentifier[] path;
  private char operationCode;
  private ICommandObject cmdobj;
  private ModelMap model;
  private RedirectAttributes redirectAttrs;
  private HttpServletRequest request;
  private SessionStatus status;


  public SFModel(
    String operation,
    ICommandObject cmdobj,
    ModelMap model,
    RedirectAttributes redirectAttrs,
    HttpServletRequest request,
    SessionStatus status
  ) {
    // path compute
    int i;
    int objIdsCount = 0;
    ObjectIdentifier[] objIds = new ObjectIdentifier[8];
    final String path = (String) request.getAttribute(
      PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    StringTokenizer strToken = new StringTokenizer(path);
    while(strToken.hasMoreTokens()) {
      ObjectIdentifier objId = new ObjectIdentifier(
        strToken.nextToken("/"), "");
      if (strToken.hasMoreTokens()) {
        objId.setId(strToken.nextToken("/"));
      }
      objIds[objIdsCount++] = objId;
    }

    // prepare attribute
    this.operationCode = operation.length() > 0 ? operation.charAt(0) : ' ';
    this.cmdobj = cmdobj;
    this.model = model;
    this.redirectAttrs = redirectAttrs;
    this.request = request;
    this.status = status;
    this.path = new ObjectIdentifier[objIdsCount];
    for(i = 0; i < objIdsCount; i++) {
      this.path[i] = objIds[i];
    }

    // System.out.println("[-----]");
    // for (i = 0; i < this.path.length; i++) {
    //   System.out.println(this.path[i]);
    // }
    // System.out.println("[-----]"+this.operationCode);
  }

  public ObjectIdentifier[] getPath() {
    return this.path;
  }

  public char getOperationCode() {
    return this.operationCode;
  }

  public ICommandObject getCmdobj() {
    return this.cmdobj;
  }

  public ModelMap getModel() {
    return this.model;
  }

  public RedirectAttributes getRedirectAttrs() {
    return this.redirectAttrs;
  }

  public HttpServletRequest getRequest() {
    return this.request;
  }

  public SessionStatus getStatus() {
    return this.status;
  }

  public boolean isRoot() {
    return this.getPath().length <= 1;
  }

  public String getRoot() {
    ObjectIdentifier[] a = this.getPath();
    return a.length > 0 ? a[0].getName() : "";
  }

  public String getHomeUrl() {
    ObjectIdentifier[] a = this.getPath();
    return a.length > 0 ? "/" + a[0].getName() + "/" + a[0].getId() : "";
  }

  public String getParentUrl() {
    StringBuffer buffer = new StringBuffer();
    ObjectIdentifier[] a = this.getPath();
    int i;
    int n = a.length - 1;
    for (i = 0; i < n; i++) {
      buffer.append('/');
      buffer.append(a[i].getName());
      buffer.append('/');
      buffer.append(a[i].getId());
    }
    return buffer.toString();
  }

  public String getViewTemplate() {
    char op = this.getOperationCode();
    StringBuffer buffer = new StringBuffer();

    for (ObjectIdentifier curObj : getPath()) {
      buffer.append(this.getObjectIdentifier().getName());
      buffer.append('/');
    }
    if (op == 'c' || op == 'u') {
      buffer.append("cu");
    }
    else {
      buffer.append(op);
    }
    buffer.append(".html");
    // System.out.println("TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
    // System.out.println(buffer.toString());
    // System.out.println("TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
    return buffer.toString();
  }

  private ObjectIdentifier getObjectIdentifier() {
    ObjectIdentifier[] a = this.getPath();
    return a[a.length - 1];
  }
}


