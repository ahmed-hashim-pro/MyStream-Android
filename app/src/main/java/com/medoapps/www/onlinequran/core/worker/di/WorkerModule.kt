package com.medoapps.www.onlinequran.core.worker.di

import com.medoapps.www.onlinequran.core.worker.WorkerKey
import com.medoapps.www.onlinequran.core.worker.WorkerTaskFactory
import com.medoapps.www.onlinequran.worker.AudioUpdateWorker
import com.medoapps.www.onlinequran.worker.MissingPageDownloadWorker
import com.medoapps.www.onlinequran.worker.PartialPageCheckingWorker
import com.medoapps.www.onlinequran.worker.PartialPageCheckingWorker.Factory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module(includes = [ AudioUpdateModule::class ])
abstract class WorkerModule {

  @Binds
  @IntoMap
  @WorkerKey(PartialPageCheckingWorker::class)
  abstract fun bindPartialPageCheckingWorkerFactory(
    workerFactory: Factory
  ): WorkerTaskFactory

  @Binds
  @IntoMap
  @WorkerKey(MissingPageDownloadWorker::class)
  abstract fun bindMissingPageDownloadWorkerFactory(
    workerFactory: MissingPageDownloadWorker.Factory
  ): WorkerTaskFactory

  @Binds
  @IntoMap
  @WorkerKey(AudioUpdateWorker::class)
  abstract fun bindAudioUpdateWorkerFactory(
    workerFactory: AudioUpdateWorker.Factory
  ): WorkerTaskFactory
}
