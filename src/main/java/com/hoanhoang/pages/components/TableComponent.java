package com.hoanhoang.pages.components;

import com.hoanhoang.core.util.UiWait;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

/**
 * Generic row-based table: reads rows as objects, finds a row by its unique key (e.g. product name,
 * order number) and performs an action inside that row.
 */
public class TableComponent<T> {
    private final By table;
    private final By rows;
    private final Function<WebElement, T> rowMapper;
    private final Function<WebElement, String> rowKey;

    public TableComponent(
            By table,
            By rows,
            Function<WebElement, T> rowMapper,
            Function<WebElement, String> rowKey) {
        this.table = table;
        this.rows = rows;
        this.rowMapper = rowMapper;
        this.rowKey = rowKey;
    }

    public boolean isVisible() {
        return UiWait.isVisible(table);
    }

    public int size() {
        return UiWait.visibleElements(rows).size();
    }

    /**
     * Consistent snapshot of all visible rows; retried if the table re-renders while being read.
     */
    public List<T> getRows() {
        return readRows(
                "Read table rows " + rows, visible -> visible.stream().map(rowMapper).toList());
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
     * Re-reads while a row is half re-rendered by AJAX (e.g. an empty quantity or price cell makes
     * the mapper throw). On timeout the last read error is attached to the TimeoutException.
     */
    private <R> R readRows(String description, Function<List<WebElement>, R> read) {
        RuntimeException[] lastError = {null};
        try {
            return UiWait.until(
                    description,
                    d -> {
                        try {
                            return read.apply(UiWait.visibleElements(rows));
                        } catch (StaleElementReferenceException e) {
                            throw e; // already retried by UiWait
                        } catch (RuntimeException e) {
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

    /** Waits until the row with the given key is visible and returns it. */
    public WebElement row(String key) {
        return UiWait.until(
                "Table row: " + key,
                d ->
                        UiWait.visibleElements(rows).stream()
                                .filter(r -> key.equals(rowKey.apply(r)))
                                .findFirst()
                                .orElse(null));
    }

    public WebElement firstRow() {
        return UiWait.until(
                "First table row " + rows,
                d -> UiWait.visibleElements(rows).stream().findFirst().orElse(null));
    }

    public void clickInRow(String key, By action) {
        row(key).findElement(action).click();
    }
}
