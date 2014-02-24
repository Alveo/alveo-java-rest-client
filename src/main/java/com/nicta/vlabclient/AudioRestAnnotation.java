package com.nicta.vlabclient;

import com.nicta.vlabclient.entity.AudioAnnotation;
import com.nicta.vlabclient.entity.AudioDocument;
import com.nicta.vlabclient.entity.Document;
import com.nicta.vlabclient.entity.TextAnnotation;
import com.nicta.vlabclient.entity.TextDocument;

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
