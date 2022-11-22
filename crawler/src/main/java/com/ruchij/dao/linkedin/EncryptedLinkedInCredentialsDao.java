package com.ruchij.dao.linkedin;


import com.ruchij.dao.linkedin.models.EncryptedLinkedInCredentials;
import io.reactivex.rxjava3.core.Flowable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface EncryptedLinkedInCredentialsDao {
    CompletableFuture<String> insert(EncryptedLinkedInCredentials encryptedLinkedInCredentials);

    Flowable<EncryptedLinkedInCredentials> getAll();

    CompletableFuture<Optional<EncryptedLinkedInCredentials>> findByUserId(String userId);

    CompletableFuture<Boolean> deleteByUserId(String userId);
}
