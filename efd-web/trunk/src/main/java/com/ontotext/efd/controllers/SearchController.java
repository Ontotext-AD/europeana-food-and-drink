package com.ontotext.efd.controllers;

import com.ontotext.efd.model.FTSSearchResults;
import com.ontotext.efd.model.FacetModel;
import com.ontotext.efd.model.HierarchyFacet;
import com.ontotext.efd.model.SearchModel;
import com.ontotext.efd.services.CategoryFacetSearchService;
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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

    @Value("${facets.category}")
    private String categoryFacetQuery;

    @Value("${facet.article}")
    private String articleFacetQuery;

    @Value("${facet.places}")
    private String placesFacetQuery;


    @Autowired
    SearchQueryService searchQueryService;

    @Autowired
    ResourceQueryService resourceQueryService;

    @Autowired
    CategoryFacetSearchService facetSearchService;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public SearchModel search(HttpServletRequest request,
                              @RequestParam(value = "query", required = false, defaultValue = "") String query,
                              @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
                              @RequestParam(value = "limit", required = false) Integer limit) {

        try {
            query = URLDecoder.decode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        return searchQueryService.ftsSearch(query, offset, limit, request);
        return searchQueryService.choSearch(query, offset, limit, request);
    }

    @RequestMapping(value = "/search/count", method = RequestMethod.GET)
    @ResponseBody
    public String searchCount(HttpServletRequest request, @RequestParam("query") String query) {
//        SearchModel results = searchQueryService.ftsSearch(query, null, null, request);

        String count = searchQueryService.ESCount(query, null, null, request);

//        if (results == null) return 0;
        return count;
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
        try {
            resource = URLDecoder.decode(resource, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return resourceQueryService.getResource(resource);
    }


    @RequestMapping(value = "/categoryFacet", method = RequestMethod.GET)
    @ResponseBody
    public HierarchyFacet getCategoryFacet(HttpServletRequest request,
                                           @RequestParam(value = "subCategories", required = false, defaultValue = "Food_and_drink") String subCategories,
                                           @RequestParam(value = "category", required = false) String category,
                                           @RequestParam(value = "article", required = false) String article,
                                           @RequestParam(value = "query", required = false) String query) {

        HierarchyFacet hierarchyFacet = new HierarchyFacet();
        String categoryQuery = facetSearchService.preprocessCategoryQuery(categoryFacetQuery, subCategories, category, query, article, request);
        hierarchyFacet.setCategoryFacet(facetSearchService.getCategoryArticleFacets(categoryQuery));

        String articleQuery = facetSearchService.preprocessArticleQuery(articleFacetQuery, subCategories, query);
        hierarchyFacet.setArticleModel(facetSearchService.getCategoryArticleFacets(articleQuery));

        return hierarchyFacet;
    }

    @RequestMapping(value = "/placesFacet", method = RequestMethod.GET)
    @ResponseBody
    public HierarchyFacet getPlacesArticleFacet(HttpServletRequest request,
                                                @RequestParam(value = "subCategories", required = false, defaultValue = "Earth") String subPlaces,
                                                @RequestParam(value = "query", required = false) String query) {
        HierarchyFacet hierarchyFacet = new HierarchyFacet();
        String placesQuery = facetSearchService.preprocessPlacesQuery(placesFacetQuery, subPlaces, query, request);
        hierarchyFacet.setCategoryFacet(facetSearchService.getCategoryArticleFacets(placesQuery));

        return hierarchyFacet;
    }

}
