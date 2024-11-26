package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	//home handler
	@GetMapping("/")
	public String home(Model m) {
		m.addAttribute("title","Home-Smart Contact Manager");
		return "home";
	}
	//about handler
	@GetMapping("/about")
	public String about(Model m) {
		m.addAttribute("title","About-Smart Contact Manager");
		return "about";
	}
	//signup handler
	@GetMapping("/signup")
	public String signUp(Model m) {
		m.addAttribute("title","Signup-Smart Contact Manager");
		m.addAttribute("user",new User());
		return "signup";
	}
	
	//handler for user signup 
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute ("user") User user,BindingResult bindingResult, @RequestParam(value="agreement",defaultValue="false")
	boolean agreement,Model m,HttpSession session) {
		try {
			if(!agreement) {
				System.out.println("Agree TnC");
				throw new Exception("Agree to Terms and Conditions");
			}
			if(bindingResult.hasErrors()) {
				System.out.println("Error:"+ bindingResult.toString());
				m.addAttribute("user",user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			User result = this.userRepository.save(user);
			
			
			
			System.out.println("Agreement:"+agreement);
			System.out.println("User:"+user);
			m.addAttribute("user",new User());
			session.setAttribute("message", new Message("Successfully registered!", "alert-success"));
			return "home";
			
		}
		catch(Exception e) {
			e.printStackTrace();
			m.addAttribute("user",user); 
			session.setAttribute("message", new Message(e.getMessage(), "alert-danger"));
			return "signup";
		}
	}
	
	//handler for login
	@GetMapping("/signin")
	public String login(Model m) {
		m.addAttribute("title","Login-Smart Contact Manager");
		return "login";
	}
	
}
