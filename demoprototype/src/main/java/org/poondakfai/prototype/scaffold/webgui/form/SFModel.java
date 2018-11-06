package org.poondakfai.prototype.scaffold.webgui.form;


import java.util.StringTokenizer;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Modifier;
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

  public ObjectIdentifier getRoot() {
    ObjectIdentifier[] a = this.getPath();
    return a.length > 0 ? a[0] : null;
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
      buffer.append(curObj.getName());
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

  public String[] getActionUrls() {
    String url0 = this.getRequest().getServletPath();
    String[] result = new String[] {url0};
    Class targetClass = this.getTargetObjectClass();
    if (targetClass == null) {
      return result;
    }
    Method[] methods = targetClass.getMethods();
    String[] propNames = new String[methods.length];
    int propNameCount = 0;
    int i = 0;
    for (Method m : methods) {
      String name = m.getName();
      if (name.startsWith("get")
        && Iterable.class.isAssignableFrom(m.getReturnType())) {
        propNames[propNameCount++] = Character.toLowerCase(name.charAt(3))
          + name.substring(4, name.length());
      }
    }
    result =  new String[propNameCount + 2];
    result[0] = url0;
    for (i = 0; i < propNameCount; i++){
      result[i + 1] = url0 + "/" + propNames[i];
      //System.out.println("\t\t\t" + result[i + 1]);
    }
    result[i + 1] = this.getParentUrl();
    return result;
  }

  public Class getTargetObjectClass() {
    Class clazz = this.getCmdobj().getRootClass();
    // Travel the path to get the target object class
    ObjectIdentifier[] a = this.getPath();
    int n = a.length;
    int i;
    boolean isSuccessfull = true;

    for (i = 1; i < n; i++) {
      String name = a[i].getName();
      int len = name.length();
      String getterMethodName = "get"
        + Character.toUpperCase(name.charAt(0))
        + name.substring(1, len);
      try {
        Method m = clazz.getDeclaredMethod(getterMethodName);
        if (Modifier.isPublic(m.getModifiers())) {
          Type type = m.getGenericReturnType();
          if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            if(paramType.getActualTypeArguments().length == 1) {
              Type elementType = paramType.getActualTypeArguments()[0];
              String elementClassName = elementType.getTypeName();
              clazz = Class.forName(elementClassName);
            }
          }
        }
      }
      catch(Exception e) {
        isSuccessfull = false;
        break;
      }
    }
    if (isSuccessfull) {
      return clazz;
    }
    return null;
  }

  public ObjectIdentifier getTargetObjectIdentifier() {
    ObjectIdentifier[] a = this.getPath();
    return a[a.length - 1];
  }
}


