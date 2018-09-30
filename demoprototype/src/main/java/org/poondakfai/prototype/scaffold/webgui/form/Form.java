package org.poondakfai.prototype.scaffold.webgui.form;


import javax.servlet.http.HttpServletRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.core.convert.ConversionService;
import org.poondakfai.prototype.scaffold.webgui.form.model.ICommandObject;
import org.poondakfai.prototype.scaffold.webgui.form.model.SessionCommandObject;
import org.poondakfai.prototype.scaffold.webgui.form.model.KeyPool;


public class Form<T, ID> {
  public static final String OP_CODE = "op";
  private Class cmdObjClass;
  private CrudRepository<T, ID> repository;
  private String name;
  private ConversionService conversionService;


  public Form(
    Class cmdObjClass,
    CrudRepository<T, ID> repository,
    String name,
    ConversionService conversionService
  ) {
    this.cmdObjClass = cmdObjClass;
    this.repository = repository;
    this.name = name;
    this.conversionService = conversionService;
  }

  public CrudRepository<T, ID> getRepository() {
    return this.repository;
  }

  public String process(
    final String op,
    final ICommandObject cmdobj,          // Type declare should be specified
    ModelMap model,
    RedirectAttributes reAttrs,
    final HttpServletRequest req,
    SessionStatus status
  ) {
    String result = "";
    SFModel o = new SFModel(op, cmdobj, model, reAttrs, req, status);

    if (o.isRoot()) {
      switch (o.getOperationCode()) {
        case 'l':
          result = listRootPageShow(o);
          break;

        case 'c': // 'p' persist
          result = createRootPageShow(o);
          break;

        case 'r':
        case 'u':
          result = detailRootPageShow(o);
          break;

        case 'd':
          break;

        case 'p':
          result = createRootPageDo(o);
          break;

        default:
          break;
      }
    }
    else {
      switch (o.getOperationCode()) {
        case 'l':
          break;

        case 'c':
          result = createChildPageShow(o);
          break;

        case 'r':
          break;

        case 'u':
          break;

        case 'd':
          break;

        default:
          break;
      }
    }
    return result;
  }

  private String getSessionAttributeName() {
    return this.name;
  }

  private SessionCommandObject getTargetObject(HttpServletRequest req) {
    SessionCommandObject result = (SessionCommandObject)req.getSession()
      .getAttribute(this.getSessionAttributeName());
    if (result == null) {
      try {
        ICommandObject cmdobj = (ICommandObject) BeanUtils
          .instantiateClass(this.cmdObjClass);
        System.out.println("Create new session object");
        // Default op is 'c' if other is expected??? REVIEWED ME
        // System.out.println("Cheating, this session is created as update before");
        cmdobj.setOp("c");
        cmdobj.setActionCode('c');
        result = new SessionCommandObject(cmdobj, new KeyPool());
        req.getSession().setAttribute(this.getSessionAttributeName(), result);
        System.out.println("Create new session object DONE");
      }
      catch(BeanInstantiationException e) {
        // e.printStackTrace();
        result = null;
      }
    }
    return result;
  }

  private String listRootPageShow(SFModel model) {
    System.out.println("private String listRootPageShow(SFModel model)");
    listPageShow(model.getModel(), model.getStatus());
    if (model.getRequest().getSession(false) != null) {
      model.getRequest().getSession(false).invalidate();
    }
    return model.getViewTemplate();
  }

  private String detailRootPageShow(SFModel model) {
    System.out.println("private String detailRootPageShow(SFModel model)");
    ICommandObject sobj = getTargetObject(model.getRequest()).getCmdobj();
    ObjectIdentifier oi = model.getRoot();
    Object rootKey = CommandObjectPropertyUtils.decodeRootKey(
      model.getCmdobj(),
      oi.getId(),
      this.conversionService
    );
    T targetObj = this.getRepository().findById((ID) rootKey).orElse(null);
    sobj.setRoot(targetObj);
    model.getModel().addAttribute("cmdobj", sobj);
    String[] strs =  new String[2];
    strs[0] = model.getRequest().getServletPath();
    strs[1] = model.getRequest().getServletPath() + "/authorities/_";
    sobj.setActionUrls(strs);
    // Load object
    return model.getViewTemplate();
  }

  // [List Form] - show
  private String listPageShow(
    ModelMap model,
    SessionStatus status
  ) {
    // Clear session
    // Load model from JPA
    // create model attribute variable
    // Render model flat properties by template
    // status.setComplete();

    // Iterable<User> usersdatasource = this.getRepository().findAll();
    Iterable<T> usersdatasource = this.getRepository().findAll();
    model.addAttribute("usersdatasource", usersdatasource);
    //return "users";
    return "";
  }

  private String createRootPageShow(SFModel model) {
    System.out.println("private String createRootPageShow(SFModel model)");
    createPageShow(model.getModel(), model.getRedirectAttrs(), model.getRequest());

    ICommandObject sobj = getTargetObject(model.getRequest()).getCmdobj();
    String[] strs =  new String[2];
    strs[0] = model.getRequest().getServletPath();
    strs[1] = model.getRequest().getServletPath() + "/authorities/_";
    sobj.setActionUrls(strs);
    return model.getViewTemplate();
  }

  // [Create Form] - show
  public String createPageShow(
    ModelMap model,
    RedirectAttributes reAttrs,
    final HttpServletRequest req) { // @TODO review Will HttpServletRequest req be use?
    // Retrieve session object
    // Update op for session object
    // Create model attribute variable from session object
    // Render model form flat properties and detail level 1 properties

    // Get session object
    ICommandObject sobj = getTargetObject(req).getCmdobj();
    // Cheating process detect
    model.addAttribute("cmdobj", sobj); // USE THIS FOR ENTRY POINT SUB FORM
    //return "user";
    return "";
  }

  private String createRootPageDo(SFModel model) {
    System.out.println("private String createRootPageDo(SFModel model)");
    createPageDoPersist(
      model.getCmdobj(),
      model,
      model.getRedirectAttrs(),
      model.getRequest(),
      model.getStatus()
    );
    model.getRedirectAttrs().addAttribute("op", "l");
    return "redirect:" + model.getHomeUrl();
  }

  // [Create Form] - 'create' click
  private String createPageDoPersist(
    final ICommandObject cmdobj,
//    ModelMap model,
    SFModel model,
    RedirectAttributes reAttrs,
    final HttpServletRequest req,
    SessionStatus status) {
    // Retrieve session object
    // Update flat properties for session object with data provide from Cmd Object
    // Do persist and test result
    // Optional clear data in the session object (prevent security risk)
    // Redirect to parent page

    System.out.println("[Create Form] - create click");

    ICommandObject sobj = getTargetObject(req).getCmdobj();
    if (CommandObjectPropertyUtils.copyRootFlatPropertyToSession(sobj, model)) {
      try {
 
        this.getRepository().save((T) sobj.getRoot());
        System.out.println("-> Persist new user is ok");
        if (req.getSession(false) != null) {
          req.getSession(false).invalidate();
        }
        System.out.println("-> Clear session is ok");
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

    reAttrs.addAttribute("id", "");
    reAttrs.addAttribute("op", "l");
    return "";
  }

  private String createChildPageShow(SFModel model) {
    System.out.println("private String createChildPageShow(SFModel model)");
    ICommandObject sobj = getTargetObject(model.getRequest()).getCmdobj();

    if (!CommandObjectPropertyUtils.copyChildObjectToSession(sobj, model)) {
      return "redirect:" + model.getParentUrl();
    }
    model.getRedirectAttrs().addAttribute("op", "c"); // how to retrieve this??
    return "redirect:" + model.getParentUrl();
  }
}


