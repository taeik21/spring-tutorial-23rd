package com.ceos23.spring_boot.domain.test;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestService {
    private final TestRepository testRepository;

    @Transactional(readOnly = true)
    public List<Test> findAllTest() {
        return testRepository.findAll();
    }

}
