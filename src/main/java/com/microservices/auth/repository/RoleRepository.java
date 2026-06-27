package com.microservices.auth.repository;

import com.microservices.auth.entity.Role;
import com.microservices.auth.enumtype.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);

    boolean existsByName(RoleName name);
}