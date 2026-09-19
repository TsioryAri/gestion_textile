package com.example.textile.config;

import com.example.textile.entity.Role;
import com.example.textile.entity.RoleName;
import com.example.textile.entity.Utilisateur;
import com.example.textile.repository.RoleRepository;
import com.example.textile.repository.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository,
                      UtilisateurRepository utilisateurRepository,
                      PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role admin = creerRoleSiAbsent(RoleName.ADMIN);
        Role manager = creerRoleSiAbsent(RoleName.MANAGER);
        Role operator = creerRoleSiAbsent(RoleName.OPERATOR);
        Role user = creerRoleSiAbsent(RoleName.USER);

        creerUtilisateurSiAbsent("admin", "admin123", Set.of(admin));
        creerUtilisateurSiAbsent("manager", "manager123", Set.of(manager));
        creerUtilisateurSiAbsent("operator", "operator123", Set.of(operator));
        creerUtilisateurSiAbsent("user", "user123", Set.of(user));
    }

    private Role creerRoleSiAbsent(RoleName name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(null, name)));
    }

    private void creerUtilisateurSiAbsent(String username, String motDePasseClair, Set<Role> roles) {
        if (utilisateurRepository.findByUsername(username).isEmpty()) {
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setUsername(username);
            utilisateur.setPassword(passwordEncoder.encode(motDePasseClair));
            utilisateur.setEnabled(true);
            utilisateur.setRoles(roles);
            utilisateurRepository.save(utilisateur);
        }
    }
}