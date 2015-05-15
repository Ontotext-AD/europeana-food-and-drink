package com.ontotext.efd.controllers;

import com.ontotext.efd.model.EFDCategory;
import com.ontotext.efd.services.EFDRepositoryConnection;
import org.openrdf.model.impl.URIImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@RequestMapping("/")
public class EFDCategoryController {

	@Autowired
	EFDRepositoryConnection efdRepositoryConnection;


	@RequestMapping(method = RequestMethod.GET)
	public String printWelcome(ModelMap model) {

		return "index";
	}

	@RequestMapping(value = "/jdummy", method = RequestMethod.GET, params = "category")
	@ResponseBody public EFDCategory getJsonDummy(@RequestParam("category") String category, Model model) {
		long start = 0;
		long end = 0;
		double result = 0;

		start = new Date().getTime();
		EFDCategory efdCategory = new EFDCategory(new URIImpl(category));
		end = new Date().getTime();

		result = (end - start);

		System.out.println("Tree time : " + result);

		return efdCategory;
	}

	@RequestMapping(value = "/remove", method = RequestMethod.GET, params = "category")
	@ResponseBody public String removeNode(@RequestParam("category") String category) {
		return null;
	}
}