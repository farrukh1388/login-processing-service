package com.tipico.loginprocessing.repository;

import com.tipico.loginprocessing.entity.LoginTrackingResult;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginTrackingResultRepository extends JpaRepository<LoginTrackingResult, UUID> {
  Optional<LoginTrackingResult> findByMessageId(UUID messageId);
}
