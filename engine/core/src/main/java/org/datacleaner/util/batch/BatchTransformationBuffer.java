/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.util.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.datacleaner.api.Transformer;
import org.datacleaner.job.concurrent.PreviousErrorsExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A batch transformation buffer utility for archieving minor batch operations
 * while preserving the one-by-one transformation interface of
 * {@link Transformer}.
 * 一个批处理转换缓冲区实用程序，用于归档次要批处理操作，同时保留{@link Transformer}的一对一转换接口。
 *
 * @param <I>
 *            the input type
 * @param <O>
 *            the output type
 */
public class BatchTransformationBuffer<I, O> {

    // default 1 second interval of flushing
    public static final int DEFAULT_FLUSH_INTERVAL = 1000;
    // default max 20 items in buffer
    public static final int DEFAULT_MAX_BATCH_SIZE = 20;
    private static final Logger logger = LoggerFactory.getLogger(BatchTransformationBuffer.class);
    private static final long[] AWAIT_TIMES = { 20, 50, 100, 100, 200 };

    private final BatchTransformation<I, O> _transformation;
    private final BlockingQueue<BatchEntry<I, O>> _queue;
    private final AtomicInteger _batchNo;
    private final int _maxBatchSize;
    private final ScheduledExecutorService _threadPool;
    private final int _flushInterval;

    private Throwable exception;

    public BatchTransformationBuffer(final BatchTransformation<I, O> transformation) {
        this(transformation, DEFAULT_MAX_BATCH_SIZE, DEFAULT_FLUSH_INTERVAL);
    }

    public BatchTransformationBuffer(final BatchTransformation<I, O> transformation, final int maxBatchSize,
            final int flushIntervalMillis) {
        _transformation = transformation;
        _flushInterval = flushIntervalMillis;
        _maxBatchSize = maxBatchSize;
        _queue = new ArrayBlockingQueue<>(maxBatchSize);
        _batchNo = new AtomicInteger();
        _threadPool = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        logger.info("start()");
        _threadPool.scheduleAtFixedRate(createFlushCommand(), _flushInterval, _flushInterval, TimeUnit.MILLISECONDS);
    }

    private Runnable createFlushCommand() {
        return () -> {
            try {
                do {
                    flushBuffer(true);
                } while (!_queue.isEmpty());
            } catch (final Throwable t) {
                logger.warn("Cannot flush buffer", t);
                exception = t;
                shutdown();
            }
        };
    }

    public int getBatchCount() {
        return _batchNo.get();
    }

    public void flushBuffer() {
        flushBuffer(false);
    }

    private void flushBuffer(final boolean scheduled) {
        if (_queue.isEmpty()) {
            // do nothing when queue is empty
            return;
        }
        if (!scheduled) {
            if (_queue.size() < _maxBatchSize) {
                logger.debug("Batch ignored, flush operation not scheduled and queue is not full");
                return;
            }
        }

        final List<BatchEntry<?, O>> entries = new ArrayList<>(_maxBatchSize);

        final int batchSize = _queue.drainTo(entries);

        if (batchSize == 0) {
            logger.debug("Batch ignored, no elements left in queue");
            // it may happen that multiple threads try to flush at the same
            // time - in this case we want to stop them here.
            return;
        }

        final int batchNumber = _batchNo.incrementAndGet();

        logger.info("Batch #{} - Preparing {} entries, scheduled={}", batchNumber, batchSize, scheduled);

        final Object[] input = new Object[batchSize];
        for (int i = 0; i < batchSize; i++) {
            input[i] = entries.get(i).getInput();
        }

        final BatchSource<I> source = new ArrayBatchSource<>(input);
        final BatchEntryBatchSink<O> sink = new BatchEntryBatchSink<>(entries);

        _transformation.map(source, sink);

        logger.info("Batch #{} - Finished", batchNumber, batchSize);
    }

    public void shutdown() {
        logger.info("shutdown()");
        _threadPool.shutdown();
    }

    public O transform(final I input) {
        final BatchEntry<I, O> entry = new BatchEntry<>(input);

        while (!_queue.offer(entry)) {
            flushBuffer();
        }

        int attemptIndex = 0;
        while (true) {
            rethrowException();
            if (_threadPool.isShutdown()) {
                // Re-check the exception from background thread - it is preferred
                rethrowException();
                throw new PreviousErrorsExistException("Transformer closed");
            }
            final long waitTime = (attemptIndex < AWAIT_TIMES.length
                    ? AWAIT_TIMES[attemptIndex]
                    : AWAIT_TIMES[AWAIT_TIMES.length - 1]);

            try {
                final boolean finished = entry.await(waitTime);
                if (finished) {
                    return entry.getOuput();
                }

                flushBuffer();
                attemptIndex++;
            } catch (final Exception e) {
                if (exception == null) {
                    exception = e;
                }
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new IllegalStateException(e);
            }
        }
    }

    /** Re-throws the exception from background thread */
    private void rethrowException() {
        if (exception != null) {
            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            }
            throw new RuntimeException(exception);
        }
    }
}
