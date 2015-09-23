package com.ontotext.efd.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by boyan on 15-9-23.
 */
@Controller
public class IndexPageController {

    @RequestMapping("/")
    public String getIndex() {
        return "index";
    }
}
