package main.java.model;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {

    private String view;
    private Map<String, Object> data = new HashMap<>();

    public String getView() { 
        return view; 
    }
    public void setView(String view) { 
        this.view = view; 
    }

    public Map<String, Object> getData() { 
        return data;
     }

    public void addObject(String name, Object obj) {
        data.put(name, obj);
    }
}
