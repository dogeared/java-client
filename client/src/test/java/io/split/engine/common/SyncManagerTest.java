package io.split.engine.common;

import io.split.engine.sse.NotificationManagerKeeper;
import io.split.engine.sse.SSEHandler;
import io.split.engine.sse.dtos.ErrorNotification;
import org.junit.Test;
import org.mockito.Mockito;

public class SyncManagerTest {
    private final Synchronizer _synchronizer;
    private final PushManager _pushManager;
    private final SSEHandler _sseHandler;
    private final NotificationManagerKeeper _notificationManagerKeeper;

    public SyncManagerTest() {
        _synchronizer = Mockito.mock(Synchronizer.class);
        _pushManager = Mockito.mock(PushManager.class);
        _sseHandler = Mockito.mock(SSEHandler.class);
        _notificationManagerKeeper = Mockito.mock(NotificationManagerKeeper.class);
    }

    @Test
    public void startWithStreamingFalseShouldStartPolling() {
        SyncManager syncManager = new SyncManagerImp(false, _synchronizer, _pushManager, _sseHandler, _notificationManagerKeeper);
        syncManager.start();

        Mockito.verify(_synchronizer, Mockito.times(1)).startPeriodicFetching();
        Mockito.verify(_synchronizer, Mockito.times(0)).syncAll();
        Mockito.verify(_pushManager, Mockito.times(0)).start();
    }

    @Test
    public void startWithStreamingTrueShouldStartPolling() {
        SyncManager syncManager = new SyncManagerImp(true, _synchronizer, _pushManager, _sseHandler, _notificationManagerKeeper);
        syncManager.start();

        Mockito.verify(_synchronizer, Mockito.times(0)).startPeriodicFetching();
        Mockito.verify(_synchronizer, Mockito.times(1)).syncAll();
        Mockito.verify(_pushManager, Mockito.times(1)).start();
    }

    @Test
    public void onStreamingAvailable() {
        SyncManagerImp syncManager = new SyncManagerImp(true, _synchronizer, _pushManager, _sseHandler, _notificationManagerKeeper);

        syncManager.onStreamingAvailable();

        Mockito.verify(_synchronizer, Mockito.times(1)).stopPeriodicFetching();
        Mockito.verify(_synchronizer, Mockito.times(1)).syncAll();
        Mockito.verify(_pushManager, Mockito.times(1)).startWorkers();
    }

    @Test
    public void onStreamingDisabled() {
        SyncManagerImp syncManager = new SyncManagerImp(true, _synchronizer, _pushManager, _sseHandler, _notificationManagerKeeper);

        syncManager.onStreamingDisabled();

        Mockito.verify(_synchronizer, Mockito.times(1)).startPeriodicFetching();
        Mockito.verify(_pushManager, Mockito.times(1)).stopWorkers();
    }

    @Test
    public void onStreamingShutdown() {
        SyncManagerImp syncManager = new SyncManagerImp(true, _synchronizer, _pushManager, _sseHandler, _notificationManagerKeeper);

        syncManager.onStreamingShutdown();

        Mockito.verify(_pushManager, Mockito.times(1)).stop();
    }

    @Test
    public void onConnected() {
        SyncManagerImp syncManager = new SyncManagerImp(true, _synchronizer, _pushManager, _sseHandler, _notificationManagerKeeper);

        syncManager.onConnected();

        Mockito.verify(_synchronizer, Mockito.times(1)).stopPeriodicFetching();
        Mockito.verify(_synchronizer, Mockito.times(1)).syncAll();
    }

    @Test
    public void onDisconnect() {
        SyncManagerImp syncManager = new SyncManagerImp(true, _synchronizer, _pushManager, _sseHandler, _notificationManagerKeeper);

        syncManager.onDisconnect(false);

        Mockito.verify(_synchronizer, Mockito.times(1)).startPeriodicFetching();
    }

    @Test
    public void onDisconnectAndReconnect() {
        SyncManagerImp syncManager = new SyncManagerImp(true, _synchronizer, _pushManager, _sseHandler, _notificationManagerKeeper);

        syncManager.onDisconnect(true);

        Mockito.verify(_synchronizer, Mockito.times(1)).startPeriodicFetching();
        Mockito.verify(_synchronizer, Mockito.times(1)).syncAll();
        Mockito.verify(_pushManager, Mockito.times(1)).start();
    }

    @Test
    public void onErrorNotification() {
        ErrorNotification errorNotification = new ErrorNotification("503", "Timeout error", 50003);

        SyncManagerImp syncManager = new SyncManagerImp(true, _synchronizer, _pushManager, _sseHandler, _notificationManagerKeeper);
        syncManager.onErrorNotification(errorNotification);

        Mockito.verify(_pushManager, Mockito.times(0)).stop();
        Mockito.verify(_synchronizer, Mockito.times(0)).syncAll();
        Mockito.verify(_pushManager, Mockito.times(0)).start();
    }

    @Test
    public void onErrorNotificationTokenExpire() {
        ErrorNotification errorNotification = new ErrorNotification("401", "Token expire", 40142);

        SyncManagerImp syncManager = new SyncManagerImp(true, _synchronizer, _pushManager, _sseHandler, _notificationManagerKeeper);
        syncManager.onErrorNotification(errorNotification);

        Mockito.verify(_pushManager, Mockito.times(1)).stop();
        Mockito.verify(_synchronizer, Mockito.times(1)).syncAll();
        Mockito.verify(_pushManager, Mockito.times(1)).start();
    }
}
