package com.example.client.provideclarification.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(onConstructor_ = @JsonCreator)
public class ProvideClarification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String provideClarification;

    private String clarificationProvidedBy;

    private LocalDateTime clarifiedDate = LocalDateTime.now();




}
