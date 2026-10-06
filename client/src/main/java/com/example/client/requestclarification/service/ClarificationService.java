package com.example.client.requestclarification.service;

import com.example.client.requestclarification.model.Clarification;
import com.example.client.requestclarification.repository.ClarificationRepository;
import com.example.client.user.model.RoleName;
import com.example.client.user.model.User;
import com.example.client.user.repository.UserRepository;
import com.example.client.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClarificationService {

    private final ClarificationRepository clarificationRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    /** Staff raise clarifications to customers and customers raise them to staff. */
    public Clarification addClarification(Clarification clarification, User requester){
        if (clarification.getRequestedTo() == null || clarification.getRequestedTo().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested To is required");
        }
        User requestedTo = userRepository.findById(clarification.getRequestedTo().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested To user does not exist"));
        String counterpart = userService.counterpartRole(requester);
        if (counterpart == null || !userService.hasRole(requestedTo, counterpart)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Staff can only ask customers, and customers can only ask staff");
        }
        clarification.setId(null);
        clarification.setRequestedTo(requestedTo);
        clarification.setRequestedBy(requester.getUserName());
        clarification.setRequestedDate(LocalDateTime.now());
        clarification.setProvideClarification(null);
        clarification.setClarificationProvidedBy(null);
        clarification.setClarifiedDate(null);
        return clarificationRepository.save(clarification);
    }

    public Clarification findClarificationById(Long id, User user) {
        Clarification clarification = clarificationRepository.findClarificationById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clarification " + id + " was not found"));
        if (!canView(clarification, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot view this clarification");
        }
        return clarification;
    }

    public List<Clarification>findAllClarifications(){
        return clarificationRepository.findAll();
    }

    public List<Clarification> findAssignedTo(User user) {
        return clarificationRepository.findByRequestedTo_IdOrderByIdDesc(user.getId());
    }

    public List<Clarification> findRequestedBy(User user) {
        return clarificationRepository.findByRequestedByOrderByIdDesc(user.getUserName());
    }

    /** Only the user the clarification was requested from can answer it, and only once. */
    public Clarification answer(Long id, String answer, User user) {
        Clarification clarification = clarificationRepository.findClarificationById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clarification " + id + " was not found"));
        if (clarification.getRequestedTo() == null || !clarification.getRequestedTo().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the requested user can answer this clarification");
        }
        if (clarification.getClarifiedDate() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This clarification has already been answered");
        }
        if (answer == null || answer.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clarification text is required");
        }
        clarification.setProvideClarification(answer);
        clarification.setClarificationProvidedBy(user);
        clarification.setClarifiedDate(new Date());
        return clarificationRepository.save(clarification);
    }

    private boolean canView(Clarification clarification, User user) {
        return userService.hasRole(user, RoleName.ADMIN)
                || user.getUserName().equals(clarification.getRequestedBy())
                || (clarification.getRequestedTo() != null && user.getId().equals(clarification.getRequestedTo().getId()));
    }

}
