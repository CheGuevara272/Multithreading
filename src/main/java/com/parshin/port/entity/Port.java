package com.parshin.port.entity;

import com.parshin.port.exception.CustomException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Port {
    private static final Logger log = LogManager.getLogger();
    private static final AtomicReference<Port> instance = new AtomicReference<>();

    private double maxNumberOfContainers;
    private double numberOfDocks;
    private double containerMaxLoadMultiplier;
    private double containerMinLoadMultiplier;
    private AtomicInteger numberOfContainers;
    private ArrayDeque<Dock> dockPool;
    private AtomicInteger debit;
    private AtomicInteger credit;
    private AtomicInteger shipCounter;

    private final Lock locker = new ReentrantLock();
    private final Condition onGetDock = locker.newCondition();
    private final Condition onReturnDock = locker.newCondition();
    private final AtomicInteger dockGetCount = new AtomicInteger(0);
    private final AtomicInteger dockReturnCount = new AtomicInteger(0);

    private Port(){
    }

    public static Port getInstance() {
        while (true) {
            Port port = instance.get();
            if (port != null) {
                return port;
            }

            port = new Port();
            if (instance.compareAndSet(null, port)) {
                return port;
            }
        }
    }

    public double getMaxNumberOfContainers() {
        return maxNumberOfContainers;
    }

    public void setMaxNumberOfContainers(double maxNumberOfContainers) {
        this.maxNumberOfContainers = maxNumberOfContainers;
    }

    public double getNumberOfDocks() {
        return numberOfDocks;
    }

    public void setNumberOfDocks(double numberOfDocks) {
        this.numberOfDocks = numberOfDocks;
    }

    public double getContainerMaxLoadMultiplier() {
        return containerMaxLoadMultiplier;
    }

    public void setContainerMaxLoadMultiplier(double containerMaxLoadMultiplier) {
        this.containerMaxLoadMultiplier = containerMaxLoadMultiplier;
    }

    public double getContainerMinLoadMultiplier() {
        return containerMinLoadMultiplier;
    }

    public void setContainerMinLoadMultiplier(double containerMinLoadMultiplier) {
        this.containerMinLoadMultiplier = containerMinLoadMultiplier;
    }

    public AtomicInteger getNumberOfContainers() {
        return numberOfContainers;
    }

    public void setNumberOfContainers(AtomicInteger numberOfContainers) {
        this.numberOfContainers = numberOfContainers;
    }

    public void incrementNumberOfContainers() {
        numberOfContainers.getAndIncrement();
        debit.getAndIncrement();
    }

    public void decrementNumberOfContainers() {
        numberOfContainers.getAndDecrement();
        credit.getAndIncrement();
    }

    public void initialise() {
        debit = new AtomicInteger(0);
        credit = new AtomicInteger(0);
        dockPool = new ArrayDeque<>();
        shipCounter = new AtomicInteger(0);

        for (int i = 0; i < numberOfDocks; i++) {
            dockPool.push(new Dock(i, this));
        }
    }

    public Dock popDockPool() throws CustomException {
        locker.lock();
        Dock dock;
        try {
            while (dockPool.isEmpty()) {
                onGetDock.await();
            }
            dock = dockPool.pop();
            dockGetCount.incrementAndGet();
            log.log(Level.INFO, "", Thread.currentThread().getName(),
                    dock.getDockId(), dockPool.size());
            onReturnDock.signal();
            return dock;
        } catch (InterruptedException e) {
            log.log(Level.ERROR, "", Thread.currentThread().getName(), e);
        } finally {
            locker.unlock();
        }
        throw new CustomException("Dock was not given");
    }

    public void pushDockPool(Dock dock) throws CustomException {
        locker.lock();
        try {
            dockPool.push(dock);
            dockReturnCount.incrementAndGet();
            log.log(Level.INFO, "", Thread.currentThread().getName(), dockPool.size());
            onGetDock.signal();
        } finally {
            locker.unlock();
        }
    }

    public AtomicInteger getDebit() {
        return debit;
    }

    public void setDebit(AtomicInteger debit) {
        this.debit = debit;
    }

    public AtomicInteger getCredit() {
        return credit;
    }

    public void setCredit(AtomicInteger credit) {
        this.credit = credit;
    }

    public AtomicInteger getShipCounter() {
        return shipCounter;
    }

    public void incrementShipCounter() {
        shipCounter.getAndIncrement();
    }

    public void decrementShipCounter() {
        shipCounter.getAndDecrement();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Port.class.getSimpleName() + "[", "]")
                .add("maxNumberOfContainers=" + maxNumberOfContainers)
                .add("numberOfDocks=" + numberOfDocks)
                .add("containerMaxLoadMultiplier=" + containerMaxLoadMultiplier)
                .add("containerMinLoadMultiplier=" + containerMinLoadMultiplier)
                .add("numberOfContainers=" + numberOfContainers)
                .add("dockPool=" + dockPool)
                .add("debit=" + debit)
                .add("credit=" + credit)
                .toString();
    }
}
