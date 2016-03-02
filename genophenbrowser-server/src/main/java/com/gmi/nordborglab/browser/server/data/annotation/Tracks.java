package com.gmi.nordborglab.browser.server.data.annotation;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 31.05.13
 * Time: 10:22
 * To change this template use File | Settings | File Templates.
 */
public class Tracks {

    private final List<GenomeStat> tracks;

    public Tracks(List<GenomeStat> tracks) {
        this.tracks = tracks;

    }

    public List<GenomeStat> getTracks() {
        return tracks;
    }

}
