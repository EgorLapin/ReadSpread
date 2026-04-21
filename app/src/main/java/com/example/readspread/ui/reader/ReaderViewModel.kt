package com.example.readspread.ui.reader

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import data.local.domain.repository.BookRepository
import data.local.entity.Book
import data.local.entity.BookFormat
import data.local.entity.BookStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val repository: BookRepository   // ← kept for future use, but ignored in stub
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val book: Book, val content: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val bookIdFlow = MutableStateFlow(0L)

    val uiState: StateFlow<UiState> = bookIdFlow
        .flatMapLatest { id ->
            Log.d("READER_VM", "STUB: Requested book with ID = $id")
            // --- TEMPORARY STUB ---
            // Return a fake book with sample content instead of calling repository
            val stubBook = createStubBook(id)
            val stubContent = createStubContent()
            flowOf(stubBook to stubContent)
            // --- END STUB ---
            // Original repository call (commented out):
            // repository.getBookById(id).map { book -> book to "" }
        }
        .map { (book, content) ->
            if (book != null) {
                Log.d("READER_VM", "STUB: Returning example book: ${book.title}")
                UiState.Success(book, content)
            } else {
                Log.d("READER_VM", "STUB: Book is null (should not happen with stub)")
                UiState.Error("Book not found (stub error)")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun setBookId(id: Long) {
        Log.d("READER_VM", "setBookId called: $id")
        bookIdFlow.value = id
    }

    // ---------- STUB HELPER FUNCTIONS ----------
    private fun createStubBook(id: Long): Book {
        return Book(
            id = id.takeIf { it > 0 } ?: 1L,
            title = "The Adventures of Sherlock Holmes",
            author = "Arthur Conan Doyle",
            isbn = "978-0-14-043771-8",
            publisher = "Penguin Classics",
            publishedDate = "1892-10-14",
            description = "A collection of twelve short stories featuring the famous detective Sherlock Holmes and his loyal friend Dr. Watson.",
            filePath = "/storage/emulated/0/Books/sherlock_holmes.txt",
            format = BookFormat.TXT,
            coverPath = null,
            fileSize = 512000L,
            totalPages = 240,
            currentPage = 1,
            progress = 0f,
            status = BookStatus.NOT_STARTED,
            isFavorite = true,
            fileHash = "abc123def456",
            addedAt = System.currentTimeMillis() - 86400000, // yesterday
            lastReadAt = null,
            completedAt = null,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun createStubContent(): String {
        return """
            CHAPTER I. MR. SHERLOCK HOLMES

            In the year 1878 I took my degree of Doctor of Medicine of the University of London, and proceeded to Netley to go through the course prescribed for surgeons in the army. Having completed my studies there, I was duly attached to the Fifth Northumberland Fusiliers as Assistant Surgeon. The regiment was stationed in India at the time, and before I could join it, the second Afghan war had broken out. On landing at Bombay, I learned that my corps had advanced through the passes, and was already deep in the enemy's country.

            I followed, however, with many other officers who were in the same situation as myself, and succeeded in reaching Candahar in safety, where I found my regiment, and at once entered upon my new duties. The campaign brought honours and promotion to many, but for me it had nothing but misfortune and disaster. I was removed from my brigade and attached to the Berkshires, with whom I served at the fatal battle of Maiwand. There I was struck on the shoulder by a Jezail bullet, which shattered the bone and grazed the subclavian artery. I should have fallen into the hands of the murderous Ghazis had it not been for the devotion and courage shown by Murray, my orderly, who threw me across a pack-horse, and succeeded in bringing me safely to the British lines.

            Worn with pain, and weak from the prolonged hardships which I had undergone, I was removed, with a great train of wounded sufferers, to the base hospital at Peshawar. Here I rallied, and had already improved so far as to be able to walk about the wards, and even to bask a little upon the verandah, when I was struck down by enteric fever, that curse of our Indian possessions. For months my life was despaired of, and when at last I came to myself and became convalescent, I was so weak and emaciated that a medical board determined that not a day should be lost in sending me back to England. I was dispatched, accordingly, in the troopship "Orontes," and landed a month later on Portsmouth jetty, with my health irretrievably ruined, but with permission from a paternal government to spend the next nine months in attempting to improve it.

            I had neither kith nor kin in England, and was therefore as free as air—or as free as an income of eleven shillings and sixpence a day will permit a man to be. Under such circumstances, I naturally gravitated to London, that great cesspool into which all the loungers and idlers of the Empire are irresistibly drained. There I stayed for some time at a private hotel in the Strand, leading a comfortless, meaningless existence, and spending such money as I had, considerably more freely than I ought. So alarming did the state of my finances become, that I soon realized that I must either leave the metropolis and rusticate somewhere in the country, or that I must make a complete alteration in my style of living. Choosing the latter alternative, I began by making up my mind to leave the hotel, and to take up my quarters in some less pretentious and less expensive domicile.

            On the very day that I had come to this conclusion, I was standing at the Criterion Bar, when some one tapped me on the shoulder, and turning round I recognized young Stamford, who had been a dresser under me at Barts. The sight of a friendly face in the great wilderness of London is a pleasant thing indeed to a lonely man. In old days Stamford had never been a particular crony of mine, but now I hailed him with enthusiasm, and he, in his turn, appeared to be delighted to see me. In the exuberance of my joy, I asked him to lunch with me at the Holborn, and we started off together in a hansom.

            "Whatever have you been doing with yourself, Watson?" he asked in undisguised wonder, as we rattled through the crowded London streets. "You are as thin as a lath and as brown as a nut."

            I gave him a short sketch of my adventures, and had hardly concluded it by the time that we reached our destination.

            "Poor devil!" he said, commiseratingly, after he had listened to my misfortunes. "What are you up to now?"

            "Looking for lodgings," I answered. "Trying to solve the problem as to whether it is possible to get comfortable rooms at a reasonable price."

            "That's a strange thing," remarked my companion; "you are the second man to-day that has used that expression to me."

            "And who was the first?" I asked.

            "A fellow who is working at the chemical laboratory up at the hospital. He was bemoaning himself this morning because he could not get someone to go halves with him in some nice rooms which he had found, and which were too much for his purse."

            "By Jove!" I cried; "if he really wants someone to share the rooms and the expense, I am the very man for him. I should prefer having a partner to being alone."

            Young Stamford looked rather strangely at me over his wine-glass. "You don't know Sherlock Holmes yet," he said; "perhaps you would not care for him as a constant companion."

            "Why, what is there against him?"

            "Oh, I didn't say there was anything against him. He is a little queer in his ideas—an enthusiast in some branches of science. As far as I know he is a decent fellow enough."
        """.trimIndent()
    }
}