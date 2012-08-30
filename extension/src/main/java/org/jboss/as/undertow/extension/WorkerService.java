package org.jboss.as.undertow.extension;

import java.io.IOException;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a> (c) 2012 Red Hat Inc.
 */
public class WorkerService implements Service<XnioWorker> {
    private XnioWorker worker;
    private int writeThreads = 4;
    private int readThreads = 4;
    private int workerThreads = 10;

    public WorkerService(int writeThreads, int readThreads, int workerThreads) {
        this.writeThreads = writeThreads;
        this.readThreads = readThreads;
        this.workerThreads = workerThreads;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        final Xnio xnio = Xnio.getInstance();
        final OptionMap map = OptionMap.builder()
                .set(Options.WORKER_WRITE_THREADS, writeThreads)
                .set(Options.WORKER_READ_THREADS, readThreads)
                .set(Options.CONNECTION_LOW_WATER, 1000000)
                .set(Options.CONNECTION_HIGH_WATER, 1000000)
                .set(Options.WORKER_TASK_CORE_THREADS, workerThreads)
                .set(Options.WORKER_TASK_MAX_THREADS, 16)
                .set(Options.TCP_NODELAY, true)
                .getMap();
        try {
            worker = xnio.createWorker(map);
        } catch (IOException e) {
            throw new StartException("Could not create worker!",e);
        }
    }

    @Override
    public void stop(StopContext stopContext) {
        worker.shutdown();
    }

    @Override
    public XnioWorker getValue() throws IllegalStateException, IllegalArgumentException {
        return worker;
    }
}
