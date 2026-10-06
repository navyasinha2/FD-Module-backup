package com.banklab.fdservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banklab.fdservice.batch.EndOfDayService;
import com.banklab.fdservice.dto.AdvanceBusinessDateRequest;
import com.banklab.fdservice.dto.BusinessClockView;
import com.banklab.fdservice.dto.EndOfDayResult;
import com.banklab.fdservice.dto.SetBusinessDateRequest;
import com.banklab.fdservice.service.BusinessClockService;
import com.banklab.fdservice.service.FdAccountMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Business clock ("time travel") and a manual EOD trigger. Not in the original
 * OpenAPI excerpt; added so the simulated date the batch depends on can be inspected
 * and moved without SQL.
 */
@RestController
@RequestMapping("/business-clock")
@RequiredArgsConstructor
public class BusinessClockController {

    private final BusinessClockService businessClockService;
    private final EndOfDayService endOfDayService;

    @GetMapping
    public BusinessClockView getClock() {
        return FdAccountMapper.toResponse(businessClockService.getClock());
    }

    @PutMapping
    public BusinessClockView setBusinessDate(@Valid @RequestBody SetBusinessDateRequest request) {
        return FdAccountMapper.toResponse(businessClockService.setBusinessDate(request.businessDate()));
    }

    @PostMapping("/advance")
    public BusinessClockView advance(@Valid @RequestBody AdvanceBusinessDateRequest request) {
        return FdAccountMapper.toResponse(businessClockService.advance(request.days()));
    }

    @PostMapping("/eod")
    public EndOfDayResult runEndOfDay() {
        return endOfDayService.runEndOfDay();
    }
}
