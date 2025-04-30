package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	
	// method for adding comman data
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("Username=" + userName);

		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER=" + user);
		// get the user using username(Email)
		model.addAttribute("user", user);
	}

	// dashboard Home
	@RequestMapping("/index")
	public String dashboard(Model model) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	@GetMapping("/add-contact")
	public String OpenAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";

	}

	// processing add contact home
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, 
			Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			if(file.isEmpty()) {
				contact.setImage("contact.png");
			} else {
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			contact.setUserId(user.getId());
			Contact savedContact = this.contactRepository.save(contact);
			user.getContacts().add(savedContact);
			this.userRepository.save(user);
			
			session.setAttribute("message", new Message("Your contact is added !! add more..", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong !! Try again.. ", "danger"));
		}
		return "normal/add_contact_form";
	}
	
	//show contact handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		try {
			model.addAttribute("title", "Show User Contacts");
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			
			Pageable pageable = PageRequest.of(page, 5);
			Page<Contact> contacts = this.contactRepository.findByUserId(user.getId(), pageable);
			
			model.addAttribute("contacts", contacts);
			model.addAttribute("currentPage", page);
			model.addAttribute("totalPages", contacts.getTotalPages());
			
			return "normal/show_contacts";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/user/index";
		}
	}
	
	//showing particular contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") String cId, Model model, Principal principal) {
		try {
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			
			Optional<Contact> contactOptional = this.contactRepository.findById(cId);
			if (!contactOptional.isPresent()) {
				return "redirect:/user/show-contacts/0";
			}
			
			Contact contact = contactOptional.get();
			if (!user.getId().equals(contact.getUserId())) {
				return "redirect:/user/show-contacts/0";
			}
			
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
			return "normal/contact_detail";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/user/show-contacts/0";
		}
	}
	
	//delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") String cId, Model model, HttpSession session, Principal principal) {
		try {
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			
			Optional<Contact> contactOptional = this.contactRepository.findById(cId);
			if (!contactOptional.isPresent()) {
				session.setAttribute("message", new Message("Contact not found!", "danger"));
				return "redirect:/user/show-contacts/0";
			}
			
			Contact contact = contactOptional.get();
			if (!user.getId().equals(contact.getUserId())) {
				session.setAttribute("message", new Message("You don't have permission to delete this contact!", "danger"));
				return "redirect:/user/show-contacts/0";
			}
			
			// Delete contact image if exists
			if (!contact.getImage().equals("contact.png")) {
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, contact.getImage());
				file1.delete();
			}
			
			// Remove from user's contacts list
			user.getContacts().remove(contact);
			this.userRepository.save(user);
			
			// Delete contact
			this.contactRepository.delete(contact);
			
			session.setAttribute("message", new Message("Contact deleted successfully!", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Error deleting contact: " + e.getMessage(), "danger"));
		}
		return "redirect:/user/show-contacts/0";
	}
	
	//Open update form handler
	@GetMapping("/update-contact/{cid}")
	public String updateContactForm(@PathVariable("cid") String cid, Model model, Principal principal) {
		try {
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			
			Optional<Contact> contactOptional = this.contactRepository.findById(cid);
			if (!contactOptional.isPresent()) {
				return "redirect:/user/show-contacts/0";
			}
			
			Contact contact = contactOptional.get();
			if (!user.getId().equals(contact.getUserId())) {
				return "redirect:/user/show-contacts/0";
			}
			
			model.addAttribute("title", "Update Contact");
			model.addAttribute("contact", contact);
			return "normal/update_form";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/user/show-contacts/0";
		}
	}
	
	//update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model model, HttpSession session, Principal principal) {
		try {
			// Get current user
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			
			// Find existing contact
			Optional<Contact> contactOptional = this.contactRepository.findById(contact.getId());
			if (!contactOptional.isPresent()) {
				session.setAttribute("message", new Message("Contact not found!", "danger"));
				return "redirect:/user/show-contacts/0";
			}
			
			Contact oldContact = contactOptional.get();
			
			// Verify user has permission to update this contact
			if (!user.getId().equals(oldContact.getUserId())) {
				session.setAttribute("message", new Message("You don't have permission to update this contact!", "danger"));
				return "redirect:/user/show-contacts/0";
			}
			
			// Handle image update
			if (!file.isEmpty()) {
				// Delete old image if it's not the default
				if (!oldContact.getImage().equals("contact.png")) {
					File deleteFile = new ClassPathResource("static/img").getFile();
					File file1 = new File(deleteFile, oldContact.getImage());
					file1.delete();
				}
				
				// Save new image
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			} else {
				contact.setImage(oldContact.getImage());
			}
			
			// Preserve the user ID and update the contact
			contact.setUserId(oldContact.getUserId());
			
			// Update user's contacts list
			user.getContacts().remove(oldContact);
			Contact updatedContact = this.contactRepository.save(contact);
			user.getContacts().add(updatedContact);
			this.userRepository.save(user);
			
			session.setAttribute("message", new Message("Contact updated successfully!", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Error updating contact: " + e.getMessage(), "danger"));
		}
		return "redirect:/user/" + contact.getId() + "/contact";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model, Principal principal) {
		try {
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			model.addAttribute("title", "Profile Page");
			model.addAttribute("user", user);
			return "normal/profile";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/user/index";
		}
	}

	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute User user, @RequestParam("profileImage") MultipartFile file,
			HttpSession session, Principal principal) {
		try {
			User oldUser = this.userRepository.getUserByUserName(principal.getName());
			
			if (!file.isEmpty()) {
				// Delete old image
				if (!oldUser.getImageUrl().equals("default.png")) {
					File deleteFile = new ClassPathResource("static/img").getFile();
					File file1 = new File(deleteFile, oldUser.getImageUrl());
					file1.delete();
				}
				
				// Save new image
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				user.setImageUrl(file.getOriginalFilename());
			} else {
				user.setImageUrl(oldUser.getImageUrl());
			}
			
			user.setId(oldUser.getId());
			user.setPassword(oldUser.getPassword());
			user.setRole(oldUser.getRole());
			user.setEnable(oldUser.isEnable());
			
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Profile updated successfully!", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Error updating profile: " + e.getMessage(), "danger"));
		}
		return "redirect:/user/profile";
	}

	@PostMapping("/share-contact/{contactId}")
	public String shareContact(@PathVariable("contactId") String contactId,
			@RequestParam("shareEmail") String shareEmail,
			@RequestParam(value = "shareMessage", required = false) String shareMessage,
			Principal principal, HttpSession session) {
		try {
			// Get the contact to share
			Optional<Contact> contactOptional = this.contactRepository.findById(contactId);
			if (!contactOptional.isPresent()) {
				session.setAttribute("message", new Message("Contact not found!", "danger"));
				return "redirect:/user/show-contacts/0";
			}
			
			Contact contact = contactOptional.get();
			
			// Get the user to share with
			User shareWithUser = this.userRepository.getUserByUserName(shareEmail);
			if (shareWithUser == null) {
				session.setAttribute("message", new Message("User not found with email: " + shareEmail, "danger"));
				return "redirect:/user/" + contactId + "/contact";
			}
			
			// Create a new contact for the shared user
			Contact sharedContact = new Contact();
			sharedContact.setName(contact.getName());
			sharedContact.setSecondName(contact.getSecondName());
			sharedContact.setWork(contact.getWork());
			sharedContact.setEmail(contact.getEmail());
			sharedContact.setPhone(contact.getPhone());
			sharedContact.setImage(contact.getImage());
			sharedContact.setDescription(contact.getDescription());
			sharedContact.setUserId(shareWithUser.getId());
			
			// Save the shared contact
			this.contactRepository.save(sharedContact);
			
			// Add to user's contacts list
			shareWithUser.getContacts().add(sharedContact);
			this.userRepository.save(shareWithUser);
			
			session.setAttribute("message", new Message("Contact shared successfully with " + shareEmail, "success"));
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Error sharing contact: " + e.getMessage(), "danger"));
		}
		return "redirect:/user/" + contactId + "/contact";
	}
}
