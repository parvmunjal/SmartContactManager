package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model m,Principal principal) {
		String userName = principal.getName();
		
		System.out.println("USERNAME:"+userName);
		
		User user=userRepository.getUserByUsername(userName);
		System.out.println("USER:"+user);
		
		m.addAttribute("user",user);
		
	}
	
	//user dashboard handler
	@GetMapping("/dashboard")
	public String dashboard(Model m) {
		m.addAttribute("title","Home");
		return "normal/user_dashboard";
	}
	
	//add contact handler
	@GetMapping("/add_contact")
	public String addContact(Model m) {
		m.addAttribute("title","Add Contact");
		m.addAttribute("contact",new Contact());
		return "normal/add_contact";
	}
	
	//process contact handler
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file ,
			Principal principal,HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUsername(name);
			contact.setUser(user);
			user.getContacts().add(contact);
			
			if(!file.isEmpty()) {
				contact.setImageUrl(file.getOriginalFilename());
				
				File file2 = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image uploaded");
			}
			else {
				System.out.println("No image included");
				contact.setImageUrl("contact.png");
			}
			userRepository.save(user);
			
			//System.out.println("CONTACT:"+contact);
			System.out.println("Added to database");
			
			session.setAttribute("message", new Message("The contact has been saved!", "success"));
			
		}
		catch(Exception e) {
			System.out.println("ERROR:"+e.getMessage());
			session.setAttribute("message", new Message("Unable to save the contact!", "danger"));
			
		}
		
		return "normal/add_contact";
	}
//	//show contacts handler
//	@GetMapping("/showcontacts/{page}")
//	public String showContacts(@PathVariable("page")Integer page ,Model m,Principal principal) {
//		m.addAttribute("title","View Contacts");  
//		
//		String name = principal.getName();
//		User user = this.userRepository.getUserByUsername(name);
//		//Current Page- page
//		//No of contacts per page- 11
//		Pageable pageable = PageRequest.of(page, 6);
//		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
//		m.addAttribute("contacts",contacts);
//		
//		//pagination
//		m.addAttribute("currentPage",page);
//		m.addAttribute("totalPages",contacts.getTotalPages());
//		return "normal/showcontacts";
//	}
	
	@GetMapping("/showcontacts")
	public String show(Model m,Principal principal) {
		m.addAttribute("title","View Contacts");  
		
		String name = principal.getName();
		User user = this.userRepository.getUserByUsername(name);
		
		List<Contact> contacts = this.contactRepository.findContactsByUser(user.getId());
		m.addAttribute("contacts",contacts);
	
		return "normal/show";
	}
	
	//showing each contact details
	@GetMapping("/a{cId}")
	public String contactDetails(@PathVariable("cId") Integer cId,Model m,Principal principal) {
		System.out.println("CID:"+cId);
		Contact contact = this.contactRepository.getReferenceById(cId);
		
		String userName = principal.getName();
		User user = userRepository.getUserByUsername(userName);
		
		if(user.getId()==contact.getUser().getId()) {
			m.addAttribute("contact",contact);
			m.addAttribute("title",contact.getName());
			return "normal/contact_details";
		}
		return "/user_dashboard";
	}
	
	//deleting contact
	@GetMapping("/c{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,Model m,HttpSession session) {
		System.out.println("CID:"+cId);
		Contact contact = this.contactRepository.getReferenceById(cId);
		contact.setUser(null);
		
		this.contactRepository.deleteById(contact.getcId());
		System.out.println("Deleted");
		session.setAttribute("message", new Message("Contact Deleted Succesfully", "success"));
		
		return "redirect:/user/showcontacts";
	}
	
	//updating contact
	@PostMapping("/b{cId}")
	public String updateContact(@PathVariable("cId") Integer cId,Model m) {
		Contact contact = this.contactRepository.getReferenceById(cId);
		m.addAttribute("title","Update contact-"+ contact.getName());
		m.addAttribute("contact",contact);
		
		return "normal/update_contact";
	}
	//process-update contact
	@PostMapping("/process-update")
	public String processUpdateContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,
			Model m,HttpSession session,Principal principal) {
		
		try {
			Contact oldContact=this.contactRepository.getReferenceById(contact.getcId());
			
			if(!file.isEmpty()) {
				//delete old img
				
				
				//update new img
				File file2 = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImageUrl(file.getOriginalFilename());
			}
			
			else {
				contact.setImageUrl(oldContact.getImageUrl());
				
			}
		System.out.println("NAME:"+contact.getName());
		System.out.println("ID:"+contact.getcId());
		User user = this.userRepository.getUserByUsername(principal.getName());
		contact.setUser(user);
		this.contactRepository.save(contact); 
		session.setAttribute("message", new Message("Contact has been updated", "success"));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/showcontacts";
	}
	//show profile handler
	@GetMapping("/profile")
	public String profile(Principal principal,Model m) {
		String name = principal.getName();
		User user = this.userRepository.getUserByUsername(name);
		m.addAttribute("user",user);
		m.addAttribute("title",user.getName());
		return "normal/profile";
	}
	
	
}
