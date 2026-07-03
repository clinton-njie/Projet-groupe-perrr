package com.microservices.serviceadmin.service;

import com.microservices.serviceadmin.feign.SignalementResponse;
import com.microservices.serviceadmin.feign.SignalementServiceClient;
import com.microservices.serviceadmin.feign.StatutRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SignalementManagementService {

private final SignalementServiceClient signalementServiceClient;

// Types d'objet de signalement disponibles (fixés côté service-signalements).
private static final List<String> OBJETS_SIGNALEMENT = List.of("AGENT", "TECHNIQUE", "AUTRE");

public List<SignalementResponse> listerSignalements() {
return signalementServiceClient.getAllSignalements();
}

public SignalementResponse getSignalement(Long id) {
return signalementServiceClient.getSignalement(id);
}

public SignalementResponse changerStatut(Long id, String statut) {
return signalementServiceClient.updateStatut(id, new StatutRequest(statut));
}

public List<String> listerObjetsSignalement() {
return OBJETS_SIGNALEMENT;
}
}
