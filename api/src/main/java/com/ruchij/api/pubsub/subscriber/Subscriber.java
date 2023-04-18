package com.ruchij.api.pubsub.subscriber;

import io.reactivex.rxjava3.core.Flowable;

public interface Subscriber {
	<T> Flowable<T> subscribe(String topicName, Class<T> clazz, String subscriberName);
}
