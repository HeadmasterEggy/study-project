package com.example.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public interface AuthorizeService extends UserDetailsService {

    boolean sendValidateEmail(String email, String sessionId);

}
