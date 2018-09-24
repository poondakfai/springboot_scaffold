package org.poondakfai.prototype.scaffold.controller;


import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.ModelMap;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.poondakfai.prototype.scaffold.controller.fragment.DemoCommandObject;
import org.poondakfai.prototype.scaffold.repository.UserRepository;
import org.poondakfai.prototype.scaffold.model.User;
import org.poondakfai.prototype.scaffold.webgui.form.Form;


@Controller
public class DemoController {
  private UserRepository userRepository;
  private Form<User, String> demoForm;


  // DEMO controller wire up code
  public DemoController(@Autowired UserRepository userRepository) {
    this.userRepository = userRepository;
    this.demoForm = new Form<User, String>(DemoCommandObject.class, userRepository, "formname");
  }

  @RequestMapping(
    value  = "/user/**",
    method = {RequestMethod.GET, RequestMethod.POST},
    params = {Form.OP_CODE}
  )
  public String process(
    @RequestParam(Form.OP_CODE) final String op,
    final DemoCommandObject cmdobj, // @TODO properties import/export (to share with other @RequestMapping processor)
    ModelMap model,
    RedirectAttributes reAttrs,
    final HttpServletRequest req,
    SessionStatus status
  ) {
    return this.demoForm.process(op, cmdobj, model, reAttrs, req, status);
  }
  // END demo controller wire up code
}


