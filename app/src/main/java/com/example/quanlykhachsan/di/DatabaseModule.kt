package com.example.quanlykhachsan.di

import android.content.Context
import androidx.room.Room
import com.example.quanlykhachsan.data.local.database.AppDatabase
import com.example.quanlykhachsan.data.local.dao.PhongDao
import com.example.quanlykhachsan.data.local.dao.LoaiPhongDao
import com.example.quanlykhachsan.data.local.dao.DatPhongDao
import com.example.quanlykhachsan.data.local.dao.NhanVienDao
import com.example.quanlykhachsan.data.local.dao.TraPhongDao
import com.example.quanlykhachsan.data.repository.chart.ChartRepository
import com.example.quanlykhachsan.data.repository.chart.ChartRepositoryImpl
import com.example.quanlykhachsan.data.repository.loaiphong.LoaiPhongRepository
import com.example.quanlykhachsan.data.repository.loaiphong.LoaiPhongRepositoryImpl
import com.example.quanlykhachsan.data.repository.phong.PhongRepository
import com.example.quanlykhachsan.data.repository.phong.PhongRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = AppDatabase.getDatabase(context)

    @Provides fun providePhongDao(db: AppDatabase): PhongDao = db.phongDao()
    @Provides fun provideLoaiPhongDao(db: AppDatabase): LoaiPhongDao = db.loaiPhongDao()
    @Provides fun provideNhanVienDao(db: AppDatabase): NhanVienDao = db.nhanVienDao()
    @Provides fun provideDatPhongDao(db: AppDatabase): DatPhongDao = db.datPhongDao()
    @Provides fun provideTraPhongDao(db: AppDatabase): TraPhongDao = db.traPhongDao()
    @Provides fun provideChartRepository(dao: TraPhongDao): ChartRepository = ChartRepositoryImpl(dao)

    @Provides @Singleton fun providePhongRepository(dao: PhongDao): PhongRepository = PhongRepositoryImpl(dao)
    @Provides @Singleton fun provideLoaiPhongRepository(dao: LoaiPhongDao):LoaiPhongRepository = LoaiPhongRepositoryImpl(dao)
}
