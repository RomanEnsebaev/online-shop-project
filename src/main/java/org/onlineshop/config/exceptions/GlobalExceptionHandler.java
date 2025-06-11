package org.onlineshop.config.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handle404(NoHandlerFoundException ex) {
        log.warn("NoHandlerFound: {}", ex.getRequestURL(), ex);
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("path", ex.getRequestURL());
        return mav;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ModelAndView handle405(HttpRequestMethodNotSupportedException ex) {
        log.warn("MethodNotAllowed: {}", ex.getMethod(), ex);
        ModelAndView mav = new ModelAndView("error/405");
        mav.addObject("method", ex.getMethod());
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handle500(Exception ex) {
        log.error("Unhandled exception", ex);
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("errorMessage", "Произошла техническая ошибка. Попробуйте позже.");
        return mav;
    }
}
