package org.poondakfai.prototype.scaffold;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.poondakfai.prototype.scaffold.controller.fragment.DemoCommandObject;
import org.poondakfai.prototype.scaffold.repository.UserRepository;
import org.poondakfai.prototype.scaffold.model.User;
import org.poondakfai.prototype.scaffold.webgui.form.Form;


@Configuration
public class DemoFormConfiguration {
  @Autowired
  private ConversionService conversionService;

  @Autowired
  private UserRepository userRepository;


  @Primary
  @Bean
  public Form<User, String> demoForm() {
    return new Form<User, String>(
      DemoCommandObject.class,
      userRepository,
      "formname",
      conversionService
    );
  }
}

