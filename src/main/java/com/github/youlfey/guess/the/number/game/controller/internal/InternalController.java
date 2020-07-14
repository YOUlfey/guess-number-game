package com.github.youlfey.guess.the.number.game.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

@RequiredArgsConstructor
public abstract class InternalController<T, ID> {
    private final PagingAndSortingRepository<T, ID> repository;

    @GetMapping
    public Page<T> getAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                          @RequestParam(required = false, defaultValue = "10") Integer size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public T getById(@PathVariable ID id) {
        return repository.findById(id).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
    }
}
