package com.Projet_immo.service_user.repository;

import com.Projet_immo.service_user.entity.RoleChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoleChangeRequestRepository extends JpaRepository<RoleChangeRequest, Long> {
    List<RoleChangeRequest> findByStatus(String status);
    List<RoleChangeRequest> findByAuthUserId(Long authUserId);
}
