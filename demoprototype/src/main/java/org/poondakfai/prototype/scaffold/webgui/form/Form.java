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
import org.poondakfai.prototype.scaffold.webgui.form.model.SessionObject;
import org.poondakfai.prototype.scaffold.webgui.form.model.Utilities;


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

  /**
   * This is an entry point method of Thymeleaf controller. It does:
   *     . Package requests into Scaffold framework model object
   *     . Determine target usecase base on request parameters ('op' http
   *       parameters)
   *     . Invoke usecase correspondence method
   *
   * @param  op       http 'op' parameter depicts requested perform action
   * @param  cmdobj   http form submit parameter packed by Thymeleaf framework
   * @param  model    Thymeleaf framework model data structure
   * @param  reAttrs  redirect parameters request to Thymeleaf to be fullfilled
   * @param  req      generic http servlet request supports advance operations
   * @param  status   Thymeleaf session status support session related operation
   * @return          render requested view presented by Thymeleaf expression
   */
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
          result = showRootListPage(o);
          break;

        case 'c': // next phase 'p' code to persist
          result = showRootCreatePage(o);
          break;

        case 'r':
        case 'u':
          result = showRootDetailPage(o);
          break;

        case 'd':
          result = doRootDeletePage(o);
          break;

        case 'p':
          result = doCreateRootPage(o);
          break;

        case 's':
          result = doUpdateRootPage(o);
          break;

        default:
          break;
      }
    }
    else {
      switch (o.getOperationCode()) {
        case 'c':
          result = doChildCreatePage(o);
          break;

        case 'r':
          result = showChildViewPage(o);
          break;

        case 'u':
          break;

        case 'd':
          result = doChildDeletePage(o);
          break;

        default:
          break;
      }
    }
    return result;
  }

  private String showRootListPage(SFModel model) {
    System.out.println("private String listRootPageShow(SFModel model)");
    // Clear session
    // Load model from JPA
    // create model attribute variable
    // Render model flat properties by template
    // status.setComplete();
    if (model.getRequest().getSession(false) != null) {
      model.getRequest().getSession(false).invalidate();
    }
    Iterable<T> usersdatasource = this.getRepository().findAll();
    model.getModel().addAttribute(
      this.getDataSourceAttributeName(),
      usersdatasource
    );
    return model.getViewTemplate();
  }

  private String showRootCreatePage(SFModel model) {
    System.out.println("private String createRootPageShow(SFModel model)");
    // Retrieve session object
    // (-)Not need to update op for session object (default constructor)
    // Create model attribute variable from session object
    // Prepare form action urls strings
    // Render model form flat properties and detail level 1 properties

    // @TODO cheating process detection
    ICommandObject sobj = loadTargetObject(model).getCmdobj();

    sobj.setActionUrls(model.getActionUrls());
    return model.getViewTemplate();
  }

  private String showRootDetailPage(SFModel model) {
    System.out.println("private String detailRootPageShow(SFModel model)");

    ICommandObject sobj = loadTargetObject(model).getCmdobj();

    ObjectIdentifier oi = model.getRoot();
    Object rootKey = CommandObjectPropertyUtils.getSingletonInstance()
      .decodeRootKey(
        model.getCmdobj(),
        oi.getId(),
        this.conversionService
      );
    T targetObj = this.getRepository().findById((ID) rootKey).orElse(null);
    sobj.setRoot(targetObj);
    sobj.setActionUrls(model.getActionUrls());
    // Load object
    return model.getViewTemplate();
  }

  private String doRootDeletePage(SFModel model) {
    System.out.println("private String doRootDeletePage(SFModel model)");
    RedirectAttributes reAttrs = model.getRedirectAttrs();
    reAttrs.addAttribute("op", "l");

    ObjectIdentifier oi = model.getRoot();
    Object rootKey = CommandObjectPropertyUtils.getSingletonInstance()
      .decodeRootKey(
        model.getCmdobj(),
        oi.getId(),
        this.conversionService
      );
    try {
      T targetObj = this.getRepository().findById((ID) rootKey).orElse(null);
      this.getRepository().delete(targetObj);
    }
    catch(Exception e) {
      //e.printStackTrace();
      System.out.println("Could not delete object: "
        + (oi == null ? "null" : oi.getId())
      );
    }
    return "redirect:_";
  }

  private String doCreateRootPage(SFModel model) {
    System.out.println("private String createRootPageDo(SFModel model)");
    System.out.println("[Create Form] - create click");
    HttpServletRequest req = model.getRequest();
    RedirectAttributes reAttrs = model.getRedirectAttrs();

    // Retrieve session object
    // Update flat properties for session object with data provide from Cmd Object
    // Do persist and test result
    // Clear data in the session object (prevent security risk)
    // Redirect to parent page
    ICommandObject sobj = getTargetObject(req).getCmdobj();

    if (CommandObjectPropertyUtils.getSingletonInstance()
      .copyRootFlatPropertyToSession(sobj, model)
    ) {
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
    model.getRedirectAttrs().addAttribute("op", "l");
    return "redirect:" + model.getHomeUrl();
  }

  private String doUpdateRootPage(SFModel model) {
    System.out.println("private String doUpdateRootPage(SFModel model)");
    RedirectAttributes reAttrs = model.getRedirectAttrs();
    HttpServletRequest req = model.getRequest();
    ICommandObject sobj = getTargetObject(req).getCmdobj();

    // Root object key retreive
    ObjectIdentifier oi = model.getRoot();
    Object rootKey = CommandObjectPropertyUtils.getSingletonInstance()
      .decodeRootKey(
        model.getCmdobj(),
        oi.getId(),
        this.conversionService
      );
    // @TODO verify key is changed by cheat!

    if (CommandObjectPropertyUtils.getSingletonInstance()
      .copyRootFlatPropertyToSession(sobj, model)
    ) {
      try {
        // Reload object (since transaction is expired)
        this.getRepository().findById((ID) rootKey).orElse(null);
        // Persist after refresh transaction
        this.getRepository().save((T) sobj.getRoot());
        System.out.println("-> Persist update user is ok");
        if (req.getSession(false) != null) {
          req.getSession(false).invalidate();
        }
        System.out.println("-> Clear session is ok");
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

    reAttrs.addAttribute("op", "l");
    model.getRedirectAttrs().addAttribute("op", "l");
    return "redirect:_";
  }

  private String doChildCreatePage(SFModel model) {
    System.out.println("private String doChildCreatePage(SFModel model)");
    ICommandObject sobj = getTargetObject(model.getRequest()).getCmdobj();

    if (!CommandObjectPropertyUtils.getSingletonInstance()
      .copyChildObjectToSession(sobj, model)
    ) {
      return "redirect:" + model.getParentUrl();
    }
    model.getRedirectAttrs().addAttribute("op", "c"); // @TODO how to retrieve this??
    return "redirect:" + model.getParentUrl();
  }

  private String showChildViewPage(SFModel model) {
    System.out.println("private String showChildViewPage(SFModel model)");
    ICommandObject sobj = loadTargetObject(model).getCmdobj();
    sobj.setActionUrls(model.getActionUrls()); // @TODO

    if (!CommandObjectPropertyUtils.getSingletonInstance()
      .loadChildObject(getTargetChildObject(model), model, sobj)
    ) {
      model.getRedirectAttrs().addAttribute("op", "r"); // @TODO how to retrieve this??
      return "redirect:" + model.getParentUrl();
    }
    return model.getViewTemplate();
  }

  private String doChildDeletePage(SFModel model) {
    System.out.println("private String doChildDeletePage(SFModel model)");
    ICommandObject sobj = getTargetObject(model.getRequest()).getCmdobj();

    if (!CommandObjectPropertyUtils.getSingletonInstance()
      .removeChildObjectFromSession(sobj, model, getTargetChildObject(model))
    ) {
      return "redirect:" + model.getParentUrl();
    }

    model.getRedirectAttrs().addAttribute("op", "c"); // @TODO how to retrieve this??
    return "redirect:" + model.getParentUrl();
  }

  private Object getTargetChildObject(SFModel model) {
    Integer key = Integer.parseInt(model.getTargetObjectIdentifier().getId());
    Utilities utils = this.getTargetObject(model.getRequest()).getUtilities();
    return utils.getKeyPool().lookup(model.getTargetObjectClass(), key);
  }

  private SessionObject loadTargetObject(SFModel sfModel) {
    SessionObject tObj = getTargetObject(sfModel.getRequest());
    ModelMap model = sfModel.getModel();
    model.addAttribute(this.getCommandObjectAttributeName(), tObj.getCmdobj());
    model.addAttribute(this.getUtilitiesAttributeName(), tObj.getUtilities());

    char op = sfModel.getOperationCode();
    // @TODO Please draw a state diagram to clarify below code
    if (op == 'u') {
      tObj.getCmdobj().setActionCode(op);
    }
    return tObj;
  }

  private SessionObject getTargetObject(HttpServletRequest req) {
    SessionObject result = (SessionObject)req.getSession()
      .getAttribute(this.getSessionAttributeName());
    if (result == null) {
      try {
        System.out.println("Create new session object");
        ICommandObject cmdobj = (ICommandObject) BeanUtils
          .instantiateClass(this.cmdObjClass);
        // Default op is 'c' if other is expected??? REVIEWED ME
        // System.out.println("Cheating, this session is created as update before");
        cmdobj.setOp("c");
        cmdobj.setActionCode('c');
        result = new SessionObject(cmdobj, new Utilities());
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

  // Attribute names organization
  private String getSessionAttributeName() {
    return this.name;
  }

  private String getCommandObjectAttributeName() {
    return this.name + "cmdobj";
  }

  private String getDataSourceAttributeName() {
    return this.name + "datasource";
  }

  private String getUtilitiesAttributeName() {
    return this.name + "utils";
  }
}


