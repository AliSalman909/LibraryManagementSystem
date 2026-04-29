package com.library.exception;

import com.library.messages.UserFacingMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessRuleException.class)
    public ModelAndView handleBusiness(BusinessRuleException ex) {
        log.warn("Business rule: {}", ex.getMessage());
        ModelAndView mv = new ModelAndView("error/generic");
        mv.addObject("status", HttpStatus.BAD_REQUEST.value());
        mv.addObject("title", "We could not complete that");
        mv.addObject("message", UserFacingMessages.orGeneric(ex.getMessage()));
        mv.setStatus(HttpStatus.BAD_REQUEST);
        return mv;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ModelAndView handleDataConflict(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation (user shown generic message): {}", ex.getMostSpecificCause().getMessage());
        ModelAndView mv = new ModelAndView("error/generic");
        mv.addObject("status", HttpStatus.CONFLICT.value());
        mv.addObject("title", "This information could not be saved");
        mv.addObject("message", UserFacingMessages.DATA_CONFLICT);
        mv.setStatus(HttpStatus.CONFLICT);
        return mv;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUpload(
            MaxUploadSizeExceededException ex, RedirectAttributes redirectAttributes) {
        log.warn("Upload too large: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("registrationGlobalError", UserFacingMessages.PROFILE_PHOTO_TOO_LARGE);
        return "redirect:/register";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNotFound(NoResourceFoundException ex) {
        ModelAndView mv = new ModelAndView("error/generic");
        mv.addObject("status", HttpStatus.NOT_FOUND.value());
        mv.addObject("title", "Page not found");
        mv.addObject("message", UserFacingMessages.PAGE_NOT_FOUND);
        mv.setStatus(HttpStatus.NOT_FOUND);
        return mv;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneric(Exception ex) {
        log.error("Unhandled error", ex);
        ModelAndView mv = new ModelAndView("error/generic");
        mv.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        mv.addObject("title", "Something went wrong");
        mv.addObject("message", UserFacingMessages.GENERIC_TRY_AGAIN);
        mv.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return mv;
    }
}
