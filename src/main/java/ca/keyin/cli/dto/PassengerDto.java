package ca.keyin.cli.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PassengerDto(Long id, String firstName, String lastName, String phoneNumber) {}
