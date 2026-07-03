package com.microservices.serviceadmin.controller;

import com.microservices.serviceadmin.feign.UserProfileResponse;
import com.microservices.serviceadmin.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

private final UserManagementService userManagementService;

@GetMapping
public ResponseEntity<List<UserProfileResponse>> listerUtilisateurs() {
return ResponseEntity.ok(userManagementService.listerUtilisateurs());
}

@PutMapping("/{authUserId}/deactivate")
public ResponseEntity<Void> desactiverUtilisateur(@PathVariable Long authUserId) {
userManagementService.desactiverUtilisateur(authUserId);
return ResponseEntity.ok().build();
}

@PutMapping("/{authUserId}/reactivate")
public ResponseEntity<Void> reactiverUtilisateur(@PathVariable Long authUserId) {
userManagementService.reactiverUtilisateur(authUserId);
return ResponseEntity.ok().build();
}
}
