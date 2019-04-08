package com.igorvd.chuckfacts.domain.jokes.interactor

import com.igorvd.chuckfacts.domain.jokes.repository.JokeRepository
import com.igorvd.chuckfacts.domain.utils.DUMMY_JOKES
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RetrieveJokesInteractorTest {

    private lateinit var retrieveJokesInteractor: RetrieveJokesInteractor

    @MockK
    private lateinit var repository: JokeRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        retrieveJokesInteractor = RetrieveJokesInteractor(repository)
    }

    @Test
    fun `should retrieve jokes`() = runBlocking {

        coEvery { repository.retrieveJokes("infinite") } returns DUMMY_JOKES

        val jokes = retrieveJokesInteractor.execute(RetrieveJokesInteractor.Params("infinite"))

        coVerify (exactly = 1) { repository.retrieveJokes("infinite") }
    }



}