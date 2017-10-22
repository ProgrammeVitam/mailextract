package fr.gouv.vitam.tools.mailextract.lib.store.javamail;

public interface JMEmbeddedStore {

	abstract public void setObjectContent(Object objectContent);
	
	abstract public Object getObjectContent();
}
