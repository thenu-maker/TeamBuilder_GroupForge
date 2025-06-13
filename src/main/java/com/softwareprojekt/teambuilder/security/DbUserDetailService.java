package com.softwareprojekt.teambuilder.security;

import com.softwareprojekt.teambuilder.entities.Benutzer;
import com.softwareprojekt.teambuilder.services.BenutzerService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

//Author: Silas Weber
@Service
public class DbUserDetailService implements UserDetailsService {

    private final BenutzerService benutzerService;

    public DbUserDetailService(BenutzerService benutzerService) {
        this.benutzerService = benutzerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Benutzer benutzer = benutzerService.findBenutzerByUsername(username);
        if (benutzer != null) {
            return User.builder()
                    .username(benutzer.getUsername())
                    .password(benutzer.getPassword())
                    .roles(benutzer.getRole().toString())
                    .build();
        } else {
            throw new UsernameNotFoundException(username);
        }
    }
}
