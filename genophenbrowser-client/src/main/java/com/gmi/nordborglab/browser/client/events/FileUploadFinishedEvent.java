package com.gmi.nordborglab.browser.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import elemental.html.File;

/**
 * Created by uemit.seren on 5/30/14.
 */
public class FileUploadFinishedEvent extends GwtEvent<FileUploadFinishedEvent.FileUploadFinishedHandler> {

    public interface FileUploadFinishedHandler extends EventHandler {
        void onFileUploadFinished(FileUploadFinishedEvent event);
    }

    public static final GwtEvent.Type<FileUploadFinishedHandler> TYPE = new GwtEvent.Type<FileUploadFinishedHandler>();

    private final File file;
    private final String responseText;

    public FileUploadFinishedEvent(String responseText, File file) {
        this.responseText = responseText;
        this.file = file;
    }

    public static GwtEvent.Type<FileUploadFinishedHandler> getType() {
        return TYPE;
    }

    @Override
    public GwtEvent.Type<FileUploadFinishedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FileUploadFinishedHandler handler) {
        handler.onFileUploadFinished(this);
    }

    public File getFile() {
        return file;
    }

    public String getResponseText() {
        return responseText;
    }
}
