package com.microservices.auth.security;

import com.microservices.auth.entity.User;
import com.microservices.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur introuvable avec l'email : " + email
                ));

        List<GrantedAuthority> authorities = user.getRoles()
            .stream()
            .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.getName().name()))
            .toList();

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .disabled(!user.getEnabled())
                .authorities(authorities)
                .build();
    }
}