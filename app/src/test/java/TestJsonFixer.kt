import com.acme.twitteradeater.JsonTimeline
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.BufferedReader

class TestJsonFixer {

    @Test
    fun empty_json() {
        val actual = JsonTimeline("{}").removePromote()
        val expected = "{}"

        assertThat(actual, equalTo(expected))
    }

    @Test
    fun example_1() {
        val inputStream = javaClass.classLoader.getResourceAsStream("example1.txt")
        val data = inputStream.bufferedReader().use(BufferedReader::readText)
        val actual = JsonTimeline(data).removePromote()
        val gson = Gson()
        val actualJson = gson.fromJson(actual, JsonObject::class.java)

        val nItemsPromoted = actualJson.getAsJsonObject("timeline").getAsJsonArray("instructions").get(0).asJsonObject
        .getAsJsonObject("addEntries").getAsJsonArray("entries").filter {
            it.asJsonObject.get("entryId").asString.contains("promoted")
        }.size

        assertThat(nItemsPromoted, equalTo(0))
    }



    fun example_data_1() {

    }
}