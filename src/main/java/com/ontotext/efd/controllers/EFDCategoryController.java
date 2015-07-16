package com.ontotext.efd.controllers;

import com.ontotext.efd.model.EFDCategory;
import com.ontotext.efd.rdf.EFDTaxonomy;
import com.ontotext.efd.services.EFDRepositoryConnection;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Controller
@RequestMapping("/")
public class EFDCategoryController {

	@Autowired
	EFDRepositoryConnection efdRepositoryConnection;


	@RequestMapping(method = RequestMethod.GET, params = "category")
	public String printWelcome(ModelMap model, @RequestParam("category") String category) {

		model.addAttribute("category", category);
		return "index";
	}

	@RequestMapping(value = "/jdummy", method = RequestMethod.GET, params = "category")
	@ResponseBody public EFDCategory getJsonDummy(@RequestParam("category") String category, Model model) {
		long start = 0;
		long end = 0;
		double result = 0;


		start = new Date().getTime();
		EFDCategory efdCategory = new EFDCategory(new URIImpl(category.split(",")[0]));
		end = new Date().getTime();

		result = (end - start);

		System.out.println("Tree time : " + result);

		return efdCategory;
	}

	@RequestMapping(value = "/remove", method = RequestMethod.GET, params = "category")
	@ResponseBody public ResponseEntity<String> removeNode(@RequestParam("category") String category, HttpServletResponse response) {
		String cat[] = category.split(",");
		String recreateCat = "";
		if (cat.length > 1) {
			String uri[] =  cat[0].split("/");
			uri[uri.length - 1] = "Category:" + uri[uri.length - 1].replaceAll(" ", "_");
			for(String part : uri) {
				if (part.equals("http:")) recreateCat +=  part;
				else recreateCat += "/" +  part;
			}
		}
		else {
			recreateCat = cat[0].replaceAll(" ", "_");
		}
		EFDCategory efdCategory = new EFDCategory(new URIImpl(recreateCat));

		try {
			efdCategory.markAsIrrelevant(new URIImpl(EFDTaxonomy.EFD_ANNOTATOR_MANUAL));
		} catch (RepositoryException e) {
			e.printStackTrace();
			return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity(HttpStatus.OK);
	}
}