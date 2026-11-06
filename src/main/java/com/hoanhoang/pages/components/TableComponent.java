package com.hoanhoang.pages.components;

import static com.hoanhoang.core.element.ElementFactory.$;
import static com.hoanhoang.core.element.ElementFactory.$$;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.element.Elements;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;

/**
 * Generic row-based table: reads rows as objects, finds a row by its unique key (e.g. product name,
 * order number) and performs an action inside that row.
 */
public class TableComponent<T> {
    private final Element table;
    private final Elements rows;
    private final String description;
    private final Function<Element, T> rowMapper;
    private final Function<Element, String> rowKey;

    public TableComponent(
            By table, By rows, Function<Element, T> rowMapper, Function<Element, String> rowKey) {
        this.table = $(table);
        this.rows = $$(rows).visible();
        this.description = rows.toString();
        this.rowMapper = rowMapper;
        this.rowKey = rowKey;
    }

    public boolean isVisible() {
        return table.isVisible();
    }

    public int size() {
        return rows.size();
    }

    /**
     * Consistent snapshot of all visible rows; retried if the table re-renders while being read.
     */
    public List<T> getRows() {
        return readRows(
                "Read table rows " + description,
                visible -> visible.stream().map(rowMapper).toList());
    }

    public Optional<T> find(String key) {
        return readRows(
                "Read table row " + key,
                visible ->
                        visible.stream()
                                .filter(r -> key.equals(rowKey.apply(r)))
                                .findFirst()
                                .map(rowMapper));
    }

    /**
     * Re-reads explicitly incomplete rows during AJAX. Unexpected mapper/browser failures propagate
     * immediately; stale or missing DOM nodes are retried by the collection wrapper.
     */
    private <R> R readRows(String description, Function<List<Element>, R> read) {
        RowNotReadyException[] lastError = {null};
        try {
            return rows.waitUntil(
                    description,
                    visible -> {
                        try {
                            return read.apply(visible);
                        } catch (RowNotReadyException e) {
                            lastError[0] = e;
                            return null;
                        }
                    });
        } catch (TimeoutException e) {
            if (lastError[0] != null) {
                e.addSuppressed(lastError[0]);
            }
            throw e;
        }
    }

    public T get(String key) {
        return find(key)
                .orElseThrow(() -> new NoSuchElementException("Table row not found: " + key));
    }

    /** Live row query; child reads and actions resolve the key again when needed. */
    public Element row(String key) {
        return rows.filter(row -> key.equals(rowKey.apply(row))).first();
    }

    public Element firstRow() {
        return rows.first();
    }

    public void clickInRow(String key, By action) {
        row(key).find(action).click();
    }

    static String requiredCellValue(String value, String column) {
        if (value == null || value.isBlank()) {
            throw new RowNotReadyException("Empty table cell: " + column);
        }
        return value;
    }

    /** A mapper may request another read while the DOM contains an incomplete row. */
    public static final class RowNotReadyException extends RuntimeException {
        public RowNotReadyException(String message) {
            super(message);
        }
    }
}
