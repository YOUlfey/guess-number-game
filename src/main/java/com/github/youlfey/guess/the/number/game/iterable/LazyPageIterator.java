package com.github.youlfey.guess.the.number.game.iterable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class LazyPageIterator<T> implements Iterator<T> {
    private Integer size = 10;
    private Integer currentPageIndex;
    private Page<T> currentPage;
    private Iterator<T> iteratorCurrentPage;
    private Function<Pageable, Page<T>> pageResolver;

    public LazyPageIterator(Function<Pageable, Page<T>> pageResolver) {
        this.pageResolver = pageResolver;
        this.currentPageIndex = 0;
        nextPage();
    }

    public LazyPageIterator(Integer size, Function<Pageable, Page<T>> pageResolver) {
        this.size = size;
        this.pageResolver = pageResolver;
        this.currentPageIndex = 0;
        nextPage();
    }

    @Override
    public boolean hasNext() {
        return iteratorCurrentPage.hasNext() || currentPage.hasNext();
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        if (!iteratorCurrentPage.hasNext()) {
            nextPage();
        }
        return iteratorCurrentPage.next();
    }

    private void nextPage() {
        Pageable pageable = getPageable();
        currentPage = pageResolver.apply(pageable);
        iteratorCurrentPage = currentPage.iterator();
        currentPageIndex++;
    }

    private Pageable getPageable() {
        return PageRequest.of(currentPageIndex, size);
    }
}
