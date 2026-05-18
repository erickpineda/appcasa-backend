package com.appcasa.infrastructure.security;

import com.appcasa.domain.usuario.Usuario;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuditorAwareImpl implements AuditorAware<UUID> {

  @Override
  public Optional<UUID> getCurrentAuditor() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !auth.isAuthenticated()) {
      return Optional.empty();
    }

    Object principal = auth.getPrincipal();
    if (principal instanceof Usuario usuario) {
      return Optional.of(usuario.getId());
    }

    return Optional.empty();
  }
}
