package cc.coopersoft.keycloak.phone.providers.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendException extends Exception {

    private Integer statusCode = -1;
    private String errorCode = "";
    private String errorMessage = "";

    public MessageSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
