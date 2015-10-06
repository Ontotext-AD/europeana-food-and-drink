//package com.ontotext.efd.controllers;
//
//import com.ontotext.efd.services.ResourceQueryService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * Created by boyan on 15-10-6.
// */
//@Controller
//@RequestMapping(value = "/rest/resource")
//public class ResourceController {
//
//    @Autowired
//    ResourceQueryService resourceQueryService;
//
//    @ResponseBody
//    public Map<String, List<String>> getResource(@RequestParam("uri") String resource) {
//        System.out.println("Fuck you");
//
//        return resourceQueryService.getResource(resource);
//    }
//}
