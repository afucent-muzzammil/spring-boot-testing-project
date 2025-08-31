package com.example.user_information.Exception;

import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

import com.example.user_information.ApiResponse;
import com.example.user_information.Common.Status;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice  // This annotation ensures that this class will handle exceptions globally across all controllers
public class GlobalExceptionHandler {
	
	
	
	// A helper method to construct the ApiResponse in a consistent format
    private ApiResponse<List<String>> buildApiResponse(
            int code,
            Status status,
            String message,
            List<String> data,
            String path,
            LocalDateTime timestamp) {

        // Build and return an ApiResponse object with all required details
        return new ApiResponse<>(
                code,  // HTTP status code (e.g., 400, 500)
                status,  // Status (e.g., SUCCESS, FAIL, ERROR)
                message,  // Message describing the result
                data,  // List of error messages or relevant data
                path,  // The endpoint (URL) that triggered the error
                timestamp  // The timestamp of when the response was generated
        );
    }

    
    

    // Handles validation errors (for example, when @Valid validation fails)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
    	    	
        // Extract all error messages from the validation exceptions
        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(err -> err.getDefaultMessage())  // Get the default message for each error
                .collect(Collectors.toList());  // Collect all error messages into a list

        log.warn("Method Argument Not Valid: {}" , ex.getMessage());
        
        // Return the response with the validation errors in a structured format
        return new ResponseEntity<>(
                buildApiResponse(
                    HttpStatus.BAD_REQUEST.value(),  // HTTP Status Code 400 for bad request
                    Status.FAIL,  // Status FAIL as the validation failed
                    "Validation failed",  // Message to inform validation failed
                    errors,  // List of error messages
                    request.getRequestURI(),  // Path of the request that triggered the error
                    LocalDateTime.now()  // Timestamp when the error occurred
                ),
                HttpStatus.BAD_REQUEST  // Return BAD_REQUEST HTTP status
        );
    }

    
    
    
    // Handles business logic errors (for example, when an email or phone number is already registered)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
    	
        log.warn("Business validation failed: {}" , ex.getMessage());

        // Return the response with the business validation error
        return new ResponseEntity<>(
                buildApiResponse(
                    HttpStatus.BAD_REQUEST.value(),  // HTTP Status Code 400
                    Status.FAIL,  // Status FAIL for business logic errors
                    "Business validation failed",  // Message explaining the failure
                    List.of(ex.getMessage()),  // List containing the specific error message
                    request.getRequestURI(),  // Path of the request that triggered the error
                    LocalDateTime.now()  // Timestamp of when the error occurred
                ),
                HttpStatus.BAD_REQUEST  // Return BAD_REQUEST HTTP status
        );
    }
    
    
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleNotFound(ResourceNotFoundException ex,
    		HttpServletRequest request){
    	
    	log.warn("Resources Not Found: {}" , ex.getMessage());
    	
    	return new ResponseEntity<>(
    			buildApiResponse(
    					HttpStatus.NOT_FOUND.value(),
    					Status.FAIL,
    					"Resources Not Found",
    					List.of(ex.getMessage()),
    					request.getRequestURI(),
    					LocalDateTime.now()
    					),
    			HttpStatus.NOT_FOUND
    			);
    }
    

    // A catch-all handler for unexpected exceptions (e.g., server errors)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<List<String>>> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request) {

        // Print the stack trace for debugging purposes
        ex.printStackTrace();
        
        log.error("INTERNAL_SERVER_ERROR: {}" , ex.getMessage());

        // Return the response for unexpected errors
        return new ResponseEntity<>(
                buildApiResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),  // HTTP Status Code 500 for internal server error
                    Status.ERROR,  // Status ERROR as this is an unexpected error
                    "Unexpected error occurred",  // Generic error message
                    List.of("Something went wrong"),  // General error message indicating the failure
                    request.getRequestURI(),  // Path of the request that triggered the error
                    LocalDateTime.now()  // Timestamp when the error occurred
                ),
                HttpStatus.INTERNAL_SERVER_ERROR  // Return INTERNAL_SERVER_ERROR HTTP status
        );
    }

    
            
    
}










/*


1. @RestControllerAdvice
This annotation makes the GlobalExceptionHandler class globally handle exceptions for all controllers in the application. It catches exceptions and returns a custom response to the client.



2. handleValidationException Method
This method handles validation errors. It listens for MethodArgumentNotValidException, which occurs when input validation fails (for example, when @Valid fails).

The error messages from the validation are collected and returned in a list as part of the response.

The response is wrapped in a ResponseEntity, with a status of HttpStatus.BAD_REQUEST (400) to indicate a client-side issue.



3. handleIllegalArgument Method
This method handles business validation errors, such as when an argument passed to the method is invalid (e.g., duplicate email or phone number).

The error message from the exception is returned as part of the response.

The status is also BAD_REQUEST (400), as the client provided invalid input.



4. handleUnexpectedException Method
This method is a catch-all handler for any other unexpected exceptions. For example, if a server-side error occurs.

The status is set to INTERNAL_SERVER_ERROR (500), which indicates a server error.

The exception's stack trace is printed for debugging purposes (though this should be logged in production).



5. buildApiResponse Method
This is a helper method to standardize the response structure. It creates an ApiResponse object containing:

The HTTP status code (e.g., 400 or 500).

The status (SUCCESS, FAIL, ERROR).

A message describing the result.

A list of error messages or relevant data.

The path (URL) of the request that caused the error.

A timestamp indicating when the error occurred.



6. ApiResponse Class
The ApiResponse class is a generic wrapper for API responses, allowing consistent formatting across the application.

*/
