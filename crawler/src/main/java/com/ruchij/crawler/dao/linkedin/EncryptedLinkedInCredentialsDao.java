package com.ruchij.crawler.dao.linkedin;


import com.ruchij.crawler.dao.linkedin.models.EncryptedLinkedInCredentials;
import com.ruchij.crawler.utils.Kleisli;
import com.ruchij.crawler.utils.ReaderMonad;
import io.reactivex.rxjava3.core.Flowable;

import java.util.List;
import java.util.Optional;

public interface EncryptedLinkedInCredentialsDao<A> {
	Kleisli<A, String> insert(EncryptedLinkedInCredentials encryptedLinkedInCredentials);

	default ReaderMonad<A, Flowable<EncryptedLinkedInCredentials>> getAll() {
		return flowable(0, 100);
	}

	default ReaderMonad<A, Flowable<EncryptedLinkedInCredentials>> flowable(int pageNumber, int pageSize) {
		return new ReaderMonad<>(input ->
			Flowable.fromCompletionStage(getAll(pageNumber, pageSize).run(input))
				.concatMap(encryptedLinkedInCredentials -> {
					if (encryptedLinkedInCredentials.size() != pageSize) {
						return Flowable.fromIterable(encryptedLinkedInCredentials);
					} else {
						return Flowable.fromIterable(encryptedLinkedInCredentials)
							.concatWith(Flowable.defer(() -> flowable(pageNumber + 1, pageSize).run(input)));
					}
				})
		);
	}

	Kleisli<A, List<EncryptedLinkedInCredentials>> getAll(int pageNumber, int pageSize);

	Kleisli<A, Optional<EncryptedLinkedInCredentials>> findByUserId(String userId);

	Kleisli<A, Boolean> deleteByUserId(String userId);
}
