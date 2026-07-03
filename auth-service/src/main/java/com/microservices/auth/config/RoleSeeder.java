package com.microservices.auth.config;

import com.microservices.auth.entity.Role;
import com.microservices.auth.enumtype.RoleName;
import com.microservices.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

private final RoleRepository roleRepository;

@Override
public void run(String... args) {
for (RoleName roleName : RoleName.values()) {
if (!roleRepository.existsByName(roleName)) {
roleRepository.save(Role.builder().name(roleName).build());
}
}
}
}
