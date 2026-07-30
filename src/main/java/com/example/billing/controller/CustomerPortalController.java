package com.example.billing.controller;

import com.example.billing.model.Invoice;
import com.example.billing.model.Reading;
import com.example.billing.service.PortalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/portal")
@CrossOrigin(origins = "*")
public class CustomerPortalController {

    private final PortalService portalService;

    public CustomerPortalController(PortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<Invoice>> getMyInvoices() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(portalService.getMyInvoices(auth.getName()));
    }

    @GetMapping("/usage")
    public ResponseEntity<List<Reading>> getMyUsage() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(portalService.getMyUsage(auth.getName()));
    }
}