package au.edu.alveo.client;

import au.edu.alveo.client.entity.AudioAnnotation;
import au.edu.alveo.client.entity.AudioDocument;
import au.edu.alveo.client.entity.Document;

/**
 * Created by amack on 20/02/14.
 */
public class AudioRestAnnotation extends BasicRestAnnotation implements AudioAnnotation{

	private Document annotationTarget = null;

	public void setAnnotationTarget(Document doc) {
		annotationTarget = doc;
	}

	@Override
	public Document getAnnotationTarget() {
		return annotationTarget;
	}

	@Override
	public AudioDocument getAudioAnnotationTarget() {
		return (AudioDocument) getAnnotationTarget();
	}
}
