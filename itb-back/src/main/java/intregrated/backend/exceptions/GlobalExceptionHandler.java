//package intregrated.backend.exceptions;
//
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.converter.HttpMessageNotReadableException;
//import org.springframework.validation.FieldError;
//import org.springframework.validation.method.ParameterValidationResult;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.MissingServletRequestParameterException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//import org.springframework.web.context.request.WebRequest;
//import org.springframework.web.method.annotation.HandlerMethodValidationException;
//
//import java.util.List;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//    @ExceptionHandler(IllegalArgumentException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ResponseEntity<Object> handleIllegalArgumentException(
//            IllegalArgumentException ex, WebRequest request) {
//        GeneralErrorResponse ger = new GeneralErrorResponse(
//                HttpStatus.BAD_REQUEST.value(), ex.getMessage()
//                , request.getDescription(false)
//        );
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ger);
//    }
//
//    @ExceptionHandler(MissingServletRequestParameterException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ResponseEntity<Object> handleMissingServletRequestParameterException(
//            MissingServletRequestParameterException ex, WebRequest request) {
//        GeneralErrorResponse ger = new GeneralErrorResponse(
//                HttpStatus.BAD_REQUEST.value(), ex.getMessage()
//                , request.getDescription(false)
//        );
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ger);
//    }
//
//    @ExceptionHandler(HttpMessageNotReadableException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ResponseEntity<Object> handleHttpMessageNotReadableException(
//            HttpMessageNotReadableException ex, WebRequest request) {
//        GeneralErrorResponse ger = new GeneralErrorResponse(
//                HttpStatus.BAD_REQUEST.value(), ex.getMessage()
//                , request.getDescription(false)
//        );
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ger);
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Object> handleMethodArgumentNotValidException(
//            MethodArgumentNotValidException ex, HttpServletRequest request) {
//        GeneralErrorResponse errorResponse = new GeneralErrorResponse(
//                HttpStatus.BAD_REQUEST.value()
//                , "Validation error. Check 'errors' field for details."
//                , request.getRequestURI());
//        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
//            errorResponse.addValidationError(
//                    fieldError.getField(), fieldError.getDefaultMessage());
//        }
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//    }
//
//    @ExceptionHandler(HandlerMethodValidationException.class)
//    public ResponseEntity<Object> handleHandlerMethodValidationException(
//            HandlerMethodValidationException exception
//            , HttpServletRequest request) {
//        GeneralErrorResponse errorResponse = new GeneralErrorResponse(
//                HttpStatus.BAD_REQUEST.value()
//                , "Validation error. Check 'errors' field for details."
//                , request.getRequestURI());
//        List<ParameterValidationResult> paramNames = exception.getParameterValidationResults();
//        for (ParameterValidationResult param : paramNames) {
//            errorResponse.addValidationError(
//                    param.getMethodParameter().getParameterName()
//                    , param.getResolvableErrors().get(0).getDefaultMessage());}
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);}
//}
