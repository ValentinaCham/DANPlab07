package com.example.danp_lab07.di

import android.content.Context
import com.example.danp_lab07.data.local.ProductDao
import com.example.danp_lab07.data.local.ProductDatabase
import com.example.danp_lab07.repository.ProductRepository
import com.example.danp_lab07.repository.ProductRepositoryImpl
import com.example.danp_lab07.repository.FakeProductRepository
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

    companion object {
        @Provides
        @Singleton
        fun provideProductDatabase(@ApplicationContext context: Context): ProductDatabase {
            return Room.databaseBuilder(
                context,
                ProductDatabase::class.java,
                "product_db"
            ).build()
        }

        @Provides
        @Singleton
        fun provideProductDao(database: ProductDatabase): ProductDao {
            return database.productDao()
        }
    }
}
