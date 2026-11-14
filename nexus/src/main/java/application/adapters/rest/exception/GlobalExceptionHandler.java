package application.adapters.rest.exception;

import application.domain.exceptions.DamagedInventoryReservationException;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.DuplicateEntityException;
import application.domain.exceptions.DuplicateUserException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InsufficientInventoryException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.exceptions.NegativeInventoryException;
import application.domain.exceptions.OrderAlreadyFinalizedException;
import application.domain.exceptions.SelfApprovalNotAllowedException;
import application.domain.exceptions.SelfRegistrationNotAllowedException;
import application.domain.exceptions.UnauthorizedDomainAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates business rule violations into HTTP answers with a clear message.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorResponse(int status, String error, String message) {
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException exception) {
        HttpStatus status = statusFor(exception);
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), exception.getClass().getSimpleName(), exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, "InvalidRequest", exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrity(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, "DataConflict", "The data conflicts with an existing record."));
    }

    private HttpStatus statusFor(DomainException exception) {
        if (exception instanceof EntityNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (exception instanceof DuplicateUserException || exception instanceof DuplicateEntityException
                || exception instanceof InvalidStatusTransitionException
                || exception instanceof OrderAlreadyFinalizedException
                || exception instanceof InsufficientInventoryException
                || exception instanceof NegativeInventoryException
                || exception instanceof DamagedInventoryReservationException) {
            return HttpStatus.CONFLICT;
        }
        if (exception instanceof UnauthorizedDomainAccessException
                || exception instanceof SelfApprovalNotAllowedException
                || exception instanceof SelfRegistrationNotAllowedException) {
            return HttpStatus.FORBIDDEN;
        }
        return HttpStatus.BAD_REQUEST;
    }
}
