package com.prog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.prog.entity.User;
import com.prog.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    @Autowired
    private UserRepository repo;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User u, RedirectAttributes redirectAttributes) {
        // Check if email already exists
        User existingUser = repo.findByEmail(u.getEmail());
        if (existingUser != null) {
            redirectAttributes.addFlashAttribute("message", "Email already exists. Please try another.");
            return "redirect:/register"; // Redirect to the registration page
        }

        // Save new user if no duplicate
        repo.save(u);
        redirectAttributes.addFlashAttribute("message", "User registered successfully!");
        return "redirect:/";
    }


    @PostMapping("/dologin")
    public String dologin(String username, String password, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = repo.findByEmailAndPassword(username, password);

        if (user != null) {
            session.setAttribute("loggedUser", user);
            redirectAttributes.addFlashAttribute("message", "Login successful!");
            return "redirect:/home"; // Redirect to a GET-mapped home page.
        } else {
            redirectAttributes.addFlashAttribute("message", "Invalid email or password!");
            return "redirect:/login";
        }
    }

    @GetMapping("/update-details")
    public String showUpdateDetailsPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser != null) {
            model.addAttribute("user", loggedUser);
            return "update-details";
        }
        redirectAttributes.addFlashAttribute("message", "Please login first!");
        return "redirect:/login";
    }

	/*
	 * @PostMapping("/update-details") public String updateDetails(@ModelAttribute
	 * User user, HttpSession session, RedirectAttributes redirectAttributes) { User
	 * loggedUser = (User) session.getAttribute("loggedUser");
	 * 
	 * if (loggedUser != null) { user.setEmail(loggedUser.getEmail());
	 * repo.save(user); session.setAttribute("loggedUser", user);
	 * redirectAttributes.addFlashAttribute("message",
	 * "Details updated successfully!"); return "redirect:/home"; }
	 * 
	 * redirectAttributes.addFlashAttribute("message", "Error updating details!");
	 * return "redirect:/login"; }
	 */
    
    @PostMapping("/update-details")
    public String updateDetails(@ModelAttribute User user, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser != null) {
            // Retrieve the existing user from the database
            User existingUser = repo.findByEmail(loggedUser.getEmail());
            if (existingUser != null) {
                // Update the fields with values from the form
                existingUser.setName(user.getName());
                existingUser.setDob(user.getDob());
                existingUser.setAddress(user.getAddress());
                existingUser.setPassword(user.getPassword()); 

                // Save the updated user details
                repo.save(existingUser);

                // Update the session to reflect the changes
                session.setAttribute("loggedUser", existingUser);

                // Redirect with success message
                redirectAttributes.addFlashAttribute("message", "Details updated successfully!");
                return "redirect:/home";
            }
        }

        // Redirect with error message
        redirectAttributes.addFlashAttribute("message", "Error updating details!");
        return "redirect:/login";
    }



    @GetMapping("/view-details")
    public String viewDetails(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser != null) {
            model.addAttribute("user", loggedUser);
            return "view-details";
        }

        redirectAttributes.addFlashAttribute("message", "Please login first!");
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String homePage(HttpSession session, Model model) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        model.addAttribute("user", loggedUser);
        return "home";
    }
    
    
    @PostMapping("/delete-account")
    public String deleteAccount(HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser != null) {
            // Delete the user from the repository
            repo.deleteById(loggedUser.getId());

            // Invalidate the session
            session.invalidate();

            // Redirect to the signup or login page with a message
            redirectAttributes.addFlashAttribute("message", "Your account has been deleted successfully.");
            return "redirect:/";
        }

        redirectAttributes.addFlashAttribute("message", "Error deleting your account!");
        return "redirect:/home";
    }
}
