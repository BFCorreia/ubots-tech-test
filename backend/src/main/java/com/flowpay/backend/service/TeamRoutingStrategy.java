package com.flowpay.backend.service;

import com.flowpay.backend.domain.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.regex.Pattern;

@Component
public class TeamRoutingStrategy {
    
    private static final Logger log = LoggerFactory.getLogger(TeamRoutingStrategy.class);
    private static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public Team determineTeam(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            log.warn("Subject is null or empty. Routing to OTHER.");
            return Team.OTHER;
        }
        
        String normalized = normalize(subject);
        
        if (normalized.contains("cartao") || normalized.contains("card")) {
            return Team.CARDS;
        }
        
        if (normalized.contains("emprestimo") || normalized.contains("loan")) {
            return Team.LOANS;
        }
        
        log.warn("Subject '{}' did not match any known patterns. Routing to OTHER.", subject);
        return Team.OTHER;
    }

    private String normalize(String input) {
        String nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        return DIACRITICS_AND_FRIENDS.matcher(nfdNormalizedString)
                .replaceAll("")
                .toLowerCase();
    }
}
