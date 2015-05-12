package com.ontotext.efd.controllers;

import com.ontotext.efd.services.EFDRepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class HelloController {

	@Autowired
	EFDRepositoryConnection efdRepositoryConnection;

	@RequestMapping(method = RequestMethod.GET)
	public String printWelcome(ModelMap model) {
//		efdRepositoryConnection.repositoryCheck();


		return "index";
	}

	@RequestMapping(value = "/jdummy", method = RequestMethod.GET)
	public @ResponseBody String getJsonDummy() {
		String dummy = "{\n" +
				"    \"treeLevel\": 0,\n" +
				"    \"prefLabel\": \"\\\"Food and drink\\\"@en\",\n" +
				"    \"children\": {\n" +
				"        \"1\": \"http://dbpedia.org/resource/Category:Cuisine\"\n" +
				"    },\n" +
				"    \"uri\": \"http://dbpedia.org/resource/Category:Food_and_drink\",\n" +
				"    \"descCatCount\": 3,\n" +
				"    \"descArtCount\": 4\n" +
				"}";
		return dummy;
	}
}