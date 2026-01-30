package com.demo.web.application.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.demo.web.application.dto.RegistrationForm;
import com.demo.web.application.dto.Role;
import com.demo.web.application.dto.User;
import com.demo.web.application.repository.RoleRepository;
import com.demo.web.application.repository.UserRepository;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String registerForm() {
        return "registration";
    }

    @PostMapping
    public String processRegistration(RegistrationForm form) {
        User user = form.toUser(passwordEncoder);
        
        // Get or create the default USER role (stored without ROLE_ prefix in DB)
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER")));
        
        user.addRole(userRole);
        userRepository.save(user);
        
        return "redirect:/";
    }
}
