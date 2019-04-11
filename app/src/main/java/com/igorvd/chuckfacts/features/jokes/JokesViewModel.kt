package com.igorvd.chuckfacts.features.jokes

import com.igorvd.chuckfacts.domain.exceptions.MyHttpErrorException
import com.igorvd.chuckfacts.domain.exceptions.MyIOException
import com.igorvd.chuckfacts.domain.jokes.entity.Joke
import com.igorvd.chuckfacts.domain.jokes.interactor.RetrieveJokesInteractor
import com.igorvd.chuckfacts.domain.jokes.interactor.RetrieveRandomJokesInteractor
import com.igorvd.chuckfacts.features.*
import com.igorvd.chuckfacts.utils.extensions.throwOrLog
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import java.lang.Exception
import javax.inject.Inject

@FlowPreview
class JokesViewModel @Inject constructor(
    private val retrieveJokesInteractor: RetrieveJokesInteractor,
    private val retrieveRandomJokesInteractor: RetrieveRandomJokesInteractor
) : BaseViewModel() {

    var lastQuery: String? = null

    suspend fun retrieveJokes(query: String) {
        lastQuery = query
        val currentJokes = mutableListOf<Joke>()
        collectJokes(query, currentJokes)
    }

    private suspend fun collectJokes(query: String, currentJokes: MutableList<Joke>) {
        try {
            _showProgressEvent.call()

            val params = RetrieveJokesInteractor.Params(query)
            val jokesFlow = retrieveJokesInteractor.execute(params)

            jokesFlow.collect { jokes ->
                currentJokes.addAll(0, jokes)
                val state = if (currentJokes.isEmpty()) {
                    EmptyResult
                } else {
                    _hideProgressEvent.call()
                    JokeScreenState.Result(currentJokes.toJokesView())
                }
                _screenState.value = state
            }

        } catch (e: Exception) {
            if (currentJokes.isEmpty()) handleProgressException(e)
        } finally {
            if (currentJokes.isEmpty()) _hideProgressEvent.call()
        }
    }

    suspend fun retrieveRandomJokes() {
        val params = RetrieveRandomJokesInteractor.Params(10)
        val randomJokes = retrieveRandomJokesInteractor.execute(params)
        _screenState.value = JokeScreenState.Result(randomJokes.toJokesView())
    }

    private fun List<Joke>.toJokesView() = this.map { JokesMapper.jokeToJokeView(it) }

    private fun handleProgressException(e: Exception) {
        when (e) {
            is MyIOException ->  _screenState.value = NetworkError
            is MyHttpErrorException -> _screenState.value = HttpError
            else -> e.throwOrLog()
        }
    }
}