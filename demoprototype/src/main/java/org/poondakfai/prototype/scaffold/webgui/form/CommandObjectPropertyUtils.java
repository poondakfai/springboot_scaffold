package org.poondakfai.prototype.scaffold.webgui.form;


import java.lang.reflect.Method;
import java.lang.reflect.Field;
import javax.persistence.OneToMany;
import org.springframework.beans.BeanUtils;
import org.poondakfai.prototype.scaffold.webgui.form.model.ICommandObject;
import java.lang.reflect.ParameterizedType;


public class CommandObjectPropertyUtils {
  private static boolean TRACE_ENABLE = !false;


  public static boolean copyRootFlatPropertyToSession(Object sessionObject,
    SFModel model) {
    ICommandObject cmdobj = model.getCmdobj();
    if (cmdobj == null || sessionObject == null
      || !(ICommandObject.class.isAssignableFrom(sessionObject.getClass()))) {
      return false;
    }
    Object sObj = cmdobj.getRoot();
    Object dObj = ((ICommandObject) sessionObject).getRoot();
    if (dObj == null || sObj == null) {
      return false;
    }
    Class dClass = dObj.getClass();
    Class sClass = sObj.getClass();
    Method[] methods = sClass.getMethods();
    for (Method m : methods) {
      String methodName = m.getName();
      if (methodName.startsWith("get")) {
        if (!methodName.startsWith("getClass")) {
          boolean isCollection = false;
          if (m.getGenericReturnType() instanceof ParameterizedType) {
            ParameterizedType t = (ParameterizedType) m.getGenericReturnType();
            try {
              Class clazz = Class.forName(t.getRawType().getTypeName());
              if (Iterable.class.isAssignableFrom(clazz)) {
                isCollection = true;
              }
            }
            catch(Exception e) {
              isCollection = true; // by pass process this method
            }
          }
          if (!isCollection) {
            Method sm = BeanUtils.findMethodWithMinimalParameters(
              sClass, "set" + methodName.substring(3, methodName.length()));
            try {
              if (m.getParameterCount() == 0) {
                Object s = m.invoke(sObj);
                if (s != null) {
                  if (sm.getParameterCount() == 1) {
                    sm.invoke(dObj, s);
                  }
                }
              }
              else {
                if (TRACE_ENABLE) {
                  System.out.println("Invalid method: " + sm.getName());
                }
              }
            }
            catch(Exception e) {
              if (TRACE_ENABLE) {
                e.printStackTrace();
              }
            }
          }
        }
      }
    }
    return true;
  }

  public static boolean copyChildObjectToSession(Object sessionObject,
    SFModel model) {
    StringBuffer sb = new StringBuffer();
    StringBuffer fProp = new StringBuffer();
    String lastProp = null;
    Object sObj = sessionObject;
    Object sBakObj = null;
    Class sObjClass = model.getCmdobj().getClass();
    Class sBakClass = sObjClass;
    boolean isFirst = true;

    if (TRACE_ENABLE) {
      System.out.println(
        "##############################################################");
    }
    // Travel sessionObject to
    //  . Src - fProp: Compute flat property name of command object
    //  . Des - sObj:  Retrieve the parent object of flat property
    //                 parent object: nested property of session object
    //  . sObjClass:   Class type of sObj
    fProp.append("get");
    if (TRACE_ENABLE) {
     System.out.println("Travel session object methods:");
    }
    for (ObjectIdentifier oi : model.getPath()) {
      // compute getter method name to travel the session object
      sb.append("get");
      lastProp = oi.getName();
      sb.append(Character.toUpperCase(lastProp.charAt(0)));
      sb.append(lastProp.substring(1));
      if (TRACE_ENABLE) {
       System.out.println("\t" + sb.toString());
      }

      // flat property name compute
      if (!isFirst) {
        fProp.append(Character.toUpperCase(oi.getName().charAt(0)));
        fProp.append(oi.getName().substring(1));
      }
      isFirst = false;

      // trave one step property of session object
      try {
        Method m = BeanUtils.findMethodWithMinimalParameters(sObjClass,
          sb.toString());
        if (m.getParameterCount() == 0) {
          sBakObj = sObj;
          sObj = m.invoke(sObj);
          sBakClass = sObjClass;
          sObjClass = sObj.getClass();
        }
        else {
          if (TRACE_ENABLE) {
            System.out.println("Getter property method is not found "
              + sb.toString());
          }
          sObj = null;
        }
      }
      catch(Exception e) {
        if (TRACE_ENABLE) {
          System.out.println("Error while finding getter property method: "
            + e.getMessage());
        }
        sObj = null;
      }
      sb.delete(0, sb.length());
    }
    sObj = sBakObj;
    sObjClass = sBakClass;
    if (TRACE_ENABLE) {
      System.out.println("Flat command object property: " + fProp.toString());
    }

    // Retrieve destination setter property name of session object
    //   lastProp:     most child property of session object
    //   hookPropName: Sitting at source object class
    //                 compute the property name to set the destination
    String hookPropName = "";
    if (sObjClass != null && lastProp != null) {
      try {
        Field f = sObjClass.getDeclaredField(lastProp);
        OneToMany annotation = (OneToMany) f.getAnnotation(OneToMany.class);
        hookPropName = annotation.mappedBy();
      }
      catch(Exception e) {
        if (TRACE_ENABLE) {
          e.printStackTrace();
        }
        return false;
      }
    }
    else {
      return false;
    }
    sb.delete(0, sb.length());
    sb.append("set");
    sb.append(Character.toUpperCase(hookPropName.charAt(0)));
    sb.append(hookPropName.substring(1));

    if (TRACE_ENABLE) {
      System.out.println("Session object hook property name: " + hookPropName);
      System.out.println("Session object hook property setter method name: "
        + sb.toString());
    }

    // Find the flat source property getter method
    Object postedObj = null;
    Class postedObjClass = null;
    try {
      Method m = null;
      m = BeanUtils.findMethodWithMinimalParameters(
        model.getCmdobj().getClass(), fProp.toString());
      if (m.getParameterCount() == 0) {
        postedObj = m.invoke(model.getCmdobj());
        postedObjClass = m.getReturnType();
      }
      else {
        if (TRACE_ENABLE) {
          System.out.println("Invalid method " + fProp.toString());
        }
      }
    }
    catch(Exception e) {
      if (TRACE_ENABLE) {
        e.printStackTrace();
      }
    }
    if (postedObj == null) {
      if (TRACE_ENABLE) {
        System.out.println("NULL SOURCE");
      }
      return false;
    }

    // invoke flat source property setter method to hook itself to source object
    try {
      // compute setter method of source property object
      Method m = null;
      m = BeanUtils.findMethodWithMinimalParameters(
        postedObjClass, sb.toString());
      if (m.getParameterCount() == 1) {
        m.invoke(postedObj, sObj); // @TODO make sure desObj is not null
      }
      else {
        if (TRACE_ENABLE) {
          System.out.println("Invalid method hook: " + sb.toString());
        }
      }
    }
    catch(Exception e) {
      if (TRACE_ENABLE) {
        e.printStackTrace();
      }
    }
    if (postedObj == null) {
      if (TRACE_ENABLE) {
        System.out.println("NULL SOURCE");
      }
      return false;
    }
    if (TRACE_ENABLE) {
      System.out.println(
        "##############################################################");
    }
    return true;
  }
}


