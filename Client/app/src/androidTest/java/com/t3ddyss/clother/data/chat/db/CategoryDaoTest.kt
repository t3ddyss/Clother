package com.t3ddyss.clother.data.chat.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.t3ddyss.clother.data.common.db.AppDatabase
import com.t3ddyss.clother.data.offers.db.CategoryDao
import com.t3ddyss.clother.data.offers.db.models.CategoryEntity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runBlockingTest
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
    val rule = RuleChain
        .outerRule(hiltAndroidRule)
        .around(instantTaskExecutorRule)

    @Before
    fun setUp() = runBlockingTest {
        hiltAndroidRule.inject()
        mockCategories()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun categoriesCount_shouldBe5() = runBlockingTest {
        assertThat(dao.getCategoriesCount()).isEqualTo(5)
    }

    @Test
    fun rootCategories_shouldBeMenAndWomen() = runBlockingTest {
        val categories = dao.getSubcategories(null)
            .map {
                it.title
            }
        assertThat(categories).containsExactlyElementsIn(arrayOf("Men", "Women"))
    }

    @Test
    fun menSubcategories_shouldBeShirts() = runBlockingTest {
        val categories = dao.getSubcategories(2)
            .map {
                it.title
            }
        assertThat(categories).containsExactlyElementsIn(arrayOf("Shirts"))
    }

    @Test
    fun casualSubcategories_shouldBeNone() = runBlockingTest {
        val categories = dao.getSubcategories(5)
            .map {
                it.title
            }
        assertThat(categories).isEmpty()
    }

    private suspend fun mockCategories() {
        val categories = mutableListOf<CategoryEntity>()

        categories.add(
            CategoryEntity(
                id = 1,
                parentId = null,
                title = "Women",
                isLastLevel = false
            )
        )

        categories.add(
            CategoryEntity(
                id = 2,
                parentId = null,
                title = "Men",
                isLastLevel = false
            )
        )

        categories.add(
            CategoryEntity(
                id = 3,
                parentId = 2,
                title = "Shirts",
                isLastLevel = false
            )
        )

        categories.add(
            CategoryEntity(
                id = 4,
                parentId = 3,
                title = "Casual",
                isLastLevel = true
            )
        )

        categories.add(
            CategoryEntity(
                id = 5,
                parentId = 3,
                title = "Dressed",
                isLastLevel = true
            )
        )

        dao.insertAll(categories)
    }
}