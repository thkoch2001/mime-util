package eu.medsea.mimeutil.handler;

import java.util.Collection;

public abstract class AbstractMimeHandler implements MimeHandler {

	private Collection mimeTypes;

	public AbstractMimeHandler(Collection mimeTypes) {
		this.mimeTypes = mimeTypes;
	}

	public Collection getMimeTypes() {
		return mimeTypes;
	}
}
