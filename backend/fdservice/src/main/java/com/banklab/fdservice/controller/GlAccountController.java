package com.banklab.fdservice.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banklab.fdservice.dto.GlAccountView;
import com.banklab.fdservice.service.FdAccountMapper;
import com.banklab.fdservice.service.GlAccountService;

import lombok.RequiredArgsConstructor;

/**
 * Read-only view of FD_GL_ACCOUNTS balances, for checking the double-entry postings.
 */
@RestController
@RequestMapping("/gl-accounts")
@RequiredArgsConstructor
public class GlAccountController {

    private final GlAccountService glAccountService;

    @GetMapping
    public List<GlAccountView> listGlAccounts() {
        return glAccountService.findAll().stream().map(FdAccountMapper::toResponse).toList();
    }
}
