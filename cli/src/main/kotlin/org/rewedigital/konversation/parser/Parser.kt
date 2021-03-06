package org.rewedigital.konversation.parser

import org.rewedigital.konversation.*
import java.io.File
import java.util.*

class Parser(input: File) {
    constructor(input: String) : this(File(input).absoluteFile)

    val intents: List<Intent>
    val entities: Entities?
    private var lastIntent: Intent? = null

    init {
        when {
            input.name.endsWith(".values") -> {
                intents = emptyList()
                entities = parseValuesFile(input)
            }
            input.name.endsWith(".grammar") ||
                    input.name.endsWith(".kvs") -> {
                intents = parseInputFile(input)
                entities = null
            }
            else -> throw IllegalArgumentException("Unknown file type. Cannot parse ${input.name}")
        }
    }

    private fun parseInputFile(input: File): List<Intent> {
        val isGrammarFile = input.name.endsWith(".grammar")

        val lines = input.readLines()
        var lastPart: Part? = null
        var lastIntentName = UUID.randomUUID().toString()
        val intents = mutableListOf<Intent>()
        val nextIntentAnnotations: MutableMap<String, List<String>> = mutableMapOf()
        lines.filter { it.isNotBlank() }.forEachIndexed { index, rawLine ->
            val line = rawLine.trimComments()
            when {
                line.isBlank() -> {
                    // ignore comments and blank lines
                }
                line == "+" -> addTo {
                    // just let the block end
                    lastPart = null
                    //prompt.parts.add(PartImpl(type = PartType.Text, variants = mutableListOf(" ")))
                }
                line == "-" -> addTo {
                    // add a line break
                    lastPart = null
                    prompt.add(PartImpl(type = PartType.Text, variants = mutableListOf(" \n")))
                }
                line.startsWith("~") -> addTo {
                    // Voice only
                    val text = line.substring(1).trim()
                    if (lastPart?.type ?: PartType.Text == PartType.Text) {
                        lastPart = PartImpl(type = PartType.VoiceOnly, variants = mutableListOf())
                        prompt.add(lastPart!!)
                    }
                    lastPart?.variants?.addAll(Permutator.generate(text))
                }
                line.startsWith("-") -> addTo {
                    // variant
                    val text = line.substring(1).trim()
                    if (lastPart?.type ?: PartType.VoiceOnly == PartType.VoiceOnly) {
                        lastPart = PartImpl(type = PartType.Text, variants = mutableListOf())
                        prompt.add(lastPart!!)
                    }
                    lastPart?.variants?.addAll(Permutator.generate(text))
                }
                line.startsWith("!") -> addTo {
                    addUtterance(this, line.substring(1).trimStart())
                }
                line.startsWith("?") -> addTo {
                    // reprompt
                    val level = line.substring(1, line.indexOf(" ")).toIntOrNull() ?: 1
                    val text = line.substring(line.indexOf(" "))
                    val prompt = reprompt.getOrPut(level) { mutableListOf(PartImpl(type = PartType.VoiceOnly, variants = mutableListOf())) }
                    prompt.first().variants.addAll(Permutator.generate(text))
                }
                line.startsWith("[") && line.endsWith("]") -> addTo {
                    // suggestions
                    suggestions.addAll(line.substring(1, line.length - 1).split("]\\W*\\[".toRegex()))
                }
                line.startsWith("@") -> {
                    if (line.contains('(') && !line.endsWith(')')) {
                        // FIXME the intent name is wrong, because the intent name will follow
                        throw KonversationSyntaxError(rawLine, lastIntentName, index, "Missing closing bracket", input.name)
                    }
                    if (!line.contains('(') && line.endsWith(')')) {
                        throw KonversationSyntaxError(rawLine, lastIntentName, index, "Missing opening bracket", input.name)
                    }
                    val (annotation, rawValues) = (line.substring(1).trimEnd(')') + "(").split('(')
                    nextIntentAnnotations += annotation to rawValues.split(',').map { it.trim(' ', '"', '\'') }.filter { it.isNotEmpty() }
                }
                line.startsWith(">") -> addTo {
                    inContext.add(line.substring(1).trim())
                }
                line.startsWith("<") -> addTo {
                    outContext.add(line.substring(1).trim())
                }
                line.startsWith("&") -> addTo {
                    // TODO handle
                }
                line.contains('=') && line.startsWith(lastIntentName) -> {
                    lastPart = null
                    if (intents.find { it.name.equals(line, true) } != null) {
                        printErr("Intent \"$line\" already defined. Appending new parts. You have been warned.")
                    } else {
                        lastIntent = Intent(line).also {
                            intents.add(it)
                        }
                    }
                }
                line.endsWith(":") -> { // intent found
                    lastPart = null
                    lastIntentName = line.substring(0, line.length - 1)
                    val knownIntent = intents.find { it.name.equals(lastIntentName, true) }
                    if (knownIntent != null) {
                        printErr("Intent \"${lastIntent?.name}\" already defined. Appending new parts. You have been warned.")
                        knownIntent.annotations += nextIntentAnnotations
                    } else {
                        lastIntent = Intent(lastIntentName, annotations = nextIntentAnnotations.toMutableMap()).also {
                            intents.add(it)
                        }
                    }
                    nextIntentAnnotations.clear()
                }
                else -> addTo {
                    if (isGrammarFile) {
                        addUtterance(this, line)
                    } else {
                        throw KonversationSyntaxError(rawLine, lastIntentName, index, "This line has no prefix", input.name)
                    }
                }
            }
        }
        return intents
    }

    private fun parseValuesFile(input: File) = input
        .readLines()
        .map { valueLine ->
            if (valueLine.startsWith('{')) {
                val aliases = valueLine.substring(1, valueLine.length - 1).split('|')
                val (key, master) = aliases.first().split(':', limit = 2).let {
                    when (it.size) {
                        1 -> Pair(null, it.first())
                        2 -> Pair(it.first(), it.last())
                        else -> throw IllegalArgumentException("The key must not be empty. In the line: $valueLine")
                    }
                }
                Entity(master = master, key = key, synonyms = aliases.drop(1))
            } else {
                Entity(master = valueLine, key = null, synonyms = emptyList())
            }
        }.let {
            Entities(input.name.dropLast(7), it)
        }

    private fun addUtterance(intent: Intent, utterance: String) {
        intent.utterances.add(Utterance(utterance, intent.name))
    }

    private fun printErr(errorMsg: String) =
        Cli.L.error(errorMsg)

    private fun addTo(block: Intent.() -> Unit) = lastIntent?.let(block::invoke) ?: printErr("No intent defined.")

    private fun String.trimComments() = substringBefore("//").substringBefore("#").trim()
}