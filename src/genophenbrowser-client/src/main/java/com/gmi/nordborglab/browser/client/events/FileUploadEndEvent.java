package com.gmi.nordborglab.browser.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Created by uemit.seren on 5/30/14.
 */
public class FileUploadEndEvent extends GwtEvent<FileUploadEndEvent.FileUploadEndHandler> {

    public interface FileUploadEndHandler extends EventHandler {
        void onFileUploadEnd(FileUploadEndEvent event);
    }

    public static final GwtEvent.Type<FileUploadEndHandler> TYPE = new GwtEvent.Type<FileUploadEndHandler>();

    public FileUploadEndEvent() {

    }

    public static GwtEvent.Type<FileUploadEndHandler> getType() {
        return TYPE;
    }

    @Override
    public GwtEvent.Type<FileUploadEndHandler> getAssociatedType() {
        return TYPE;
    }

    public static void fire(final HasHandlers source) {
        source.fireEvent(new FileUploadEndEvent());
    }

    @Override
    protected void dispatch(FileUploadEndHandler handler) {
        handler.onFileUploadEnd(this);
    }
}
