package controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class WebController {

	@RequestMapping("/")
	public String homepage() {
		return "homepage.html";
	}

	@RequestMapping("/signin")
	public String signin() {
		return "signin.html";
	}

	@RequestMapping("/signup")
	public String signup() {
		return "signup.html";
	}
}
