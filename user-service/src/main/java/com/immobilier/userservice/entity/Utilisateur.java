package com.immobilier.userservice.entity;

import com.immobilier.userservice.enums.Role;
import com.immobilier.userservice.enums.UserStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
@Entity  //Table SQL
@Table(name = "utilisateurs") //Nom reel de la table en bd
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder //Construit l'user avec ses attributs *.build();
@Data //Pour générer les getters, setters, toString, equals et hashCode automatiquement
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nom;
    
    @NotBlank
    @Column(nullable = false)
    private String prenom;
    
    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;
    
    private String telephone;
    private String photoUrl;    

    @NotBlank
    @Column(nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder.Default
    private Boolean actif = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification; 

    private String cniRectoUrl;
    private String cniVersoUrl;
    private String adresseProfessionnelle;

    @PrePersist
    public void prePersist() {

        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();

        if (this.role == null) {
            this.role = Role.CLIENT;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

     public void setRole(Role role) {
        this.role = role;
    }
}
