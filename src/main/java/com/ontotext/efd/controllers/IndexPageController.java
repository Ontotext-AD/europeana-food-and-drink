package com.ontotext.efd.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by boyan on 15-9-23.
 */
@Controller
public class IndexPageController {

    @RequestMapping(value = {"/", "/search", "/resource/**", "/contact-us", "/about-us"})
    public String getIndex() {
        return "index.html";
    }
}
