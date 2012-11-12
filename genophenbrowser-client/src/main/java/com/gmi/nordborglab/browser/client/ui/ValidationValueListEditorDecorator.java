package com.gmi.nordborglab.browser.client.ui;

import java.util.List;

import com.google.gwt.editor.client.EditorError;
import com.google.gwt.editor.client.HasEditorErrors;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.user.client.ui.ValueListBox;

public class ValidationValueListEditorDecorator<T> extends ValidationDecorator implements
	HasEditorErrors<T>, IsEditor<TakesValueEditor<T>> {

	private TakesValueEditor<T> editor;
	
	@Override
	public TakesValueEditor<T> asEditor() {
		return editor;
	}
	
	public void setValueListBox(ValueListBox<T> widget) {
		contents.add(widget);
		setEditor(widget.asEditor());
	}
	
	public void setEditor(TakesValueEditor<T> editor) {
		this.editor = editor;
	}

	@Override
	public void showErrors(List<EditorError> errors) {
		StringBuilder sb = new StringBuilder();
		for (EditorError error : errors) {
			if (error.getEditor().equals(editor)) {
				sb.append("\n").append(error.getMessage());
			}
		}
		setError(sb.toString());
	}
	
}
