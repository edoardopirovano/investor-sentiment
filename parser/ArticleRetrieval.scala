import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model._
import org.joda.time.DateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await
import com.alchemyapi.api.AlchemyAPI
import com.alchemyapi.api._
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import java.io._;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;

object ArticleRetrieval {
	def main(args: Array[String]) = {
		val client = new GuardianContentClient("5q4xyarnabx5jzxr92rch59v")
		val searchQuery = SearchQuery().q("Google").pageSize(20) // Build a search query of 20 articles about Google
		val response = Await.result(client.getResponse(searchQuery), Duration.Inf) // Wait for result to be returned
		val alchemyObj = AlchemyAPI.GetInstanceFromString("3b90bf78e6b1892aa154ce40a7393d420ea5ee55");
		for (result <- response.results) {
			println(result.webPublicationDate + " - " + result.webTitle)
			println(getStringFromDocument(alchemyObj.URLGetTextSentiment(result.webUrl)));
		}
	}
	private def getStringFromDocument(doc: Document): String = {
		val domSource = new DOMSource(doc)
		val writer = new StringWriter()
		val result = new StreamResult(writer)
		val tf = TransformerFactory.newInstance()
		val transformer = tf.newTransformer()
		transformer.transform(domSource, result)
		writer.toString()
	}
}