package com.mealplanner.service;

import com.mealplanner.model.User;
import com.mealplanner.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");
        String picture = oAuth2User.getAttribute("picture");

        // Prefer email, but fall back to googleId if email missing
        User user = null;
        if (email != null) {
            user = userRepository.findByEmail(email).orElse(null);
        }
        if (user == null && googleId != null) {
            user = userRepository.findByGoogleId(googleId).orElse(null);
        }

        if (user == null) {
            String displayName = (name != null) ? name : (email != null ? email : "User");
            String gid = (googleId != null) ? googleId : (email != null ? email : "unknown");
            String em = (email != null) ? email : gid + "@google.local";
            user = new User(em, displayName, gid);
        }

        user.setLastLoginAt(LocalDateTime.now());
        if (picture != null && !picture.equals(user.getProfilePictureUrl())) {
            user.setProfilePictureUrl(picture);
        }
        if (googleId != null && (user.getGoogleId() == null || !googleId.equals(user.getGoogleId()))) {
            user.setGoogleId(googleId);
        }
        // update email/name if they were missing previously
        if (email != null && (user.getEmail() == null || user.getEmail().endsWith("@google.local"))) {
            user.setEmail(email);
        }
        if (name != null && (user.getName() == null || user.getName().isBlank())) {
            user.setName(name);
        }
        userRepository.save(user);

        return oAuth2User;
    }
}
