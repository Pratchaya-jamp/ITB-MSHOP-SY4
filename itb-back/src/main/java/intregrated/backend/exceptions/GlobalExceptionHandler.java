package intregrated.backend.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(
            ResponseStatusException ex, HttpServletRequest request) {
        HttpStatusCode statusCode = ex.getStatusCode();
        GeneralErrorResponse ger = new GeneralErrorResponse(
                statusCode.value(),
                statusCode.toString(),
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(statusCode).body(ger);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnhandledException(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        GeneralErrorResponse ger = new GeneralErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "Unexpected error occurred: " + ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(status).body(ger);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParams(MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        String message = "Missing required parameter: " + ex.getParameterName();

        GeneralErrorResponse ger = new GeneralErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ger);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MissingServletRequestParameterException ex,
                                                      HttpServletRequest request) {
        String message = "Missing required parameter: " + ex.getParameterName();

        GeneralErrorResponse ger = new GeneralErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ger);
    }
}
