package com.gmi.nordborglab.browser.server.tasks;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 4/12/13
 * Time: 1:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class CeleryTask {

    private String id;
    private String task;
    private List<Object> args;

    public CeleryTask(String id, String task, List<Object> args) {
        this.id = id;
        this.task = task;
        this.args = args;
    }

    public String getId() {
        return id;
    }

    public String getTask() {
        return task;
    }

    public List<Object> getArgs() {
        return args;
    }
}
