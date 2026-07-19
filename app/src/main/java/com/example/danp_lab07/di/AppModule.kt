package com.example.danp_lab07.di

import android.content.Context
import android.net.ConnectivityManager
import com.example.danp_lab07.data.local.ProductDao
import com.example.danp_lab07.data.local.ProductDatabase
import com.example.danp_lab07.data.remote.FirebaseImageStorageDataSource
import com.example.danp_lab07.data.remote.FirestoreProductDataSource
import com.example.danp_lab07.data.remote.ImageStorageDataSource
import com.example.danp_lab07.data.remote.ProductRemoteDataSource
import com.example.danp_lab07.data.remote.StorageUrlResolver
import com.example.danp_lab07.repository.ImageRepository
import com.example.danp_lab07.repository.ImageRepositoryImpl
import com.example.danp_lab07.repository.ProductRepository
import com.example.danp_lab07.repository.ProductRepositoryImpl
import com.example.danp_lab07.repository.ProductSyncScheduler
import com.example.danp_lab07.worker.WorkManagerProductSyncScheduler
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import androidx.room.Room
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindProductRemoteDataSource(
        firestoreProductDataSource: FirestoreProductDataSource,
    ): ProductRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindProductSyncScheduler(
        workManagerProductSyncScheduler: WorkManagerProductSyncScheduler,
    ): ProductSyncScheduler

    @Binds
    @Singleton
    abstract fun bindImageStorageDataSource(
        firebaseImageStorageDataSource: FirebaseImageStorageDataSource,
    ): ImageStorageDataSource

    @Binds
    @Singleton
    abstract fun bindImageRepository(
        imageRepositoryImpl: ImageRepositoryImpl,
    ): ImageRepository

    companion object {
        @Provides
        @Singleton
        fun provideProductDatabase(@ApplicationContext context: Context): ProductDatabase {
            return Room.databaseBuilder(
                context,
                ProductDatabase::class.java,
                "product_db"
            )
                .addMigrations(ProductDatabase.MIGRATION_1_2, ProductDatabase.MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
        }

        @Provides
        @Singleton
        fun provideProductDao(database: ProductDatabase): ProductDao {
            return database.productDao()
        }

        @Provides
        @Singleton
        fun provideConnectivityManager(
            @ApplicationContext context: Context,
        ): ConnectivityManager {
            return context.getSystemService(ConnectivityManager::class.java)
        }

        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore {
            return FirebaseFirestore.getInstance()
        }

        @Provides
        @Singleton
        fun provideFirebaseStorage(): FirebaseStorage {
            return FirebaseStorage.getInstance()
        }
    }
}
