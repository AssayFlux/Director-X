package com.directorx.editor.engine

import com.directorx.editor.engine.AppLog
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

private const val MEMORY_TRIM_DISPATCHER_TAG = "MemoryTrimDispatcher"

private object MemoryTrimBreadcrumbIo {
    val executor: Executor = Executors.newSingleThreadExecutor { task ->
        Thread(task, "DirectorX-MemoryTrimBreadcrumbs").apply { isDaemon = true }
    }
}

data class MemoryTrimDispatch(
    val decision: MemoryTrimDecision,
    val results: List<MemoryTrimRegistry.DispatchResult>,
)

@Singleton
class MemoryTrimDispatcher internal constructor(
    private val policy: MemoryTrimPolicy,
    private val registry: MemoryTrimRegistry,
    private val breadcrumbStore: MemoryTrimBreadcrumbStore,
    private val breadcrumbExecutor: Executor,
) {

    @Inject
    constructor(
        policy: MemoryTrimPolicy,
        registry: MemoryTrimRegistry,
        breadcrumbStore: MemoryTrimBreadcrumbStore,
    ) : this(policy, registry, breadcrumbStore, MemoryTrimBreadcrumbIo.executor)

    fun onTrimMemory(level: Int): MemoryTrimDispatch {
        val decision = policy.decisionFor(level)
        // Cache reclamation remains synchronous so the OS gets memory back during
        // the trim callback. File writes/reads/pruning are deferred off the main
        // thread; the old synchronous record() path triggered StrictMode disk I/O.
        val results = registry.dispatch(decision.actions)
        if (decision.logBreadcrumb) {
            runCatching {
                breadcrumbExecutor.execute {
                    runCatching {
                        breadcrumbStore.record(decision, results)
                    }.onFailure { t ->
                        AppLog.w(
                            MEMORY_TRIM_DISPATCHER_TAG,
                            "Unable to write memory trim breadcrumb",
                            t,
                        )
                    }
                }
            }.onFailure { t ->
                AppLog.w(
                    MEMORY_TRIM_DISPATCHER_TAG,
                    "Unable to schedule memory trim breadcrumb",
                    t,
                )
            }
        }
        return MemoryTrimDispatch(decision, results)
    }
}
