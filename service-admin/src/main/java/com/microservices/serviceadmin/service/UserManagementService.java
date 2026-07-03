package com.microservices.serviceadmin.service;

import com.microservices.serviceadmin.feign.UserProfileResponse;
import com.microservices.serviceadmin.feign.UserServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManagementService {

private final UserServiceClient userServiceClient;

public List<UserProfileResponse> listerUtilisateurs() {
return userServiceClient.getAllProfiles();
}

public void desactiverUtilisateur(Long authUserId) {
userServiceClient.deactivateProfile(authUserId);
}

public void reactiverUtilisateur(Long authUserId) {
userServiceClient.reactivateProfile(authUserId);
}
}
