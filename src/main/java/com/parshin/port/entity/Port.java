package com.parshin.port.entity;

import com.parshin.port.exception.CustomException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Port {
    private static final Logger log = LogManager.getLogger();
    private static final AtomicBoolean isInstanceCreated = new AtomicBoolean(false);
    private static Port instance;
    private static final ReentrantLock lock = new ReentrantLock();

    private double maxNumberOfContainers;
    private double numberOfDocks;
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
        if (!isInstanceCreated.get()) {
            lock.lock();
            try {
                if (instance == null) {
                    instance = new Port();
                    isInstanceCreated.set(true);
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    public double getMaxNumberOfContainers() {
        return maxNumberOfContainers;
    }

    public void setMaxNumberOfContainers(double maxNumberOfContainers) {
        this.maxNumberOfContainers = maxNumberOfContainers;
    }

    public void setNumberOfDocks(double numberOfDocks) {
        this.numberOfDocks = numberOfDocks;
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
            log.log(Level.INFO, "Thread {} get dock {},free docks - {}", Thread.currentThread().getName(), dock.getDockId(), dockPool.size());
            onReturnDock.signal();
            return dock;
        } catch (InterruptedException e) {
            log.log(Level.ERROR, "Thread {} was interrupted", Thread.currentThread().getName(), e);
            Thread.currentThread().interrupt();
        } finally {
            locker.unlock();
        }
        throw new CustomException("Dock was not given");
    }

    public void pushDockPool(Dock dock) {
        locker.lock();
        dockPool.push(dock);
        dockReturnCount.incrementAndGet();
        log.log(Level.INFO, "Thread {} return dock, free docks - {}", Thread.currentThread().getName(), dockPool.size());
        onGetDock.signal();
        locker.unlock();
    }

    public AtomicInteger getDebit() {
        return debit;
    }

    public AtomicInteger getCredit() {
        return credit;
    }

    public AtomicInteger getDockGetCount() {
        return dockGetCount;
    }

    public AtomicInteger getDockReturnCount() {
        return dockReturnCount;
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
                .add("numberOfContainers=" + numberOfContainers)
                .add("dockPool=" + dockPool)
                .add("debit=" + debit)
                .add("credit=" + credit)
                .toString();
    }
}
