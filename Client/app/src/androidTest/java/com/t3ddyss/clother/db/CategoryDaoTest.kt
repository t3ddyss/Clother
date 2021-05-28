package com.t3ddyss.clother.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class CategoryDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: CategoryDao

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        db = Room
            .databaseBuilder(ApplicationProvider.getApplicationContext(),
                AppDatabase::class.java,
            "ClotherTestDatabase")
            .createFromAsset("clother_category.db")
            .allowMainThreadQueries().build()
        dao = db.categoryDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun getCategoriesCount() = runBlockingTest {
        assertThat(dao.getCategoriesCount() > 10).isTrue()
    }
}