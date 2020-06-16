package com.gmi.nordborglab.browser.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import elemental.html.File;

/**
 * Created by uemit.seren on 5/30/14.
 */
public class FileUploadErrorEvent extends GwtEvent<FileUploadErrorEvent.FileUploadErrorHandler> {

    public interface FileUploadErrorHandler extends EventHandler {
        void onFileUploadError(FileUploadErrorEvent event);
    }

    public static final GwtEvent.Type<FileUploadErrorHandler> TYPE = new GwtEvent.Type<FileUploadErrorHandler>();

    private final File file;
    private final String responseText;

    public FileUploadErrorEvent(String responseText, File file) {
        this.responseText = responseText;
        this.file = file;
    }

    public static GwtEvent.Type<FileUploadErrorHandler> getType() {
        return TYPE;
    }

    @Override
    public GwtEvent.Type<FileUploadErrorHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FileUploadErrorHandler handler) {
        handler.onFileUploadError(this);
    }

    public File getFile() {
        return file;
    }

    public String getResponseText() {
        return responseText;
    }
}
