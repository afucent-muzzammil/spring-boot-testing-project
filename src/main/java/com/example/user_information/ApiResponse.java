package com.example.user_information;

import java.time.LocalDateTime;


import com.example.user_information.Common.Status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ApiResponse<T> {

	private int code; 
	private Status status;
	private String message;
	private T data;			// -> T data makes it generic — you can return any type of payload(user, list, error message, etc.)
	private String path;
	private LocalDateTime timestamp;
	
	
}
