package com.t3ddyss.clother.data.offers.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.t3ddyss.clother.data.common.common.db.AppDatabase
import com.t3ddyss.clother.data.offers.db.models.CategoryEntity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@MediumTest
class CategoryDaoTest {

    private val hiltAndroidRule = HiltAndroidRule(this)
    private val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var db: AppDatabase
    @Inject
    lateinit var dao: CategoryDao

    @get:Rule
    val rule: RuleChain = RuleChain
        .outerRule(hiltAndroidRule)
        .around(instantTaskExecutorRule)

    @Before
    fun setUp() = runTest {
        hiltAndroidRule.inject()
        mockCategories()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun rootCategories_shouldBeMenAndWomen() = runTest {
        val categories = dao.getCategories(null)
            .map { it.category.title }
        assertThat(categories).containsExactlyElementsIn(arrayOf("Men", "Women"))
    }

    @Test
    fun menSubcategories_shouldBeShirts() = runTest {
        val categories = dao.getCategories(2)
            .map { it.category.title }
        assertThat(categories).containsExactlyElementsIn(arrayOf("Shirts"))
    }

    @Test
    fun casualSubcategories_shouldBeNone() = runTest {
        val categories = dao.getCategories(5)
            .map { it.category.title }
        assertThat(categories).isEmpty()
    }

    private suspend fun mockCategories() {
        val categories = buildList {
            add(
                CategoryEntity(
                    id = 1,
                    parentId = null,
                    title = "Women",
                )
            )

            add(
                CategoryEntity(
                    id = 2,
                    parentId = null,
                    title = "Men",
                )
            )

            add(
                CategoryEntity(
                    id = 3,
                    parentId = 2,
                    title = "Shirts",
                )
            )

            add(
                CategoryEntity(
                    id = 4,
                    parentId = 3,
                    title = "Casual",
                )
            )

            add(
                CategoryEntity(
                    id = 5,
                    parentId = 3,
                    title = "Dressed",
                )
            )
        }

        dao.insertAll(categories)
    }
}