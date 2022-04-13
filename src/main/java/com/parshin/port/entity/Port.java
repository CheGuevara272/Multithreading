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
    private double numberOfPiers;
    private AtomicInteger numberOfContainers;
    private ArrayDeque<Pier> pierPool;
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

    public void setNumberOfPiers(double numberOfPiers) {
        this.numberOfPiers = numberOfPiers;
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
        pierPool = new ArrayDeque<>();
        shipCounter = new AtomicInteger(0);

        for (int i = 0; i < numberOfPiers; i++) {
            pierPool.push(new Pier(i + 1, this));
        }
    }

    public Pier popDockPool() throws CustomException {
        locker.lock();
        Pier pier;
        try {
            while (pierPool.isEmpty()) {
                onGetDock.await();
            }
            pier = pierPool.pop();
            dockGetCount.incrementAndGet();
            log.log(Level.INFO, "{} moored to pier {}. {} docks are free", Thread.currentThread().getName(), pier.getDockId(), pierPool.size());
            onReturnDock.signal();
            return pier;
        } catch (InterruptedException e) {
            log.log(Level.ERROR, "{} was interrupted", Thread.currentThread().getName(), e);
            Thread.currentThread().interrupt();
        } finally {
            locker.unlock();
        }
        throw new CustomException("Pier was not given");
    }

    public void pushDockPool(Pier pier) {
        locker.lock();
        pierPool.push(pier);
        dockReturnCount.incrementAndGet();
        log.log(Level.INFO, "{} unmoored from pier. {} docks are free", Thread.currentThread().getName(), pierPool.size());
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
                .add("numberOfDocks=" + numberOfPiers)
                .add("numberOfContainers=" + numberOfContainers)
                .add("pierPool=" + pierPool)
                .add("debit=" + debit)
                .add("credit=" + credit)
                .toString();
    }
}
