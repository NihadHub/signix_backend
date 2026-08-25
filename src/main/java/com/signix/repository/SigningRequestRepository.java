package com.signix.repository;

import com.signix.model.SigningRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SigningRequestRepository extends JpaRepository<SigningRequest,Long> {

    Optional<SigningRequest> findSigningRequestByToken(String token);
    boolean existsByToken(String token);

}
