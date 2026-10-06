package com.example.client.requestclarification.controller;

import com.example.client.requestclarification.model.Clarification;
import com.example.client.requestclarification.service.ClarificationService;
import com.example.client.user.model.User;
import com.example.client.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clarification")
public class ClarificationController {

    private final ClarificationService clarificationService;

    private final UserService userService;

    /** Staff or customer raises a clarification; the requester and date are set by the server. */
    @PostMapping("/add")
    public ResponseEntity<Clarification> addClarification(@RequestBody Clarification clarification, Authentication authentication){
        User requester = userService.currentUser(authentication);
        Clarification newClarification = clarificationService.addClarification(clarification, requester);
        return new ResponseEntity<>(newClarification, HttpStatus.CREATED);
    }

    /** Admin: every clarification. */
    @GetMapping("/all")
    public ResponseEntity<List<Clarification>> getAllClarification(){
        List<Clarification> clarifications = clarificationService.findAllClarifications();
        return new ResponseEntity<>(clarifications, HttpStatus.OK);
    }

    /** scope=assigned: requested from me. scope=requested: raised by me. */
    @GetMapping("/mine")
    public ResponseEntity<List<Clarification>> getMyClarifications(@RequestParam(defaultValue = "assigned") String scope,
                                                                   Authentication authentication) {
        User user = userService.currentUser(authentication);
        List<Clarification> clarifications = "requested".equals(scope)
                ? clarificationService.findRequestedBy(user)
                : clarificationService.findAssignedTo(user);
        return new ResponseEntity<>(clarifications, HttpStatus.OK);
    }

    @PatchMapping("/{id}/answer")
    public ResponseEntity<Clarification> answerClarification(@PathVariable Long id, @RequestBody Map<String, String> body,
                                                             Authentication authentication) {
        User user = userService.currentUser(authentication);
        Clarification clarification = clarificationService.answer(id, body.get("provideClarification"), user);
        return new ResponseEntity<>(clarification, HttpStatus.OK);
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<Clarification> getClarificationById(@PathVariable Long id, Authentication authentication) {
        Clarification clarification = clarificationService.findClarificationById(id, userService.currentUser(authentication));
        return new ResponseEntity<>(clarification, HttpStatus.OK);
    }
}
