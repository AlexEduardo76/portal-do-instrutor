package br.com.portal.controller;

import br.com.portal.dto.DashboardResponse;
import br.com.portal.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    DashboardResponse resumo(HttpSession session) {
        return service.resumo(session);
    }
}
