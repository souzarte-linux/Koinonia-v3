package androidx.work;

import java.util.List;
import java.util.UUID;
import android.app.PendingIntent;
import androidx.lifecycle.LiveData;
import com.google.common.util.concurrent.ListenableFuture;
import kotlinx.coroutines.flow.Flow;

public class TestWorkManager extends WorkManager {
    private static final Operation DUMMY_OPERATION = new Operation() {
        @Override
        public ListenableFuture<State.SUCCESS> getResult() {
            return null;
        }

        @Override
        public LiveData<State> getState() {
            return null;
        }
    };

    public TestWorkManager() {
        super();
    }

    @Override
    public Operation enqueue(List<? extends WorkRequest> requests) {
        return DUMMY_OPERATION;
    }

    @Override
    public Operation enqueueUniqueWork(String uniqueWorkName, ExistingWorkPolicy existingWorkPolicy, List<OneTimeWorkRequest> work) {
        return DUMMY_OPERATION;
    }

    @Override
    public Operation enqueueUniquePeriodicWork(String uniqueWorkName, ExistingPeriodicWorkPolicy existingPeriodicWorkPolicy, PeriodicWorkRequest repeatingWork) {
        return DUMMY_OPERATION;
    }

    @Override
    public WorkContinuation beginWith(List<OneTimeWorkRequest> work) {
        return null;
    }

    @Override
    public WorkContinuation beginUniqueWork(String uniqueWorkName, ExistingWorkPolicy existingWorkPolicy, List<OneTimeWorkRequest> work) {
        return null;
    }

    @Override
    public Operation cancelWorkById(UUID id) {
        return DUMMY_OPERATION;
    }

    @Override
    public Operation cancelAllWorkByTag(String tag) {
        return DUMMY_OPERATION;
    }

    @Override
    public Operation cancelUniqueWork(String uniqueWorkName) {
        return DUMMY_OPERATION;
    }

    @Override
    public Operation cancelAllWork() {
        return DUMMY_OPERATION;
    }

    @Override
    public PendingIntent createCancelPendingIntent(UUID id) {
        return null;
    }

    @Override
    public ListenableFuture<Long> getLastCancelAllTimeMillis() {
        return null;
    }

    @Override
    public LiveData<Long> getLastCancelAllTimeMillisLiveData() {
        return null;
    }

    @Override
    public ListenableFuture<WorkInfo> getWorkInfoById(UUID id) {
        return null;
    }

    @Override
    public LiveData<WorkInfo> getWorkInfoByIdLiveData(UUID id) {
        return null;
    }

    @Override
    public Flow<WorkInfo> getWorkInfoByIdFlow(UUID id) {
        return null;
    }

    @Override
    public ListenableFuture<List<WorkInfo>> getWorkInfosByTag(String tag) {
        return null;
    }

    @Override
    public LiveData<List<WorkInfo>> getWorkInfosByTagLiveData(String tag) {
        return null;
    }

    @Override
    public Flow<List<WorkInfo>> getWorkInfosByTagFlow(String tag) {
        return null;
    }

    @Override
    public ListenableFuture<List<WorkInfo>> getWorkInfosForUniqueWork(String uniqueWorkName) {
        return null;
    }

    @Override
    public LiveData<List<WorkInfo>> getWorkInfosForUniqueWorkLiveData(String uniqueWorkName) {
        return null;
    }

    @Override
    public Flow<List<WorkInfo>> getWorkInfosForUniqueWorkFlow(String uniqueWorkName) {
        return null;
    }

    @Override
    public ListenableFuture<List<WorkInfo>> getWorkInfos(WorkQuery workQuery) {
        return null;
    }

    @Override
    public LiveData<List<WorkInfo>> getWorkInfosLiveData(WorkQuery workQuery) {
        return null;
    }

    @Override
    public Flow<List<WorkInfo>> getWorkInfosFlow(WorkQuery workQuery) {
        return null;
    }

    @Override
    public ListenableFuture<UpdateResult> updateWork(WorkRequest request) {
        return null;
    }

    @Override
    public Operation pruneWork() {
        return DUMMY_OPERATION;
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }
}
