package io.github.cavaleirobrancopensante.filterout

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch

class MainActivity : AppCompatActivity() {

    private lateinit var store: WordStore
    private lateinit var statusText: TextView
    private lateinit var statusButton: Button
    private lateinit var wordList: LinearLayout
    private lateinit var emptyText: TextView
    private lateinit var countText: TextView
    private lateinit var showWordsButton: Button
    private var revealWords = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        store = WordStore(this)

        statusText = findViewById(R.id.status_text)
        statusButton = findViewById(R.id.status_button)
        wordList = findViewById(R.id.word_list)
        emptyText = findViewById(R.id.empty_text)
        countText = findViewById(R.id.count_text)
        showWordsButton = findViewById(R.id.show_words_button)

        statusButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        val ocrSwitch = findViewById<MaterialSwitch>(R.id.ocr_switch)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ocrSwitch.isChecked = store.ocrEnabled
            ocrSwitch.setOnCheckedChangeListener { _, on -> store.ocrEnabled = on }
        } else {
            ocrSwitch.isChecked = false
            ocrSwitch.isEnabled = false
            findViewById<TextView>(R.id.ocr_hint).setText(R.string.ocr_unsupported)
        }

        val input = findViewById<EditText>(R.id.word_input)
        val add = {
            val word = input.text.toString()
            if (store.add(word)) {
                input.text.clear()
                renderWords()
            } else if (word.isNotBlank()) {
                Toast.makeText(this, R.string.already_added, Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.add_button).setOnClickListener { add() }
        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) { add(); true } else false
        }

        showWordsButton.setOnClickListener {
            revealWords = !revealWords
            renderWords()
        }
    }

    override fun onResume() {
        super.onResume()
        renderStatus()
        renderWords()
    }

    private fun renderStatus() {
        if (isServiceEnabled()) {
            statusText.setText(R.string.status_on)
            statusText.setTextColor(getColor(R.color.status_on))
            statusButton.setText(R.string.open_settings)
        } else {
            statusText.setText(R.string.status_off)
            statusText.setTextColor(getColor(R.color.status_off))
            statusButton.setText(R.string.turn_on)
        }
    }

    private fun renderWords() {
        val words = store.words.sorted()
        wordList.removeAllViews()
        emptyText.visibility = if (words.isEmpty()) View.VISIBLE else View.GONE
        showWordsButton.visibility = if (words.isEmpty()) View.GONE else View.VISIBLE
        showWordsButton.setText(if (revealWords) R.string.hide_words else R.string.show_words)
        countText.text = resources.getQuantityString(R.plurals.word_count, words.size, words.size)

        val inflater = LayoutInflater.from(this)
        for (word in words) {
            val row = inflater.inflate(R.layout.item_word, wordList, false)
            row.findViewById<TextView>(R.id.word_text).text = if (revealWords) word else mask(word)
            row.findViewById<ImageButton>(R.id.remove_button).setOnClickListener {
                store.remove(word)
                renderWords()
            }
            wordList.addView(row)
        }
    }

    /** Keep the list from showing the words themselves unless asked. */
    private fun mask(word: String): String =
        word.first() + "•".repeat((word.length - 1).coerceIn(2, 12))

    private fun isServiceEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val me = ComponentName(this, FilterService::class.java)
        val splitter = TextUtils.SimpleStringSplitter(':').apply { setString(enabled) }
        return splitter.any { ComponentName.unflattenFromString(it) == me }
    }
}
