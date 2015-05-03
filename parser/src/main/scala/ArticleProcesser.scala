import scala.xml._;
import scala.xml.Elem._;
import scala.xml.Node._;
import scala.collection.immutable._;
import java.io._;
import org.joda.time.DateTime
import javax.xml.transform.sax.SAXResult;
import scala.xml.parsing.NoBindingFactoryAdapter;
import org.xml.sax.SAXException;
import com.typesafe.config.ConfigFactory;
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


/** a class to extract tag data from XML files from a given list of tags */
object ArticleProcesser {
	val conf = ConfigFactory.load();	
	val alchemyKey = conf.getString("apikeys.alchemy");
	val alchemyObj = AlchemyAPI.GetInstanceFromString(alchemyKey);
	// defines params
	val sentimentParams = new AlchemyAPI_TargetedSentimentParams();
	
	def processArticle(url: String, stock: String): (Int, Int) = { // Importance, sentiment
		val importance = SiteRank.getPopularity(url);
		try {
			val alchemyResult = asXml(alchemyObj.URLGetTargetedSentiment(url,stock,sentimentParams));
			return (importance, Math.round((1+getSentimentScore(alchemyResult))*50));
		} catch {
			case e : Exception => throw new IllegalArgumentException(stock+" couldn't be found in "+url);
		}

	}
	
	/** finds the sentiment score within the XML document */
	private def getSentimentScore(file: Node) : Float = {
		return (file \\ "score")(0).text.toFloat;
	} 
	
	private def asXml(dom: _root_.org.w3c.dom.Node): Node = {
	  val source = new DOMSource(dom)
	  val adapter = new NoBindingFactoryAdapter
	  val saxResult = new SAXResult(adapter)
	  val transformerFactory = javax.xml.transform.TransformerFactory.newInstance()
	  val transformer = transformerFactory.newTransformer()
	  transformer.transform(source, saxResult)
	  adapter.rootElem
  }
}