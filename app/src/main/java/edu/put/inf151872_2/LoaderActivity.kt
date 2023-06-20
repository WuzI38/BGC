package edu.put.inf151872_2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileWriter
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking

class LoaderActivity : AppCompatActivity() {
    private var idList = mutableListOf<String>()
    private var sync : Boolean = false
    private var username : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loader)

        username = intent.getStringExtra("Username")
        val sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE)

        if(username == null) {
            sync = true
            username = sharedPref.getString("Username", null)
            if (username == null) {
                val logIntent = Intent(this, LogActivity::class.java)
                logIntent.putExtra("Error", "Wrong username")
                startActivity(logIntent)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val urlStr = "https://boardgamegeek.com/xmlapi2/collection?username=$username"
        val filename = "games_owned.xml"

        var result = true
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                result = downloadXml(urlStr, filename)
            }
            job.join()
        }

        if (!result) {
            switchIntent("Cannot fetch user data")
        }

        idList.clear()

        if(!loadIds(filename)) switchIntent("Loading games' ids failed")
        if(idList.isEmpty()) switchIntent("No games in user's collection")

        runBlocking {
            try {
                val jobs: List<Job> = idList.map { gameId ->
                    launch(context = Dispatchers.Default) {
                        val gameUrl = "https://www.boardgamegeek.com/xmlapi2/thing?id=$gameId&stats=1"
                        val xmlGame = "$gameId.xml"

                        val resultGame = downloadXml(gameUrl, xmlGame)
                        if (!resultGame) {
                            throw Exception("Failed to download XML files")
                        }
                    }
                }
                jobs.joinAll()
            } catch (e: Exception) {
                switchIntent("Cannot download game files")
            }
        }

        if(sync) { // Data synchronization
            val idListCurrent: MutableList<String>? = DBHandler.getInstance(this)?.getIds() as MutableList<String>?
            if(idListCurrent != null) {
                val idCopy = idList.map { it }
                idList.removeAll(idListCurrent)
                idListCurrent.removeAll(idCopy)
                if(idListCurrent.isNotEmpty()) {
                    try {
                        DBHandler.getInstance(this)?.deleteRowsByIds(idListCurrent)
                    } catch (e: Exception) {
                        switchIntent("Cannot remove games from the database")
                    }
                }
            }
        }
        else { // New user
            DBHandler.getInstance(this)?.removeAllItems() // Clear all games if table not empty
        }

        for(item in idList) {
            val dbItem = addToDB("${item}.xml")
            if(!dbItem) {
                switchIntent("Cannot add data to the database")
            }
        }

        val sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("SyncDate", getCurrentDateAsString())
        editor.apply()

        if (!sync) {
            editor.putString("Username", username)
            editor.apply()
        }

        Log.d("MY_ERROR", "Everything works fine")

        removeXMLFiles()
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
    }

    private fun switchIntent(message: String) {
        val logIntent = Intent(this, LogActivity::class.java)
        val mainIntent = Intent(this, MainActivity::class.java)

        if(sync) {
            mainIntent.putExtra("Error", message)
            startActivity(mainIntent)
        }
        else {
            logIntent.putExtra("Error", message)
            startActivity(logIntent)
        }

    }

    private fun getCurrentDateAsString(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return currentDate.format(formatter)
    }

    private suspend fun downloadXml(urlStr: String, filename: String): Boolean = withContext(Dispatchers.IO) {
        val xmlDir = File("$filesDir/XML")
        if (!xmlDir.exists()) xmlDir.mkdir()
        val fileName = "$xmlDir/$filename"

        try {
            val url = URL(urlStr)
            val reader = url.openStream().bufferedReader()
            val downloadFile = File(fileName).also { it.createNewFile() }
            val writer = FileWriter(downloadFile).buffered()

            var line: String
            while (reader.readLine().also { line = it?.toString() ?: "" } != null) {
                writer.write(line)
            }

            reader.close()
            writer.close()

            return@withContext true
        } catch (e: Exception) {
            val incompleteFile = File(fileName)
            if (incompleteFile.exists()) incompleteFile.delete()
            return@withContext false
        }
    }

    @Suppress("SameParameterValue")
    private fun loadIds(filename: String) : Boolean {
        val path = filesDir
        val inDir = File(path,"XML")

        if (inDir.exists()) {
            val file = File(inDir, filename)
            if (file.exists()) {
                val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)

                xmlDoc.documentElement.normalize()

                val items: NodeList = xmlDoc.getElementsByTagName("item")

                for (i in 0 until items.length) {
                    val itemNode: Node = items.item(i)

                    if (itemNode.nodeType == Node.ELEMENT_NODE) {
                        val itemElement: Element = itemNode as Element
                        val objectId: String? = itemElement.getAttribute("objectid")

                        if (objectId != null) {
                            idList.add(objectId)
                        } else return false
                    }
                }
            }
            return true
        }
        return false
    }

    private fun addToDB(filename: String) : Boolean {
        val path = filesDir
        val inDir = File(path, "XML")

        if (inDir.exists()) {
            val file = File(inDir, filename)
            if (file.exists()) {
                val xmlDoc: Document
                try {
                    xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
                }
                catch (e: java.lang.Exception) {
                    return false
                }

                xmlDoc.documentElement.normalize()

                val itemNode = xmlDoc.getElementsByTagName("item").item(0) as Element

                val bggId = itemNode.getAttribute("id")?.toInt()
                val yearPublished = itemNode.getElementsByTagName("yearpublished").item(0)?.attributes?.getNamedItem("value")?.nodeValue?.toInt()
                val minPlayers = itemNode.getElementsByTagName("minplayers").item(0)?.attributes?.getNamedItem("value")?.nodeValue?.toInt()
                val maxPlayers = itemNode.getElementsByTagName("maxplayers").item(0)?.attributes?.getNamedItem("value")?.nodeValue?.toInt()
                val playingTime = itemNode.getElementsByTagName("playingtime").item(0)?.attributes?.getNamedItem("value")?.nodeValue?.toInt()
                val imageUrl = itemNode.getElementsByTagName("image").item(0)?.textContent
                val type = itemNode.getAttribute("type")
                val description = itemNode.getElementsByTagName("description").item(0)?.textContent
                val primaryName = xmlDoc.getElementsByTagName("name").item(0)?.attributes?.getNamedItem("value")?.nodeValue
                val bayesAverage = xmlDoc.getElementsByTagName("bayesaverage").item(0)?.attributes?.getNamedItem("value")?.nodeValue?.toFloat()
                val boardGameRank = xmlDoc.getElementsByTagName("rank").item(0)?.attributes?.getNamedItem("value")?.nodeValue

                val bggRank = boardGameRank?.toIntOrNull()
                val isExpansion = type != "boardgame"
                val cleanedDescription = Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY).toString()

                if (bggId != null &&
                    yearPublished != null &&
                    minPlayers != null &&
                    maxPlayers != null &&
                    playingTime != null &&
                    primaryName != null &&
                    bayesAverage != null &&
                    imageUrl != null) {
                    val boardGame = BoardGame(
                        bggId = bggId,
                        releaseYear = yearPublished,
                        minPlayers = minPlayers,
                        maxPlayers = maxPlayers,
                        playTime = playingTime,
                        bggRank = bggRank,
                        rating = bayesAverage,
                        isExpansion = isExpansion,
                        title = primaryName,
                        imageUrl = imageUrl,
                        description = cleanedDescription
                    )

                    try {
                        DBHandler.getInstance(this)?.addBoardGame(boardGame)
                    } catch (e: java.lang.Exception) {
                        return false
                    }
                }
                else return false
            }
            return true
        }
        return false
    }

    private fun removeXMLFiles() {
        val xmlDir = File(filesDir, "XML")
        if (xmlDir.exists() && xmlDir.isDirectory) {
            val files = xmlDir.listFiles()
            if (files != null) {
                for (file in files) {
                    file.delete()
                }
            }
        }
    }
}