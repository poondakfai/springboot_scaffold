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
          break;

        case 'p':
          result = doCreateRootPage(o);
          break;

        default:
          break;
      }
    }
    else {
      switch (o.getOperationCode()) {
        case 'c':
          result = showChildCreatePage(o);
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
    Object rootKey = CommandObjectPropertyUtils.decodeRootKey(
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
    model.getRedirectAttrs().addAttribute("op", "l");
    return "redirect:" + model.getHomeUrl();
  }

  private String showChildCreatePage(SFModel model) {
    System.out.println("private String createChildPageShow(SFModel model)");
    ICommandObject sobj = getTargetObject(model.getRequest()).getCmdobj();

    if (!CommandObjectPropertyUtils.copyChildObjectToSession(sobj, model)) {
      return "redirect:" + model.getParentUrl();
    }
    model.getRedirectAttrs().addAttribute("op", "c"); // @TODO how to retrieve this??
    return "redirect:" + model.getParentUrl();
  }

  private SessionCommandObject loadTargetObject(SFModel sfModel) {
    SessionCommandObject tObj = getTargetObject(sfModel.getRequest());
    ModelMap model = sfModel.getModel();
    model.addAttribute(this.getCommandObjectAttributeName(), tObj.getCmdobj());
    model.addAttribute(this.getUtilitiesAttributeName(), tObj.getUtilities());
    return tObj;
  }

  private SessionCommandObject getTargetObject(HttpServletRequest req) {
    SessionCommandObject result = (SessionCommandObject)req.getSession()
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
        result = new SessionCommandObject(cmdobj, new Utilities());
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


