package com.ceos23.spring_boot.controller;

import com.ceos23.spring_boot.domain.test.Test;
import com.ceos23.spring_boot.domain.test.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/tests")
public class TestController {
    private final TestService testService;

    @GetMapping
    public List<Test> findAllTest() {
        return testService.findAllTest();
    }
}
