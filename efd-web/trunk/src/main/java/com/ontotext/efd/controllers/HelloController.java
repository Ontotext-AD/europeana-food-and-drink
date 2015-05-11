package com.ontotext.efd.controllers;

import com.ontotext.efd.services.EFDRepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class HelloController {

	@Autowired
	EFDRepositoryConnection efdRepositoryConnection;

	@RequestMapping(method = RequestMethod.GET)
	public String printWelcome(ModelMap model) {
		//efdRepositoryConnection.repositoryCheck();

		model.addAttribute("message", "Hello world!");
		return "hello";
	}
}