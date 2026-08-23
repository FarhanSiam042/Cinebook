package com.cinebook.userservice.security;

import com.cinebook.userservice.model.User;
import com.cinebook.userservice.repository.UserRepository;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		return new org.springframework.security.core.userdetails.User(
			user.getUsername(),
			user.getPassword(),
			user.isEnabled(),
			true,
			true,
			true,
			toAuthorities(user)
		);
	}

	private Collection<? extends GrantedAuthority> toAuthorities(User user) {
		return user.getRoles().stream()
			.map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
			.collect(Collectors.toSet());
	}
}