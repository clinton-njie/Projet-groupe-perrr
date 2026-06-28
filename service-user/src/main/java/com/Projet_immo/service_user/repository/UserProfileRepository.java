package com.Projet_immo.service_user.repository;

import com.Projet_immo.service_user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByAuthUserId(Long authUserId);
    Optional<UserProfile> findByEmail(String email);
    List<UserProfile> findByIsDeletedTrueAndScheduledDeletionAtBefore(Instant now);
}
