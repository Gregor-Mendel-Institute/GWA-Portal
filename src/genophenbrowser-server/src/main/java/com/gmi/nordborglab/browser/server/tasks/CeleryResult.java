package com.gmi.nordborglab.browser.server.tasks;

import java.util.List;
import java.util.Map;

/**
 * Created by uemit.seren on 11/27/14.
 */
public class CeleryResult {

    private String status;
    private String traceback;
    private Map<String, Object> result;
    private List<Object> children;
    private String task_id;


    public CeleryResult() {
    }

    public String getStatus() {
        return status;
    }

    public String getTraceback() {
        return traceback;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public List<Object> getChildren() {
        return children;
    }

    public String getTask_id() {
        return task_id;
    }
}
