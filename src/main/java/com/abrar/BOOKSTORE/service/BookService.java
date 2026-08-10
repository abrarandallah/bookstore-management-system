package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.exception.ResourceNotFoundException;
import com.abrar.BOOKSTORE.repository.BookRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class BookService {

    // How many books show per page on the shelf. Not currently exposed as
    // a user-choosable option - if that changes, this is the one place
    // to add a bound/validated size parameter.
    public static final int DEFAULT_PAGE_SIZE = 12;

    @Autowired
    private BookRepository bRepo;

    public void save(Book b) {
        bRepo.save(b);
    }

    public List<Book> getAllBook() {
        return bRepo.findAll();
    }

    /**
     * @param term   free-text match against name/author, or blank for
     *               everything.
     * @param sortBy one of: name_asc, author_asc, newest, read_time_asc,
     *               read_time_desc. Falls back to name_asc for anything
     *               else, rather than erroring on an unrecognized/tampered
     *               value.
     */
    public List<Book> search(String term, String sortBy) {
        return search(term, null, sortBy);
    }

    /**
     * @param term    free-text match against name/author, or blank for
     *                everything.
     * @param genreId restrict results to books tagged with this genre, or
     *                null for every genre.
     * @param sortBy  one of: name_asc, author_asc, newest, read_time_asc,
     *                read_time_desc. Falls back to name_asc for anything
     *                else, rather than erroring on an unrecognized/tampered
     *                value.
     */
    public List<Book> search(String term, Integer genreId, String sortBy) {
        String safeTerm = term == null ? "" : term.trim();
        String effectiveSort = sortBy == null ? "" : sortBy;

        // estimatedReadMinutes is a derived value, not a persisted column,
        // so it can't be pushed down into a JPA Sort - fetch in a stable
        // order and sort by read time in memory instead.
        if (effectiveSort.equals("read_time_asc") || effectiveSort.equals("read_time_desc")) {
            List<Book> results = fetch(safeTerm, genreId, Sort.by("name").ascending());
            Comparator<Book> byReadTime = Comparator.comparingInt(Book::getEstimatedReadMinutes);
            results.sort(effectiveSort.equals("read_time_desc") ? byReadTime.reversed() : byReadTime);
            return results;
        }

        Sort sort = switch (effectiveSort) {
            case "author_asc" -> Sort.by("author").ascending();
            // No createdAt column exists, but id is auto-incrementing, so
            // higher id reliably means "added more recently".
            case "newest" -> Sort.by("id").descending();
            default -> Sort.by("name").ascending();
        };
        return fetch(safeTerm, genreId, sort);
    }

    private List<Book> fetch(String term, Integer genreId, Sort sort) {
        return genreId == null ? bRepo.search(term, sort) : bRepo.searchByGenre(term, genreId, sort);
    }

    /**
     * Same filtering/sorting as {@link #search(String, Integer, String)},
     * sliced down to one page. Slices the already-sorted/filtered list
     * rather than pushing paging down to the database - see
     * {@link PagedResult}'s class comment for why.
     *
     * @param page 1-indexed page to return. Values outside
     *             [1, totalPages] are clamped rather than producing an
     *             out-of-bounds error or an empty result for an
     *             otherwise-valid query.
     * @param size items per page; falls back to {@link #DEFAULT_PAGE_SIZE}
     *             for a non-positive value.
     */
    public PagedResult<Book> searchPaged(String term, Integer genreId, String sortBy, int page, int size) {
        List<Book> all = search(term, genreId, sortBy);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : size;
        int totalPages = Math.max(1, (int) Math.ceil((double) all.size() / safeSize));
        int safePage = Math.min(Math.max(1, page), totalPages);
        int fromIndex = Math.min((safePage - 1) * safeSize, all.size());
        int toIndex = Math.min(fromIndex + safeSize, all.size());
        return new PagedResult<>(all.subList(fromIndex, toIndex), safePage, safeSize, all.size());
    }

    /**
     * @throws ResourceNotFoundException if there are no books to pick from
     *                                   yet - lets the existing
     *                                   not-found error page handle it
     *                                   rather than needing a special case.
     */
    public Book getRandomBook() {
        List<Book> all = bRepo.findAll();
        if (all.isEmpty()) {
            throw new ResourceNotFoundException("No books available yet - check back once some have been added.");
        }
        return all.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(all.size()));
    }

    /**
     * @throws ResourceNotFoundException if no book exists with the given id.
     *                                   Previously this returned null, which caused
     *                                   NullPointerExceptions in
     *                                   every caller (editBook, getMylist, etc.)
     *                                   whenever an id didn't exist.
     */
    public Book getBookById(int id) {
        return bRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    /**
     * @throws ResourceNotFoundException if no book exists with the given id,
     *                                   instead of letting an
     *                                   EmptyResultDataAccessException escape from
     *                                   deleteById().
     */
    public void deleteById(int id) {
        if (!bRepo.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        bRepo.deleteById(id);
    }
}