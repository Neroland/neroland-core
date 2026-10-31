package za.co.neroland.nerolandcore.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class PendingRegistrations<T> {
    private final List<T> registers = new ArrayList<>();
    private int attached;

    synchronized void add(T register) {
        registers.add(register);
    }

    synchronized void attachAll(Consumer<? super T> attach) {
        while (attached < registers.size()) {
            attach.accept(registers.get(attached));
            attached++;
        }
    }
}
