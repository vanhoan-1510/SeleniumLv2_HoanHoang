package com.hoanhoang.unit.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Tracks child JVMs so teardown can stop them after a test failure or timeout. */
public final class ChildProcesses implements AutoCloseable {
    private final ThreadLocal<List<Process>> children = ThreadLocal.withInitial(ArrayList::new);

    public Process start(ProcessBuilder builder) throws IOException {
        Process child = builder.start();
        children.get().add(child);
        return child;
    }

    @Override
    public void close() {
        List<Process> processes = children.get();
        children.remove();
        for (Process child : processes) {
            if (child.isAlive()) {
                child.destroyForcibly();
            }
        }
    }
}
