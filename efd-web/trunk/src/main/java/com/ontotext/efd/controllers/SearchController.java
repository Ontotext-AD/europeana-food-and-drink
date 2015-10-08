package com.ontotext.efd.controllers;

import com.ontotext.efd.model.FTSSearchResults;
import com.ontotext.efd.model.SearchModel;
import com.ontotext.efd.services.ResourceQueryService;
import com.ontotext.efd.services.SearchQueryService;
import org.openrdf.query.TupleQueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by boyan on 15-9-18.
 */
@Controller
@RequestMapping("/rest")
public class SearchController {

    @Value("${search.query}")
    private String searchQuery;

    @Autowired
    SearchQueryService searchQueryService;

    @Autowired
    ResourceQueryService resourceQueryService;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public SearchModel search(HttpServletRequest request,
                              @RequestParam("query") String query,
                              @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
                              @RequestParam(value = "limit", required = false) Integer limit) {

        return searchQueryService.ftsSearch(query, offset, limit, request);
    }

    @RequestMapping(value = "/search/count", method = RequestMethod.GET)
    @ResponseBody
    public Integer searchCount(HttpServletRequest request, @RequestParam("query") String query) {

        return searchQueryService.ftsSearch(query, null, null, request).getSearchResults().size();
    }

    @RequestMapping(value = "/autocomplete", method = RequestMethod.GET)
    @ResponseBody
    public List<FTSSearchResults> autocomplete(HttpServletRequest request, @RequestParam("query") String query){

        return  searchQueryService.autocomplete(query);
    }

    public void facetSearch(HttpServletRequest request,
                            @RequestParam("query") String query,
                            @RequestParam(value = "rows", required = false, defaultValue = "10") String rows,
                            @RequestParam(value = "type", required = false) String [] type,
                            @RequestParam(value = "country", required = false) String [] country,
                            @RequestParam(value = "provider", required = false) String [] provider,
                            @RequestParam(value = "language", required = false) String [] language){

    }

    @RequestMapping(value = "/resource", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List<String>> getResource(@RequestParam("uri") String resource) {
        System.out.println("Fuck you");

        return resourceQueryService.getResource(resource);
    }




}
