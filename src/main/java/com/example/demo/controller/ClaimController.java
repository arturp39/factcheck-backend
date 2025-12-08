package com.example.demo.controller;

import com.example.demo.entity.Article;
import com.example.demo.entity.ClaimLog;
import com.example.demo.service.ClaimService;
import com.example.demo.service.VertexAiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ClaimController {

    private final ClaimService claimService;
    private final VertexAiService vertexService;

    public ClaimController(ClaimService claimService, VertexAiService vertexService) {
        this.claimService = claimService;
        this.vertexService = vertexService;
    }

    @PostMapping("/verify")
    public String verify(@RequestParam String claim, Model model) {

        System.out.println(">>> ClaimController.verify CALLED with claim = " + claim);

        ClaimLog saved = claimService.saveClaim(claim);

        List<Article> evidence = claimService.searchEvidence(claim);
        System.out.println(">>> /verify evidence size = " + evidence.size());

        String aiResponse = vertexService.askModel(claim, evidence);

        claimService.storeModelAnswer(saved.getId(), aiResponse);

        model.addAttribute("claim", claim);
        model.addAttribute("evidence", evidence);
        model.addAttribute("response", aiResponse);

        return "result";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }
}