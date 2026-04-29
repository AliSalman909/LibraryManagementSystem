package com.library.controller;

import com.library.exception.BusinessRuleException;
import com.library.messages.UserFacingMessages;
import com.library.security.LibraryUserDetails;
import com.library.service.ReservationService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LibrarianReservationController {

    private final ReservationService reservationService;

    public LibrarianReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * Lists all reservations for librarian management.
     * Supports filter: "ready" (default) or "all".
     */
    @GetMapping("/librarian/reservations")
    public String listReservations(
            @RequestParam(name = "filter", defaultValue = "ready") String filter,
            Model model) {
        boolean showAll = "all".equalsIgnoreCase(filter);
        model.addAttribute("reservations", showAll ? reservationService.listAll() : reservationService.listReady());
        model.addAttribute("filter", showAll ? "all" : "ready");
        return "librarian/reservations";
    }

    /** Mark a READY reservation as fulfilled. */
    @PostMapping("/librarian/reservations/{id}/fulfill")
    public String fulfill(
            @PathVariable("id") String reservationId,
            @AuthenticationPrincipal LibraryUserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            reservationService.markFulfilled(reservationId, principal.getUserId());
            redirectAttributes.addFlashAttribute("flashSuccess", "Reservation marked as fulfilled.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError",
                    UserFacingMessages.orGeneric(ex.getMessage()));
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute(
                    "flashError",
                    "Reservation could not be fulfilled right now due to a data conflict. Please refresh and try again.");
        }
        return "redirect:/librarian/reservations";
    }

    /** Cancel a reservation. */
    @PostMapping("/librarian/reservations/{id}/cancel")
    public String cancel(
            @PathVariable("id") String reservationId,
            RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelByLibrarian(reservationId);
            redirectAttributes.addFlashAttribute("flashSuccess", "Reservation cancelled.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError",
                    UserFacingMessages.orGeneric(ex.getMessage()));
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute(
                    "flashError",
                    "Reservation could not be cancelled right now due to a data conflict. Please refresh and try again.");
        }
        return "redirect:/librarian/reservations";
    }

    /** Expire all overdue READY reservations whose pickup window has passed. */
    @PostMapping("/librarian/reservations/expire-overdue")
    public String expireOverdue(RedirectAttributes redirectAttributes) {
        int count = reservationService.expireOverdueReservations();
        redirectAttributes.addFlashAttribute("flashSuccess",
                count == 0 ? "No expired reservations found." : count + " reservation(s) marked as expired.");
        return "redirect:/librarian/reservations";
    }
}
