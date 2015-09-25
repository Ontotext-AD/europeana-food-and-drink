package com.ontotext.efd.controllers;

import com.ontotext.efd.services.SearchQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by boyan on 15-9-18.
 */
@Controller
@RequestMapping("/rest/search")
public class SearchController {

    @Value("${search.query}")
    private String searchQuery;

    @Autowired
    SearchQueryService searchQueryService;

    @RequestMapping(method = RequestMethod.GET)
    public void search(HttpServletRequest request, @RequestParam("query") String query, @RequestParam(value = "rows", required = false, defaultValue = "10") String rows) {
        System.out.println(rows);
        System.out.println(searchQuery);
        searchQueryService.sayHi();


    }

    public void facetSearch(HttpServletRequest request,
                            @RequestParam("query") String query,
                            @RequestParam(value = "rows", required = false, defaultValue = "10") String rows,
                            @RequestParam(value = "type", required = false) String [] type,
                            @RequestParam(value = "country", required = false) String [] country,
                            @RequestParam(value = "provider", required = false) String [] provider,
                            @RequestParam(value = "language", required = false) String [] language){

    }

}
