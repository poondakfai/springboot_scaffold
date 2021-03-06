package org.poondakfai.prototype.scaffold.webgui.form;


import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.persistence.OneToMany;
import org.springframework.beans.BeanUtils;
import org.poondakfai.prototype.scaffold.webgui.form.model.ICommandObject;
import org.poondakfai.prototype.scaffold.webgui.form.model.Utilities;
import java.lang.reflect.ParameterizedType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import org.springframework.core.convert.ConversionService;
import java.util.Collection;


public class CommandObjectPropertyUtils {
  // Terms legend:
  //   verb  copy: from flat property to session object
  //         load: from session object to flat property
  private static final boolean TRACE_ENABLE = !false;
  private static final CommandObjectPropertyUtils singleObject;
  public Utilities utility;


  private CommandObjectPropertyUtils() {
  }

  public static CommandObjectPropertyUtils getSingletonInstance() {
    return CommandObjectPropertyUtils.singleObject;
  }

  public Object decodeRootKey(
    ICommandObject cmdobj,
    String encodedKey,
    ConversionService conversionService
  ) {
    Object result = null;
    // Find the target object class type
    if (cmdobj == null) {
      if (TRACE_ENABLE) {
        System.out.println("decodeRootKey: cmdobj is null");
      }
      return null;
    }
    Class targetObjClass = cmdobj.getRootClass();

    // Determite if targetObjClass key is single property or multiple property?
    IdClass idClass = (IdClass) targetObjClass.getAnnotation(IdClass.class);
    if (idClass == null) {
      if (TRACE_ENABLE) {
        System.out.println("decodeRootKey: Single property key");
      }
      // Find field key in class key list
      Field[] fields = targetObjClass.getDeclaredFields();
      for (Field field : fields) {
        Id keyAnnotation = (Id) field.getAnnotation(Id.class);
        if (keyAnnotation != null) {
          // This is key field because it is annotated by @Id
          // Let's extract the field value presentated in String type
          Class<?> keyClass = field.getType();
          String fieldName = field.getName();
          String fieldValue = this.extractEncodedRootKey(
            fieldName, encodedKey.toCharArray());
          try {
            result = conversionService.convert(fieldValue, keyClass);
          }
          catch(Exception e) {
            result = null;
          }
          if (TRACE_ENABLE) {
            if (result == null) {
              System.out.println("decodeRootKey: could not convert value '"
                + fieldValue
                + "'@class-"
                + keyClass
              );
            }
            else {
              System.out.println(
                "decodeRootKey: Single property key value is '"
                + result
                + "'@class-"
                + result.getClass().getSimpleName()
              );
            }
          }
        }
      }
    }
    else {
      if (TRACE_ENABLE) {
        System.out.println("decodeRootKey: Multiple properties key");
      }
      // @TODO Implement multiple properties key or null will be returned.
    }
    return result;
  }

  public boolean copyRootFlatPropertyToSession(Object sessionObject,
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

  public boolean copyChildObjectToSession(Object sessionObject,
    SFModel model) {
    StringBuffer sb = new StringBuffer("get");
    Object sObj = travelSessionObjectFromRootToParent(sessionObject, model, sb);
    if (sObj == null) {
      return false;
    }
    Class sObjClass = sObj.getClass();
    String fProp = sb.toString();
    sb.delete(0, sb.length());
    String lastProp = model.getPath()[model.getPath().length - 1].getName();

    // Retrieve destination setter property name of session object
    //   lastProp:     most child property of session object
    //   hookPropName: Sitting at source object class
    //                 compute the property name to set the destination
    // Q: Why we do not use spring bean utils? A: since @OneToMany
    String hookPropName = "";
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
        model.getCmdobj().getClass(), fProp);
      if (m.getParameterCount() == 0) {
        postedObj = m.invoke(model.getCmdobj());
        postedObjClass = m.getReturnType();
      }
      else {
        if (TRACE_ENABLE) {
          System.out.println("Invalid method " + fProp);
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

  public boolean removeChildObjectFromSession(Object sessionObject,
    SFModel model, Object childObject) {
    Object sObj = travelSessionObjectFromRootToParent(
      sessionObject,
      model,
      null
    );
    if (sObj == null) {
      return false;
    }
    Class sObjClass = sObj.getClass();
    String lastProp = model.getPath()[model.getPath().length - 1].getName();
    String methodName = "get" + Character.toUpperCase(
      lastProp.charAt(0)) + lastProp.substring(1, lastProp.length()
    );
    try {
      Method m = null;
      m = BeanUtils.findMethodWithMinimalParameters(sObjClass, methodName);
      if (m.getParameterCount() == 0) {
        Object postedObj = m.invoke(sObj);
        Class postedObjClass = m.getReturnType();
        if (Collection.class.isAssignableFrom(postedObjClass)) {
          Collection collection = (Collection) postedObj;
          collection.remove(childObject); // @TODO review me, how orphaned object is reflected in database ? Is it removed?
        }
      }
      else {
        if (TRACE_ENABLE) {
          System.out.println("Invalid method: " + methodName);
        }
      }
    }
    catch(Exception e) {
      if (TRACE_ENABLE) {
        e.printStackTrace();
      }
    }
    // System.out.println("[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[");
    // System.out.println("obj:      " + sObj.toString());
    // System.out.println("objClass: " + sObj.getClass().toString());
    // System.out.println("lastProp: " + lastProp);
    // System.out.println("]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]");
    return true;
  }

  public boolean loadChildObject(Object childObj,
    SFModel model, Object sessionObject) {
    StringBuffer fProp = new StringBuffer("set");
    travelSessionObjectFromRootToParent(sessionObject, model, fProp);

    // load child object by invoking setter methods
    try {
      Method m = BeanUtils.findMethodWithMinimalParameters(
        model.getCmdobj().getClass(),
        fProp.toString()
      );
      if (m.getParameterCount() == 1) {
        m.invoke(sessionObject, childObj);
      }
      else {
        if (TRACE_ENABLE) {
          System.out.println("Invalid method " + fProp);
        }
        return false;
      }
    }
    catch(Exception e) {
      if (TRACE_ENABLE) {
        e.printStackTrace();
      }
      return false;
    }
    return true;
  }

  private Object travelSessionObjectFromRootToParent(Object sessionObject,
    SFModel model,
    StringBuffer fProp
  ) {
    StringBuffer sb = new StringBuffer();
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
      if (fProp != null && !isFirst) {
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
          if (Iterable.class.isAssignableFrom(sObjClass)) {
            Type type = m.getAnnotatedReturnType().getType();
            if (type instanceof ParameterizedType) {
              ParameterizedType ptype = (ParameterizedType) type;
              if (ptype.getActualTypeArguments().length > 0) {
                Type atype = ptype.getActualTypeArguments()[0];
                String typeName = atype.getTypeName();
                try {
                  Class targetClass = Class.forName(typeName);
                  Integer targetKey = new Integer(oi.getId());
                  sObj = utility.getKeyPool().lookup(targetClass, targetKey);
                  sObjClass = targetClass;
                } catch (Exception e) {
                }
              }
            }
          }
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
    if (TRACE_ENABLE) {
      System.out.println("Flat command object property: "
        + (fProp == null ? "null" : fProp.toString())
      );
    }
    return sObj;
  }

  private String extractEncodedRootKey(
    String fieldName,
    char[] encodedBuffer
  ) {
    int phase;
    int strLen = encodedBuffer.length;
    int idx = 0;
    int preIdx = idx;
    String prop = null;
    String value = null;
    int valueLen = 0;

    while(idx < strLen) {
      for (phase = 0; phase < 3; phase++) {
        if (phase < 2) {
          for (idx = preIdx; idx < strLen && encodedBuffer[idx] != '_'; idx++);
          if (phase == 0) {
            prop = String.copyValueOf(encodedBuffer, preIdx, (idx - preIdx));
          }
          else {
            String str = String.copyValueOf(
              encodedBuffer, preIdx, (idx - preIdx));
            valueLen =  Integer.parseInt(str, 10);
          }
          preIdx = idx + 1;
        }
        else {
          value = String.copyValueOf(encodedBuffer, preIdx, valueLen);
          idx = preIdx + valueLen + 1; // exclude next token '_' -> + 1
          preIdx = idx;
        }
      }
      if (TRACE_ENABLE) {
        if (fieldName.compareTo(prop) == 0) {
          System.out.println("\nProperty found");
        }
        else {
          System.out.println("\nProperty not found");
        }
        System.out.println(
          "Prop: " + prop
          + "\nValue: " + value
          + "\nLen: " + valueLen
        );
      }
      if (fieldName.compareTo(prop) == 0) {
        return value;
      }
    }
    return null;
  }

  static {
    singleObject = new CommandObjectPropertyUtils();
  }
}


