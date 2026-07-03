package com.microservices.serviceadmin.repository;

import com.microservices.serviceadmin.entity.AdminInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminInviteRepository extends JpaRepository<AdminInvite, Long> {
Optional<AdminInvite> findByToken(String token);
}
