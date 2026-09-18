package io.github.edgarshlee.webpconverter.web;

import io.github.edgarshlee.webpconverter.domain.exception.InvalidConversionRequestException;
import org.springframework.http.*;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(InvalidConversionRequestException.class)
    public ProblemDetail handleInvalidRequest(InvalidConversionRequestException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("변환 요청을 확인해 주세요");
        return problem;
    }
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(MissingServletRequestParameterException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "필수 입력값이 누락되었습니다.");
        problem.setTitle("변환 요청을 확인해 주세요");
        return problem;
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleTooLarge(MaxUploadSizeExceededException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE,
                "업로드 제한을 초과했습니다. 파일당 20MB, 요청당 100MB까지 사용할 수 있습니다.");
        problem.setTitle("파일이 너무 큽니다");
        return problem;
    }
}
