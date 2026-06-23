package main.java.annotation.controllers;

import main.java.annotation.FWController;
import main.java.annotation.UrlMapping;

@FWController
public class TestController {

    @UrlMapping("/test/list")
    public String list() {
        return "List method called";
    }

    @UrlMapping("/test/hello")
    public String hello() {
        return "Hello method called";
    }
}
