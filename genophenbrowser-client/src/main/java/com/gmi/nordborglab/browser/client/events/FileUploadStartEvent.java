package com.gmi.nordborglab.browser.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Created by uemit.seren on 5/30/14.
 */
public class FileUploadStartEvent extends GwtEvent<FileUploadStartEvent.FileUploadStartHandler> {

    public interface FileUploadStartHandler extends EventHandler {
        void onFileUploadStart(FileUploadStartEvent event);
    }

    public static final GwtEvent.Type<FileUploadStartHandler> TYPE = new GwtEvent.Type<FileUploadStartHandler>();

    public FileUploadStartEvent() {

    }

    public static GwtEvent.Type<FileUploadStartHandler> getType() {
        return TYPE;
    }

    @Override
    public GwtEvent.Type<FileUploadStartHandler> getAssociatedType() {
        return TYPE;
    }

    public static void fire(final HasHandlers source) {
        source.fireEvent(new FileUploadStartEvent());
    }

    @Override
    protected void dispatch(FileUploadStartHandler handler) {
        handler.onFileUploadStart(this);
    }
}
