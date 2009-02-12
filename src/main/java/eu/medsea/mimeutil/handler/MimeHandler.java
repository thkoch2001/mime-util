package eu.medsea.mimeutil.handler;

import java.util.Collection;

public interface MimeHandler {
	public Collection getMimeTypes();
	public Collection handle(Collection mimeTypes);
}
