package com.eliranabu.falafelrush

import com.eliranabu.falafelrush.data.reviews.LocalReviewGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalReviewGeneratorTest {

    @Test
    fun `perfect day yields three five-star reviews`() {
        val reviews = LocalReviewGenerator.generateReviews(
            day = 3, servedCount = 12, errorCount = 0, leftCount = 0, coinsEarned = 250
        )
        assertEquals(3, reviews.size)
        assertTrue(reviews.all { it.rating == 5 })
        assertTrue(reviews.all { it.dayNumber == 3 })
    }

    @Test
    fun `disaster day yields low ratings`() {
        val reviews = LocalReviewGenerator.generateReviews(
            day = 1, servedCount = 1, errorCount = 4, leftCount = 5, coinsEarned = 10
        )
        assertEquals(3, reviews.size)
        assertTrue(reviews.all { it.rating <= 2 })
    }

    @Test
    fun `placeholders are fully substituted`() {
        repeat(20) {
            val reviews = LocalReviewGenerator.generateReviews(
                day = 2, servedCount = 7, errorCount = 1, leftCount = 1, coinsEarned = 120
            )
            reviews.forEach { review ->
                assertFalse(review.comment.contains("{served}"))
                assertFalse(review.comment.contains("{coins}"))
                assertFalse(review.comment.contains("{left}"))
                assertFalse(review.comment.contains("{errors}"))
            }
        }
    }

    @Test
    fun `reviewer names are unique within a batch`() {
        val reviews = LocalReviewGenerator.generateReviews(
            day = 4, servedCount = 8, errorCount = 1, leftCount = 0, coinsEarned = 180
        )
        assertEquals(3, reviews.map { it.customerName }.distinct().size)
    }
}
