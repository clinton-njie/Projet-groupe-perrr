package com.microservices.serviceadmin.repository;

import com.microservices.serviceadmin.entity.RoleChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleChangeRequestRepository extends JpaRepository<RoleChangeRequest, Long> {
long countByAuthUserId(Long authUserId);
}
