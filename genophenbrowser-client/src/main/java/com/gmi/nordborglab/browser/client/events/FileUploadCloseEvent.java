package com.gmi.nordborglab.browser.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Created by uemit.seren on 5/30/14.
 */
public class FileUploadCloseEvent extends GwtEvent<FileUploadCloseEvent.FileUploadCloseHandler> {

    public interface FileUploadCloseHandler extends EventHandler {
        void onFileUploadClose(FileUploadCloseEvent event);
    }

    public static final GwtEvent.Type<FileUploadCloseHandler> TYPE = new GwtEvent.Type<FileUploadCloseHandler>();

    public FileUploadCloseEvent() {

    }

    public static GwtEvent.Type<FileUploadCloseHandler> getType() {
        return TYPE;
    }

    @Override
    public GwtEvent.Type<FileUploadCloseHandler> getAssociatedType() {
        return TYPE;
    }

    public static void fire(final HasHandlers source) {
        source.fireEvent(new FileUploadStartEvent());
    }

    @Override
    protected void dispatch(FileUploadCloseHandler handler) {
        handler.onFileUploadClose(this);
    }
}
