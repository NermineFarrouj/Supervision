package net.supervision.superviseurapp.dtos;


/**
 * DTO (Data Transfer Object) pour recevoir les données de la requête du frontend.
 * Utilise un record Java 17 pour la concision et l'immuabilité.
 */
public record TestRequest(String ip, int port) {
}
