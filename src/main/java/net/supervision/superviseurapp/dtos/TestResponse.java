package net.supervision.superviseurapp.dtos;


import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO pour envoyer la réponse au frontend.
 * @JsonInclude(JsonInclude.Include.NON_NULL) garantit que les champs nuls (comme version ou error)
 * ne sont pas inclus dans le JSON final, ce qui correspond au comportement attendu par le frontend.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TestResponse(
        String status,
        String version,
        String error
) {
    // Constructeur simplifié pour les cas sans version ni erreur
    public TestResponse(String status) {
        this(status, null, null);
    }
}